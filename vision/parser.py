import re

def extract_medicine_details(text):
    expiry = re.findall(r"(EXP|Expiry)[^\d]*(\d{2}/\d{2,4})", text, re.IGNORECASE)
    expiry_date = expiry[0][1] if expiry else "Not Found"

    lines = text.split("\n")
    medicine_name = lines[0] if lines else "Unknown"

    return medicine_name, expiry_date
