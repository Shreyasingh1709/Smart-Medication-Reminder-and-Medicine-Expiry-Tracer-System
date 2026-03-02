import re
from typing import Dict, Any

def extract_medicine_info(text: str):
    """
    Extracts info for multiple medicines from OCR text.
    Returns a list of dicts, each with name, dosage_timing, timing_context, expiry.
    """
    import re
    medicines = []
    current = {'name': '', 'dosage_timing': '', 'timing_context': '', 'expiry': ''}

    timing_patterns = [
        r'\b\d(?:-\d){2,3}\b',
        r'\bOD\b', r'\bBD\b', r'\bTDS\b', r'\bQID\b', r'\bHS\b', r'\bSOS\b'
    ]
    context_patterns = [
        r'after food', r'before food', r'with food', r'empty stomach', r'at bedtime', r'as needed'
    ]

    # Split by lines and look for medicine blocks
    for line in text.splitlines():
        line = line.strip()
        if not line:
            continue

        # New medicine block (heuristic: line starts with 'medicine name:' or is all caps and not a known keyword)
        if line.lower().startswith('medicine name:') or (line.isupper() and len(line.split()) <= 4 and 'expiry' not in line.lower()):
            # Save previous if valid
            if current['name']:
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
        if 'expiry' in line.lower():
            exp_match = re.search(r'([0-9]{1,2}[/-][0-9]{2,4}|[0-9]{4}-[0-9]{2}-[0-9]{2})', line)
            if exp_match:
                current['expiry'] = exp_match.group(0)

    # Add last block
    if current['name']:
        medicines.append(current)

    return medicines
