# MediEase Backend - FastAPI

This backend provides RESTful APIs for the MediEase app, supporting:
- User roles: Patient, Caregiver
- Medicine management (CRUD, expiry tracking)
- Reminders (CRUD, notifications)
- Adherence analytics (missed doses, stats)
- Image upload endpoints
- Mock authentication
- JSON responses matching Android models

## Structure
- main.py: FastAPI entry point
- models/: Pydantic models
- routers/: API endpoints
- database/: DB logic (SQLite for demo)
- static/: Uploaded images

## To run:
```sh
pip install -r requirements.txt
uvicorn main:app --reload
```
