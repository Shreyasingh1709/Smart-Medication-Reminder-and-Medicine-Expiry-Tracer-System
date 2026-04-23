import re
from typing import List, Dict, Any

def parse_dose_timing(timing_str: str) -> Dict[str, bool]:
    timing_str = timing_str.strip()
    timing_str = re.sub(r'[oOQD]', '0', timing_str)
    timing_str = re.sub(r'[lI|]', '1', timing_str)

    match = re.search(r'([01])[^0-9]([01])[^0-9]([01])', timing_str)
    if match:
        return {
            "breakfast": match.group(1) == '1',
            "lunch":     match.group(2) == '1',
            "dinner":    match.group(3) == '1',
        }
    result = {"breakfast": False, "lunch": False, "dinner": False}
    t = timing_str.lower()
    if any(w in t for w in ['morning', 'breakfast']):
        result["breakfast"] = True
    if any(w in t for w in ['noon', 'lunch', 'afternoon']):
        result["lunch"] = True
    if any(w in t for w in ['night', 'dinner', 'evening', 'bed']):
        result["dinner"] = True
    return result

def extract_food_instruction(text: str) -> str:
    t = text.lower()
    if re.search(r'af[a-z]{0,3}r?\s*food|after\s*food', t):
        return "after food"
    if re.search(r'before\s*food|empty\s*stomach|bf\s*food', t):
        return "before food"
    if re.search(r'with\s*food', t):
        return "with food"
    return ""

def extract_duration(text: str) -> str:
    match = re.search(
        r'(?:for|x)\s*(\d+)\s*(?:days?|day[a-z]?|dars?|dayt|wccks?|weeks?|months?)',
        text, re.IGNORECASE
    )
    if match:
        num = match.group(1)
        unit_raw = text[match.start():match.end()].lower()
        if re.search(r'w[a-z]{2,4}', unit_raw.split(num)[-1]):
            return f"{num} weeks"
        elif re.search(r'month', unit_raw):
            return f"{num} months"
        else:
            return f"{num} days"
    return ""

def extract_medicine_name(line: str) -> str:
    line = re.sub(r'^\d+[\.\)]\s*', '', line.strip())
    before_paren = re.split(r'\(', line)[0].strip()
    tokens = before_paren.split()
    name_tokens = []
    for tok in tokens:
        clean = re.sub(r'[^A-Za-z0-9]', '', tok)
        if clean and re.match(r'^[A-Z][A-Za-z0-9]{1,}$', clean):
            name_tokens.append(clean)
        if len(name_tokens) >= 3:
            break
    return ' '.join(name_tokens) if name_tokens else before_paren[:30]

def split_into_medicine_lines(ocr_text: str) -> List[str]:
    if not re.search(r'Prescriptons|Prescription|Rx|Diagnosis|Consultant|Patient', ocr_text, re.IGNORECASE):
        return []
    presc_match = re.search(r'Presc?r?ipt?ions?\s*[:\n]?', ocr_text, re.IGNORECASE)
    if presc_match:
        ocr_text = ocr_text[presc_match.end():]
    end_match = re.search(r'Special\s*adv|Next\s*follow|OPD\s*Timing', ocr_text, re.IGNORECASE)
    if end_match:
        ocr_text = ocr_text[:end_match.start()]
    lines = re.split(r'(?=\d+\.\s+[A-Z])|(?=\(\s*[Dd]ispens)', ocr_text)
    result = []
    for line in lines:
        line = line.strip()
        line = re.sub(r'\s+', ' ', line)
        if len(line) < 8:
            continue
        if re.match(r'^\(\s*[Dd]ispens', line):
            continue
        result.append(line)
    return result

def parse_prescription(ocr_text: str) -> List[Dict]:
    lines = split_into_medicine_lines(ocr_text)
    medicines = []
    for line in lines:
        name = extract_medicine_name(line)
        if not name or len(name) < 3:
            continue
        timing_match = re.search(r'[01oOlI][^0-9a-zA-Z][01oOlI][^0-9a-zA-Z][01oOlI]', line)
        timing_str = timing_match.group(0) if timing_match else ""
        timing_text_match = re.search(r'(morning|noon|evening|night|bed\s*time|breakfast|lunch|dinner)', line, re.IGNORECASE)
        timing_context = (timing_str + " " + (timing_text_match.group(0) if timing_text_match else "")).strip()
        dose = parse_dose_timing(timing_context)
        food = extract_food_instruction(line)
        duration = extract_duration(line)
        instr_match = re.search(r'[Ii]nstruct?i?on\s*[:\-]?\s*(.+?)(?:\.|$)', line)
        instruction = instr_match.group(1).strip() if instr_match else ""
        route = ""
        if re.search(r'[Tt]opical', line):
            route = "Topical"
        elif re.search(r'[Oo]rally|[Oo]ral', line):
            route = "Oral"
        timing_meaning = []
        if dose["breakfast"]: timing_meaning.append("Morning")
        if dose["lunch"]: timing_meaning.append("Afternoon")
        if dose["dinner"]: timing_meaning.append("Night")
        medicines.append({
            "medicine_name": name,
            "dosage": "",
            "timing_meaning": ", ".join(timing_meaning),
            "food_instruction": food,
            "duration": duration,
            "route": route,
            "special_instruction": instruction
        })
    return medicines

def extract_structured_prescriptions(text: str) -> List[Dict[str, Any]]:
    return parse_prescription(text)
