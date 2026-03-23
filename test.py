from dotenv import load_dotenv
load_dotenv()
import os
from vision_utils.ocr import extract_text

result = extract_text(r"C:\Users\shrey\OneDrive\Smart Medication Reminder\medicine_dataset\Background_notebook\20260202_111728.jpg")
print(result)
USE_MOCK_OCR = os.getenv("USE_MOCK_OCR", "1") == "1"

if not USE_MOCK_OCR:
	from google.cloud import vision
	client = vision.ImageAnnotatorClient()
	print("Google Vision API connected successfully!")
	from vision_utils.ocr import extract_text
else:
	def extract_text(image_path):
		# Use mock OCR output from local file
		import pathlib
		fname = pathlib.Path(image_path).stem + ".txt"
		mock_path = os.path.join("mock_ocr_outputs", fname)
		try:
			with open(mock_path, "r", encoding="utf-8") as f:
				return f.read()
		except FileNotFoundError:
			return "[MOCK OCR] No mock output found for " + image_path
from vision_utils.parser import extract_medicine_details
from cnn_module.predict_cnn import predict_medicine_from_image   # existing team code
from nlp_module import extract_medicine_info



# Test image (use any image from medicine_dataset)
image_path = "medicine_dataset/Background_notebook/20260202_111722.jpg"


text = extract_text(image_path)

if not text or text.strip() == "":
    print("No prescription found, trying to find dosage time in medicine strip.")

medicine_name, expiry_date = extract_medicine_details(text)
result = predict_medicine_from_image(image_path)

# Extract line_count from mock OCR text if present
import re
line_count_match = re.search(r"line_count\s*=\s*(\d+)", text)
if line_count_match:
    line_count = int(line_count_match.group(1))
    def lines_to_dosage(line_count):
        if line_count == 1:
            return "1 time per day"
        elif line_count == 2:
            return "2 times per day"
        elif line_count == 3:
            return "3 times per day"
        elif line_count == 0:
            return "No dosage lines detected"
        else:
            return f"{line_count} times per day"
    dosage_str = lines_to_dosage(line_count)
else:
    dosage_str = "Not specified"

if (not text or text.strip() == "") and dosage_str == "Not specified":
    print("No prescription and no dosage found in strip, please enter dosage manually.")
elif dosage_str == "Not specified":
    print("Dosage time not found in prescription or strip, please enter manually.")

# --- New test: Use improved extraction logic ---
print("\n--- EXTRACTED MEDICINES (using nlp_module) ---")
medicines = extract_medicine_info(text)
for idx, med in enumerate(medicines, 1):
    print(f"Medicine {idx}:")
    for k, v in med.items():
        print(f"  {k}: {v}")
    print()

# Prepare final output summary
summary = f"""
--- FINAL REPORT ---
Image: {image_path}

--- OCR TEXT ---
{text}

Medicine Name: {medicine_name}
Expiry Date: {expiry_date}

CNN Output: {result}

Dosage (from lines): {dosage_str}

--- EXTRACTED MEDICINES (using nlp_module) ---
"""
summary += "\n".join([
    f"Medicine {i+1}:\n  " + "\n  ".join(f"{k}: {v}" for k, v in med.items())
    for i, med in enumerate(medicines)
]) + "\n"

print(summary)

with open("final_output.txt", "w", encoding="utf-8") as f:
    f.write(summary)
