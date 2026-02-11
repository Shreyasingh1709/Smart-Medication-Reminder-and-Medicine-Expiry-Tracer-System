from google.cloud import vision

client = vision.ImageAnnotatorClient()
print("✅ Google Vision API connected successfully!")
from vision.ocr import extract_text
from vision.parser import extract_medicine_details
from cnn_module import predict_medicine   # existing team code

# Test image (use any image from medicine_dataset)
image_path = "medicine_dataset/Background_notebook/20260202_111722.jpg"

text = extract_text(image_path)
print("\n--- OCR TEXT ---\n", text)

medicine_name, expiry_date = extract_medicine_details(text)

print("\nMedicine Name:", medicine_name)
print("Expiry Date:", expiry_date)

result = predict_medicine(medicine_name)
print("\nCNN Output:", result)
