from fastapi import APIRouter, HTTPException, BackgroundTasks
from ..models.schemas import User, Token
from typing import List
import random
import string
import smtplib
from email.mime.text import MIMEText
import os
from dotenv import load_dotenv
load_dotenv()

router = APIRouter(prefix="/auth", tags=["auth"])


# OTP logic temporarily disabled. Uncomment to re-enable.
# otp_store = {}


SMTP_SERVER = os.getenv("SMTP_SERVER")
SMTP_PORT = 587
SMTP_USER = os.getenv("SMTP_USER")
SMTP_PASS = os.getenv("SMTP_PASS")


# @router.post("/send_otp")
# def send_otp(email: str, background_tasks: BackgroundTasks):
#     if not email or "@" not in email:
#         raise HTTPException(status_code=400, detail="Invalid email")
#     otp = ''.join(random.choices(string.digits, k=6))
#     otp_store[email] = otp
#     background_tasks.add_task(send_email, email, otp)
#     return {"msg": "OTP sent"}


# def send_email(email, otp):
#     msg = MIMEText(f"Your MediEase OTP is: {otp}")
#     msg["Subject"] = "MediEase OTP Verification"
#     msg["From"] = SMTP_USER
#     msg["To"] = email
#     with smtplib.SMTP(SMTP_SERVER, SMTP_PORT) as server:
#         server.starttls()
#         server.login(SMTP_USER, SMTP_PASS)
#         server.sendmail(SMTP_USER, [email], msg.as_string())


# @router.post("/verify_otp")
# def verify_otp(email: str, otp: str):
#     if otp_store.get(email) == otp:
#         del otp_store[email]
#         return {"msg": "OTP verified"}
#     raise HTTPException(status_code=400, detail="Invalid OTP")
