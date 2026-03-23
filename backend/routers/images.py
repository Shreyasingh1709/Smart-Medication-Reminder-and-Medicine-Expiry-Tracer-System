from fastapi import APIRouter, File, UploadFile
import shutil
import os

router = APIRouter(prefix="/images", tags=["images"])

@router.post("/upload")
def upload_image(file: UploadFile = File(...)):
    file_location = f"static/{file.filename}"
    with open(file_location, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    return {"image_url": f"/{file_location}"}
