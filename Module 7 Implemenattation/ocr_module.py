# =================================================
# OPTIONAL TESSERACT CHECK
# =================================================
try:
    import pytesseract
    from PIL import Image
    TESSERACT_AVAILABLE = True
except ImportError:
    TESSERACT_AVAILABLE = False

import easyocr
import cv2
import re
import numpy as np

# =================================================
# INITIALIZE EASYOCR ONCE
# =================================================
reader = easyocr.Reader(['en'], gpu=False)

# =================================================
# CROP EXPIRY AREA FROM IMAGE
# =================================================
def crop_expiry_area(image_path):
    img = cv2.imread(image_path)
    h, w = img.shape[:2]
    crop = img[int(h * 0.7):, :]
    temp_path = image_path.replace('.jpg', '_crop.jpg') \
                          .replace('.png', '_crop.png') \
                          .replace('.jpeg', '_crop.jpeg')
    cv2.imwrite(temp_path, crop)
    return temp_path

# =================================================
# METALLIC / EMBOSSED FOIL PREPROCESSING
# =================================================
def preprocess_for_metallic_text(image_path):
    img = cv2.imread(image_path)
    if img is None:
        raise ValueError("Image not found or unreadable")

    h, w = img.shape[:2]
    if max(h, w) < 1400:
        scale = 1400 / max(h, w)
        img = cv2.resize(img, (int(w * scale), int(h * scale)))

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # Reduce glare & normalize lighting
    clahe = cv2.createCLAHE(clipLimit=3.0, tileGridSize=(8, 8))
    gray = clahe.apply(gray)

    # Edge emphasis (important for embossed foil)
    sobelx = cv2.Sobel(gray, cv2.CV_64F, 1, 0, ksize=3)
    sobely = cv2.Sobel(gray, cv2.CV_64F, 0, 1, ksize=3)
    edges = cv2.convertScaleAbs(sobelx + sobely)

    edges = cv2.fastNlMeansDenoising(edges, None, 15, 7, 21)

    thresh = cv2.adaptiveThreshold(
        edges, 255,
        cv2.ADAPTIVE_THRESH_MEAN_C,
        cv2.THRESH_BINARY,
        31, 2
    )

    return thresh

# =================================================
# STANDARD IMAGE PREPROCESSING
# =================================================
def preprocess_image(image_path):
    img = cv2.imread(image_path)
    if img is None:
        raise ValueError("Image not found or unreadable")

    h, w = img.shape[:2]
    if max(h, w) < 1200:
        scale = 1200 / max(h, w)
        img = cv2.resize(img, (int(w * scale), int(h * scale)))

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    gray = cv2.fastNlMeansDenoising(gray, None, 30, 7, 21)

    kernel = np.array([[0, -1, 0], [-1, 5, -1], [0, -1, 0]])
    gray = cv2.filter2D(gray, -1, kernel)

    gray = cv2.equalizeHist(gray)

    thresh = cv2.adaptiveThreshold(
        gray, 255,
        cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY,
        31, 2
    )
    return thresh

# =================================================
# OCR TEXT EXTRACTION WITH CONFIDENCE
# =================================================
def extract_text(image_path, metallic=True):
    img = preprocess_for_metallic_text(image_path) if metallic else preprocess_image(image_path)

    results = reader.readtext(
        img,
        detail=1,
        paragraph=False,
        text_threshold=0.5,
        low_text=0.4,
        link_threshold=0.3
    )

    lines = []
    confidences = []

    for _, text, conf in results:
        if conf > 0.25 and len(text.strip()) > 1:
            lines.append(text.strip())
            confidences.append(conf)

    avg_conf = round(sum(confidences) / len(confidences), 2) if confidences else 0.0
    return lines, avg_conf

# =================================================
# OCR PARAGRAPH
# =================================================
def extract_ocr_paragraph(lines):
    return " ".join(l.strip() for l in lines if l.strip())

# =================================================
# NORMALIZE TEXT
# =================================================
def normalize_text(lines):
    cleaned = []
    for line in lines:
        line = re.sub(r'[^A-Za-z0-9./:-]', ' ', line)
        line = re.sub(r'\s+', ' ', line).strip()
        cleaned.append(line)
    return cleaned

