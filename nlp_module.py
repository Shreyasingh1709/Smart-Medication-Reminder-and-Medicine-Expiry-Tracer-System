import regex as re
from typing import List, Dict, Any

MEDICINE_NAMES = ["Meftal-Spas", "Paracetamol", "Dolo", "Crocin", "Aspirin"]  # Add more as needed

def clean_ocr_text(text: str) -> str:
    # Enhanced OCR error corrections (convert letters to digits where appropriate)
    replacements = {
        'I': '1', 'l': '1', '|': '1', 'T': '1',
        'O': '0', 'Q': '0', 'D': '0', 'U': '0',
        'S': '5', 'Z': '2',
        'B': '8', 'G': '6',
        'A': '4', 'E': '3',
    }
    for k, v in replacements.items():
        text = text.replace(k, v)
    return text

def fuzzy_find_name(text: str) -> str:
    # Try to match known medicine names with regex fuzzy matching
    for med in MEDICINE_NAMES:
        # Allow up to 2 errors in matching
        pattern = f'({med}){{e<=2}}'
        match = re.search(pattern, text, flags=re.IGNORECASE)
        if match:
            return med
    return None

    # (Removed incorrect indentation)
def extract_medicine_info(text: str) -> List[Dict[str, Any]]:

    # Prepare both original and cleaned text
    print("[DEBUG] RAW OCR OUTPUT:", text)
    original_text = text.upper().replace('\n', ' ')
    print("[DEBUG] OCR TEXT:", original_text)
    expiry = None  # Initialize expiry to None

    # 1. Extract Name (fuzzy) from original text
    name = None
    for med in MEDICINE_NAMES:
        if med.replace('-', '').upper() in original_text.replace('-', ''):
            name = med
            break
    if not name:
        # Try fuzzy search
        name = fuzzy_find_name(original_text)
    if not name:
        # Fallback: Find first long word
        words = re.findall(r'\b[A-Z]{4,}\b', original_text)
        for w in words:
            if w not in ["DATE", "TABS", "INCL", "TAXES", "BATCH", "MFG"]:
                name = w.capitalize()
                break

    # 2. Extract Dosage (fuzzy regex) from original text (not cleaned)
    dosage = None
    dosage_match = re.search(r'(\d+\s*(?:MG|ML|MCG|G)){{e<=1}}', original_text, flags=re.IGNORECASE)
    if dosage_match:
        dosage = dosage_match.group(1).lower()

    # 3. Extract Expiry (robust for all common patterns) from original text (not cleaned)
    # Patterns: EXP DATE 11/25, EXP. 11-2025, EXP: 11.25, EXPIRY 11/2025, EXP 11/25, EXP 11-25, EXP 11.25, EXP 11 25, etc.
    expiry_patterns = [
        r'(EXP[ .]?DATE|EXPIRY)[ :.,-]*([0-3]?\d[/-][12]\d{1,3})',   # 11/25, 11/2025, 11-25, 11-2025
        r'(EXP[ .]?DATE|EXPIRY)[ :.,-]*([0-3]?\d[.][12]\d{1,3})',    # 11.25, 11.2025
        r'(EXP[ .]?DATE|EXPIRY)[ :.,-]*([0-3]?\d[ ]+[12]\d{1,3})',   # 11 25, 11 2025
        r'(EXP[ .]?DATE|EXPIRY)[ :.,-]*([0-3]?\d)',                   # Only month (rare)
        r'(EXP[ .]?DATE|EXPIRY)[ :.,-]*([12]\d{1,3})',                # Only year (rare)
        r'(EXP[ .]?DATE|EXPIRY)[ :.,-]*([0-3]?\d[/-][01]?\d{1,2})',  # 11/05, 11-05 (month/day)
    ]

    def clean_digits_only(s):
        replacements = {
            'I': '1', 'l': '1', '|': '1', 'T': '1',
            'O': '0', 'Q': '0', 'D': '0', 'U': '0',
            'S': '5', 'Z': '2',
            'B': '8', 'G': '6',
            'A': '4', 'E': '3',
        }
        return ''.join(replacements.get(c, c) for c in s)

    import calendar
    def format_expiry_date(expiry):
        # Accept only valid expiry formats, else return None
        # Try MM/YY, MM/YYYY, MM-YY, MM-YYYY, MM.YY, MM.YYYY
        match = re.match(r'^(0[1-9]|1[0-2])[/-](\d{2,4})$', expiry)
        if match:
            mm = int(match.group(1))
            yy = match.group(2)
            if len(yy) == 2:
                yyyy = int('20' + yy)
            else:
                yyyy = int(yy)
            last_day = calendar.monthrange(yyyy, mm)[1]
            return f"{last_day:02d}/{mm:02d}/{yyyy}"
        # Try MM YYYY or MM YYYY (space)
        match = re.match(r'^(0[1-9]|1[0-2])[ .](\d{2,4})$', expiry)
        if match:
            mm = int(match.group(1))
            yy = match.group(2)
            if len(yy) == 2:
                yyyy = int('20' + yy)
            else:
                yyyy = int(yy)
            last_day = calendar.monthrange(yyyy, mm)[1]
            return f"{last_day:02d}/{mm:02d}/{yyyy}"
        # Already DD/MM/YYYY or DD/MM/YY
        match = re.match(r'^(0[1-9]|[12][0-9]|3[01])[/-](0[1-9]|1[0-2])[/-](\d{2,4})$', expiry)
        if match:
            return expiry
        # If only a single number (month or year), ignore
        if re.fullmatch(r'\d{1,4}', expiry):
            return None
        return None

    for pat in expiry_patterns:
        exp_match = re.search(pat, original_text, flags=re.IGNORECASE)
        if exp_match:
            expiry_raw = exp_match.group(2)
            # Only clean the digits part, not the keyword
            expiry = clean_digits_only(expiry_raw)
            # Remove trailing non-date chars (e.g., comma, period, etc.)
            expiry = re.sub(r'[^0-9/\-. ]', '', expiry)
            expiry = format_expiry_date(expiry)
            if not expiry:
                continue  # Try next pattern if not valid
            break
    if not expiry:
        # Fallback: find any date-like pattern NEAR expiry keywords, and NOT near MFG keywords
        date_matches = list(re.finditer(r'([0-1]?\d[/-][12]\d{1,3})', original_text, flags=re.IGNORECASE))
        expiry = None
        for match in date_matches:
            start, end = match.start(), match.end()
            window = original_text[max(0, start-20):min(len(original_text), end+20)]
            # Check for expiry keywords in window, and NOT mfg keywords
            if re.search(r'exp|expiry', window, re.IGNORECASE) and not re.search(r'mfg|manufactur', window, re.IGNORECASE):
                expiry = clean_digits_only(match.group(1))

    print(f"[DEBUG] Extracted expiry: {expiry}")

    result = {
        "name": name or "",
        "dosage": dosage or "",
        "expiry_date": expiry or "",
        "instructions": "As prescribed by physician",
        "missing_fields": []
    }
    if not name: result["missing_fields"].append("name")
    if not dosage: result["missing_fields"].append("dosage")
    if not expiry: result["missing_fields"].append("expiry_date")
    return [result]