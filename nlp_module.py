import re
from typing import Dict, Any

def extract_medicine_info(text: str):
    """
    Enhanced extraction for multiple medicines from OCR text.
    Returns a list of dicts, each with name, dosage_timing, timing_context, expiry.
    Handles noisy OCR, missing fields, and flexible order.
    """
    import re
    medicines = []
    current = {'name': '', 'dosage_timing': '', 'timing_context': '', 'expiry': ''}

    # Patterns for field extraction
    timing_patterns = [
        r'\b\d(?:-\d){2,3}\b',
        r'\bOD\b', r'\bBD\b', r'\bTDS\b', r'\bQID\b', r'\bHS\b', r'\bSOS\b',
        r'\b1-0-1\b', r'\b0-1-0\b', r'\b1-1-1\b', r'\b0-0-1\b', r'\b1-0-0\b'
    ]
    context_patterns = [
        r'after food', r'before food', r'with food', r'empty stomach', r'at bedtime', r'as needed', r'with water', r'with milk'
    ]
    expiry_patterns = [
        r'expiry[:\s]*([0-9]{1,2}[/-][0-9]{2,4})',
        r'expiry[:\s]*([0-9]{4}-[0-9]{2}-[0-9]{2})',
        r'exp\.?[:\s]*([0-9]{1,2}[/-][0-9]{2,4})',
        r'exp\.?[:\s]*([0-9]{4}-[0-9]{2}-[0-9]{2})'
    ]

    # Heuristic for new medicine block: line starts with 'medicine name:', or is all caps (not a keyword), or matches a known medicine name pattern
    def is_new_medicine_line(line):
        # Don't treat known dosage/context keywords as medicine names
        if any(re.fullmatch(p, line, re.IGNORECASE) for p in timing_patterns + context_patterns):
            return False
        if line.lower().startswith('medicine name:'):
            return True
        if line.isupper() and len(line.split()) <= 4 and 'expiry' not in line.lower():
            return True
        # Heuristic: line with only letters and possibly numbers, not a context or timing
        if re.match(r'^[A-Za-z0-9\- ]{3,}$', line) and not any(re.search(p, line, re.IGNORECASE) for p in timing_patterns + context_patterns):
            return True
        return False

    try:
        for line in text.splitlines():
            line = line.strip()
            if not line:
                continue

            # Detect new medicine block
            if is_new_medicine_line(line):
                if current['name']:
                    # Save previous medicine if valid
                    if not current['dosage_timing']:
                        current['dosage_timing'] = 'no dosage time in prescription, dosage time is extracted from medicine strip'
                    medicines.append(current)
                current = {'name': '', 'dosage_timing': '', 'timing_context': '', 'expiry': ''}
                current['name'] = line.split(':', 1)[-1].strip() if ':' in line else line.strip()
                continue

            # Dosage timing
            for pattern in timing_patterns:
                timing_match = re.search(pattern, line, re.IGNORECASE)
                if timing_match:
                    current['dosage_timing'] = timing_match.group(0)
                    break

            # Contextual instructions
            for cpattern in context_patterns:
                context_match = re.search(cpattern, line, re.IGNORECASE)
                if context_match:
                    current['timing_context'] = context_match.group(0)
                    break

            # Expiry
            if 'exp' in line.lower():
                for epat in expiry_patterns:
                    exp_match = re.search(epat, line, re.IGNORECASE)
                    if exp_match:
                        current['expiry'] = exp_match.group(1)
                        break

        # Add last block
        if current['name']:
            if not current['dosage_timing']:
                current['dosage_timing'] = 'no dosage time in prescription, dosage time is extracted from medicine strip'
            medicines.append(current)
    except Exception as e:
        # Basic error handling and logging
        import logging
        logging.error(f"Error in extract_medicine_info: {e}")
        # Optionally, return what was parsed so far
        return medicines

    return medicines