# =================================================
# EXPIRY DATE EXTRACTION (SAFE, NO GUESSING)
# =================================================
def extract_expiry_date(lines):
    month_map = {
        'JAN':'01','FEB':'02','MAR':'03','APR':'04','MAY':'05','JUN':'06',
        'JUL':'07','AUG':'08','SEP':'09','OCT':'10','NOV':'11','DEC':'12'
    }

    # Flatten lines if nested lists exist
    flat_lines = []
    for l in lines:
        if isinstance(l, list):
            flat_lines.extend(l)
        elif isinstance(l, str):
            flat_lines.append(l)
    text = " ".join(flat_lines).upper()


    strict = re.search(
        r'(EXP|EXPIRY)\s*[:\-]?\s*(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)[.\s\-]?(\d{2,4})',
        text
    )
    if strict:
        return f"{month_map[strict.group(2)]}/{strict.group(3)[-2:]}"

    all_dates = re.findall(
        r'(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)[.\s\-]?(\d{2,4})',
        text
    )

    if len(all_dates) >= 2:
        m, y = all_dates[-1]
        return f"{month_map[m]}/{y[-2:]}"

    if len(all_dates) == 1:
        return "Expiry ambiguous (only one date found)"

    return "Expiry not readable from image"

# =================================================
# EXPIRY CONFIDENCE SCORE
# =================================================
def expiry_confidence_score(expiry, ocr_conf):
    if "not readable" in expiry.lower():
        return 0.1
    if "ambiguous" in expiry.lower():
        return 0.3
    if re.match(r'\d{2}/\d{2}', expiry):
        return round(min(1.0, ocr_conf + 0.2), 2)
    return round(ocr_conf * 0.5, 2)

# =================================================
# OTHER FIELD EXTRACTORS
# =================================================
def extract_mfg_date(lines):
    text = " ".join(lines).upper()
    m = re.search(r'(MFG|MFD)[ .:-]*(\d{2}[\/\-]\d{2,4})', text)
    return m.group(2) if m else "Not detected"

def extract_batch_number(lines):
    text = " ".join(lines).upper()
    m = re.search(r'(B\.?NO|BATCH)[ .:-]*([A-Z0-9]+)', text)
    return m.group(2) if m else "Not detected"

def extract_mrp(lines):
    text = " ".join(lines).upper()
    m = re.search(r'(RS|MRP)[ .:-]*([0-9]+\.?[0-9]*)', text)
    return f"₹{m.group(2)}" if m else "Not detected"

def extract_medicine_name(lines):
    for l in lines:
        if l.isupper() and len(l) > 4:
            return l.title()
    return "Not detected"

# =================================================
# MAIN PIPELINE
# =================================================
def extract_medicine_details(image_path):
    lines, ocr_conf = extract_text(image_path, metallic=True)
    lines = normalize_text(lines)

    expiry = extract_expiry_date(lines)
    expiry_conf = expiry_confidence_score(expiry, ocr_conf)

    return {
        "medicine_name": extract_medicine_name(lines),
        "manufacturing_date": extract_mfg_date(lines),
        "expiry_date": expiry,
        "expiry_confidence": expiry_conf,
        "ocr_confidence": ocr_conf,
        "batch_number": extract_batch_number(lines),
        "mrp": extract_mrp(lines),
        "needs_manual_verification": expiry_conf < 0.6,
        "raw_ocr_lines": lines
    }
# -------------------------------------------------
# GET OCR PARAGRAPH (FOR STREAMLIT)
# -------------------------------------------------
def get_ocr_paragraph(image_path):
    """
    Returns full OCR paragraph text for display in Streamlit UI
    """
    lines, _ = extract_text(image_path, metallic=True)
    return " ".join(l.strip() for l in lines if l.strip())

# =================================================
# LOCAL TEST
# =================================================
if __name__ == "__main__":
    image_path = "meftal.jpg"
    result = extract_medicine_details(image_path)

    print("\n----- OCR RESULT -----")
    for k, v in result.items():
        print(f"{k}: {v}")
