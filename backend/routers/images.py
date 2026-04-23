from fastapi import APIRouter, File, UploadFile
import shutil
import os
from typing import List
from vision_utils.ocr import extract_text
from nlp_strip import extract_medicine_info
from nlp_prescription import extract_structured_prescriptions

router = APIRouter(prefix="/images", tags=["images"])

@router.post("/upload_and_extract")
async def upload_and_extract(files: List[UploadFile] = File(...)):
    all_medicines = []

    for file in files:
        file_location = f"static/{file.filename}"
        if not os.path.exists("static"):
            os.makedirs("static")

        with open(file_location, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # Run OCR
        ocr_text = extract_text(file_location)
        print(f"DEBUG OCR TEXT: {ocr_text}")

        # Heuristic: Check if this is a prescription or a strip
        prescription_keywords = ["HOSPITAL", "DIAGNOSIS", "DOCTOR", "CONSULTANT", "DR.", "PATIENT"]
        is_prescription = any(kw in ocr_text.upper() for kw in prescription_keywords)

        if is_prescription:
            print("Processing as PRESCRIPTION")
            p_meds = extract_structured_prescriptions(ocr_text)
            for pm in p_meds:
                all_medicines.append({
                    "name": pm["medicine_name"],
                    "dosage": pm.get("dosage", ""),
                    "instructions": f"{pm.get('timing_meaning', '')} {pm.get('food_instruction', '')} {pm.get('duration', '')}".strip(),
                    "expiry_date": ""
                })
        else:
            print("Processing as MEDICINE STRIP")
            s_meds = extract_medicine_info(ocr_text)
            all_medicines.extend(s_meds)

    return {"medicines": all_medicines}
