from dotenv import load_dotenv
load_dotenv()
import os

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


# Test image (use any image from medicine_dataset)
image_path = "medicine_dataset/Background_notebook/20260202_111722.jpg"


text = extract_text(image_path)

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
"""

print(summary)

with open("final_output.txt", "w", encoding="utf-8") as f:
    f.write(summary)
