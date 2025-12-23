import firebase_admin
from firebase_admin import credentials, storage, firestore
import os

def initialize_firebase():
    if not firebase_admin._apps:
        cred = credentials.Certificate("firebase_key.json")
        firebase_admin.initialize_app(cred, {
            'storageBucket': 'your-project-id.appspot.com'  # replace with your actual Firebase bucket
        })

def upload_to_firebase(image_path, data):
    initialize_firebase()
    db = firestore.client()
    bucket = storage.bucket()
    filename = os.path.basename(image_path)
    blob = bucket.blob(f"medicine_images/{filename}")
    blob.upload_from_filename(image_path)
    blob.make_public()
    url = blob.public_url
    # Save info to Firestore
    db.collection("Medicines").add({
        "name": data["name"],
        "expiry_date": data["expiry_date"],
        "image_url": url
    })
    return url

def test_firebase_connection():
    initialize_firebase()
    db = firestore.client()
    print("🪪 Testing Firestore connection...")
    try:
        doc_ref = db.collection("test_collection").document("test_doc")
        doc_ref.set({"message": "Firebase connection successful!"})
        doc = db.collection("test_collection").document("test_doc").get()
        if doc.exists:
            print("✅ Firebase connected successfully:", doc.to_dict())
        else:
            print("⚠️ Firebase connected but document not found.")
    except Exception as e:
        print("❌ Firebase connection failed:", e)

if __name__ == "__main__":
    test_firebase_connection()
