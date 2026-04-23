import regex as re
import calendar
from typing import List, Dict, Any

MEDICINE_NAMES = ["Meftal-Spas", "Paracetamol", "Dolo", "Crocin", "Aspirin"]

def clean_ocr_text(text: str) -> str:
    # Safe cleaning for digits-only fields
    replacements = {'I': '1', 'l': '1', '|': '1', 'O': '0', 'Q': '0'}
    for k, v in replacements.items():
        text = text.replace(k, v)
    return text

def fuzzy_find_name(text: str) -> str:
    for med in MEDICINE_NAMES:
        pattern = f'({med}){{e<=2}}'
        match = re.search(pattern, text, flags=re.IGNORECASE)
        if match: return med
    return None

def extract_medicine_info(text: str) -> List[Dict[str, Any]]:
    original_text = text.upper().replace('\n', ' ')

    # 1. Extract Name (Use raw text)
    name = fuzzy_find_name(original_text)
    if not name:
        words = re.findall(r'\b[A-Z]{4,}\b', original_text)
        for w in words:
            if w not in ["DATE", "TABS", "BATCH", "MFG", "EXP", "INCL", "TAXES", "READER", "HDM"]:
                name = w.capitalize()
                break

    # 2. Extract Dosage (Skip noise at start of OCR)
    dosage = ""
    # Serial numbers like "5 2 4 5 2 7 g" at start are ignored
    search_body = original_text[80:] if len(original_text) > 80 else original_text
    dm = re.search(r'(\d+\s*(?:MG|ML|MCG|G|TABS|CAPS))', search_body, flags=re.IGNORECASE)
    if dm: dosage = dm.group(1).lower()

    # 3. Robust Expiry (dd/mm/yyyy)
    expiry = ""
    # Find position of EXP in raw text to avoid garbling E/A/X
    exp_pos = original_text.find("EXP")
    if exp_pos != -1:
        # Segment 30 chars after EXP and clean for digits
        segment = clean_ocr_text(original_text[exp_pos:exp_pos+30])
        date_match = re.search(r'(\d{1,2})[/-](\d{2,4})', segment)
        if date_match:
            mm, yy = int(date_match.group(1)), date_match.group(2)
            if 1 <= mm <= 12:
                yyyy = int('20' + yy) if len(yy) == 2 else int(yy)
                if yyyy >= 2024: # Filter out past dates (like MFG)
                    last_day = calendar.monthrange(yyyy, mm)[1]
                    expiry = f"{last_day:02d}/{mm:02d}/{yyyy}"

    return [{
        "name": name or "Unknown",
        "dosage": dosage,
        "expiry_date": expiry,
        "instructions": "Extracted from packaging",
        "missing_fields": []
    }]
