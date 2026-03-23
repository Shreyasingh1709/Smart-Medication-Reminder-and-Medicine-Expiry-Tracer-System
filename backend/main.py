from fastapi import FastAPI
from backend.routers import users, medicines, reminders, adherence, images, auth
from fastapi.middleware.cors import CORSMiddleware
import os

app = FastAPI(title="MediEase Backend")

# Allow all CORS for development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Create static dir for images if not exists
os.makedirs("static", exist_ok=True)

# Routers
app.include_router(users.router)
app.include_router(medicines.router)
app.include_router(reminders.router)
app.include_router(adherence.router)
app.include_router(images.router)
app.include_router(auth.router)

@app.get("/")
def root():
    return {"msg": "MediEase backend running"}
