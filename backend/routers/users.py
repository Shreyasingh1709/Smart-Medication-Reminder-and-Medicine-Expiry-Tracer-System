from fastapi import APIRouter, HTTPException
from backend.models.schemas import User, Token
from typing import List

router = APIRouter(prefix="/users", tags=["users"])

# Mock users for demo
mock_users = [
    User(id=1, username="patient1", role="patient"),
    User(id=2, username="caregiver1", role="caregiver", patient_ids=[1]),
]

@router.post("/login", response_model=Token)
def login(username: str, password: str):
    # Mock login, always returns token
    for user in mock_users:
        if user.username == username:
            return Token(access_token="mocktoken", token_type="bearer")
    raise HTTPException(status_code=401, detail="Invalid credentials")

@router.get("/me", response_model=User)
def get_me():
    # Always return first user for demo
    return mock_users[0]

@router.get("/", response_model=List[User])
def list_users():
    return mock_users
