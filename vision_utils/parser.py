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
