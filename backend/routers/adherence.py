from fastapi import APIRouter
from backend.models.schemas import AdherenceStat
from typing import List

router = APIRouter(prefix="/adherence", tags=["adherence"])

# Mock adherence stats for demo
mock_stats = [
    AdherenceStat(medicine_id=1, missed_doses=2, taken_doses=12, adherence_percent=85.7, week="2026-W10"),
    AdherenceStat(medicine_id=2, missed_doses=0, taken_doses=14, adherence_percent=100.0, week="2026-W10"),
]

@router.get("/", response_model=List[AdherenceStat])
def get_adherence_stats(patient_id: int):
    return mock_stats
