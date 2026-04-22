from fastapi import APIRouter, File, UploadFile
import shutil
import os
from typing import List
from vision_utils.ocr import extract_text
from nlp_module import extract_medicine_info

router = APIRouter(prefix="/images", tags=["images"])

@router.post("/upload_and_extract")
async def upload_and_extract(files: List[UploadFile] = File(...)):
    all_medicines = []

    for file in files:
        file_location = f"static/{file.filename}"
        with open(file_location, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # Run OCR
        ocr_text = extract_text(file_location)
        print(f"DEBUG OCR TEXT for {file.filename}: {ocr_text}")

        # Extract info from this specific image
        medicines_in_image = extract_medicine_info(ocr_text)
        all_medicines.extend(medicines_in_image)

    # Return exactly what the Android app expects: a flat list of medicines
    return {"medicines": all_medicines}
