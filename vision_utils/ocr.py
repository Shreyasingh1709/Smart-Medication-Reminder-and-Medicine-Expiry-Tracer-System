from google.cloud import vision
import io
import os
def extract_text(image_path, key_path="vision_utils/vision_key.json"):
    """
    Extracts text from an image using Google Cloud Vision API.
    Args:
        image_path (str): Path to the image file.
        key_path (str): Path to the Google Vision API key JSON file.
    Returns:
        str: Detected text or empty string if none found.
    """
    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = key_path
    client = vision.ImageAnnotatorClient()
    with io.open(image_path, "rb") as image_file:
        content = image_file.read()
    image = vision.Image(content=content)
    response = client.text_detection(image=image)
    texts = response.text_annotations
    if texts:
        return texts[0].description
    return ""
