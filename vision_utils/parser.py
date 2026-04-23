import re


def extract_medicine_details(text):
    # Try to extract expiry date from label first
    expiry_label = re.search(r"Expiry Date:\s*([\d/]+)", text, re.IGNORECASE)
    if expiry_label:
        expiry_date = expiry_label.group(1)
    else:
        expiry = re.findall(r"(EXP|Expiry)[^\d]*(\d{2}/\d{2,4})", text, re.IGNORECASE)
        expiry_date = expiry[0][1] if expiry else "Not Found"

    # Try to extract medicine name from label first
    med_label = re.search(r"Medicine Name:\s*(.+)", text, re.IGNORECASE)
    if med_label:
        medicine_name = med_label.group(1).strip()
    else:
        lines = text.split("\n")
        medicine_name = lines[0] if lines else "Unknown"

    return medicine_name, expiry_date

# --- New function for extracting dosage and timing patterns ---
def extract_dosage_and_timing(text):
    # Dosage patterns: 1-0-1, 0-1-0, 1-0-0, n-o-, f-o-0i, etc.
    dosage_pattern = re.compile(r"(\d\s*[-oO0]\s*\d\s*[-oO0]\s*\d|[nfNF][-oO0][-oO0iI1lL0])")
    dosages = dosage_pattern.findall(text)

    # Timing instructions
    timing_keywords = [
        r"after meal", r"before meal", r"after food", r"before food", r"bed time", r"bedtime",
        r"morning", r"night", r"evening", r"afternoon", r"breakfast", r"lunch", r"dinner"
    ]
    timing_matches = []
    for keyword in timing_keywords:
        matches = re.findall(keyword, text, re.IGNORECASE)
        timing_matches.extend(matches)

    # Clean up dosage patterns (normalize common OCR errors)
    cleaned_dosages = []
    for d in dosages:
        d = d.replace('o', '0').replace('O', '0').replace('i', '1').replace('I', '1').replace('l', '1').replace('L', '1')
        d = re.sub(r'\s+', '', d)
        cleaned_dosages.append(d)

    return {
        'dosages': cleaned_dosages,
        'timings': timing_matches
    }
