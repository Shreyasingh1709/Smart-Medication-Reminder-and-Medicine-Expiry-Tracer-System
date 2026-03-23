from fastapi import APIRouter, HTTPException
from backend.models.schemas import Medicine
from typing import List
from datetime import date

router = APIRouter(prefix="/medicines", tags=["medicines"])

# Mock medicines for demo
mock_medicines = [
    Medicine(id=1, name="Aspirin", dosage="1 tab", expiry_date=date(2026, 5, 1), image_url=None, patient_id=1),
    Medicine(id=2, name="Metformin", dosage="500mg", expiry_date=date(2026, 6, 15), image_url=None, patient_id=1),
]

@router.get("/", response_model=List[Medicine])
def list_medicines(patient_id: int):
    return [m for m in mock_medicines if m.patient_id == patient_id]

@router.post("/", response_model=Medicine)
def add_medicine(med: Medicine):
    mock_medicines.append(med)
    return med

@router.delete("/{med_id}")
def delete_medicine(med_id: int):
    global mock_medicines
    mock_medicines = [m for m in mock_medicines if m.id != med_id]
    return {"msg": "deleted"}
