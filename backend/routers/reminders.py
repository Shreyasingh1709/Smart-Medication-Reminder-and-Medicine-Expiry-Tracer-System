from fastapi import APIRouter
from ..models.schemas import Reminder
from typing import List
from datetime import time

router = APIRouter(prefix="/reminders", tags=["reminders"])

# Mock reminders for demo
mock_reminders = [
    Reminder(id=1, medicine_id=1, time=time(8, 0), days=["Mon", "Tue", "Wed"], active=True),
    Reminder(id=2, medicine_id=2, time=time(20, 0), days=["Mon", "Thu"], active=True),
]

@router.get("/", response_model=List[Reminder])
def list_reminders(medicine_id: int):
    return [r for r in mock_reminders if r.medicine_id == medicine_id]

@router.post("/", response_model=Reminder)
def add_reminder(rem: Reminder):
    mock_reminders.append(rem)
    return rem

@router.delete("/{rem_id}")
def delete_reminder(rem_id: int):
    global mock_reminders
    mock_reminders = [r for r in mock_reminders if r.id != rem_id]
    return {"msg": "deleted"}
