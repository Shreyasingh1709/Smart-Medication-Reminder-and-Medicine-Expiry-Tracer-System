
import os
import cv2
import numpy as np

import easyocr
import os

reader = easyocr.Reader(['en'])

def preprocess_image(image_path):
    img = cv2.imread(image_path)
    if img is None:
        return None
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    denoised = cv2.fastNlMeansDenoising(gray, None, 30, 7, 21)
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
    enhanced = clahe.apply(denoised)
    thresh = cv2.adaptiveThreshold(enhanced, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
                                   cv2.THRESH_BINARY, 31, 2)
    # Deskew
    coords = np.column_stack(np.where(thresh > 0))
    angle = 0
    if coords.shape[0] > 0:
        rect = cv2.minAreaRect(coords)
        angle = rect[-1]
        if angle < -45:
            angle = -(90 + angle)
        else:
            angle = -angle
        (h, w) = thresh.shape
        M = cv2.getRotationMatrix2D((w // 2, h // 2), angle, 1.0)
        thresh = cv2.warpAffine(thresh, M, (w, h), flags=cv2.INTER_CUBIC, borderMode=cv2.BORDER_REPLICATE)
    return thresh

def extract_text(image_path):
    try:
        if not os.path.exists(image_path):
            return ""
        # Run EasyOCR directly on the original image (no preprocessing)
        result = reader.readtext(image_path, detail=0, paragraph=True)
        text = "\n".join(result)
        print("[OCR DEBUG] EasyOCR output:\n", text)
        return text
    except Exception as e:
        print(f"OCR Error: {e}")
        return ""
