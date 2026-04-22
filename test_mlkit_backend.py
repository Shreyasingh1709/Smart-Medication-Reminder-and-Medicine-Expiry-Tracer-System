import requests

# Example OCR text from ML Kit (replace with your actual extracted text)
ocr_text = """
Paracetamol 500mg
1-0-1 after food
EXP 12/25

Amoxicillin 250mg
5 ml twice daily before food
EXP DEC 2025

Ofloxacin Eye Drops
2 drops every 6 hours
EXP 11/2026
"""

# Backend endpoint URL (update host/port if needed)
url = "http://127.0.0.1:8000/parse_medicine"

# Prepare JSON payload
payload = {"ocr_text": ocr_text}

# Send POST request
response = requests.post(url, json=payload)

# Print the parsed medicine info
print("Response from backend:")
print(response.json())
