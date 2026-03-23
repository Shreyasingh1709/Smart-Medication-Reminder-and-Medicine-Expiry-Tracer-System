from pydantic import BaseModel
from typing import List, Optional
from datetime import date, time

class User(BaseModel):
    id: int
    username: str
    role: str  # 'patient' or 'caregiver'
    patient_ids: Optional[List[int]] = None  # For caregivers

class Medicine(BaseModel):
    id: int
    name: str
    dosage: str
    expiry_date: date
    image_url: Optional[str] = None
    patient_id: int

class Reminder(BaseModel):
    id: int
    medicine_id: int
    time: time
    days: List[str]  # e.g. ["Mon", "Tue"]
    active: bool

class AdherenceStat(BaseModel):
    medicine_id: int
    missed_doses: int
    taken_doses: int
    adherence_percent: float
    week: Optional[str] = None
    month: Optional[str] = None

class Token(BaseModel):
    access_token: str
    token_type: str
