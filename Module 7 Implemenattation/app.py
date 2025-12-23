import streamlit as st
import os
from database import create_tables, insert_medicine
from firebase_config import upload_to_firebase
from ocr_module import extract_text, extract_expiry_date, get_ocr_paragraph

# Create database tables if not already present
create_tables()

st.set_page_config(page_title="Medication Reminder", layout="centered")

st.title("💊 Medication Reminder & Expiry Tracker")

# Upload section
st.subheader("📷 Upload Medicine Image")
uploaded_file = st.file_uploader("Choose a medicine image", type=["jpg", "jpeg", "png"])

# Create a folder to store uploads locally
UPLOAD_FOLDER = "uploads"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

if uploaded_file is not None:
    # Save uploaded image temporarily
    image_path = os.path.join(UPLOAD_FOLDER, uploaded_file.name)
    with open(image_path, "wb") as f:
        f.write(uploaded_file.getbuffer())

    st.image(image_path, caption="Uploaded Image", width='stretch')

    # Extract text using OCR
    with st.spinner("Extracting text from image..."):
        lines, _ = extract_text(image_path)
        expiry_date = extract_expiry_date(lines)
        ocr_paragraph = get_ocr_paragraph(image_path)

    st.success("✅ Text Extracted Successfully!")
    st.write("**Detected Text:**", lines)
    st.write("**Detected Paragraph:**")
    st.info(ocr_paragraph)
    st.write("**Detected Expiry Date:**", expiry_date)
    # Show all raw OCR lines for debugging
    if st.button('Show Raw OCR Lines'):
        st.write('**Raw OCR Lines:**')
        for idx, line in enumerate(lines):
            st.write(f"{idx}: {line}")
    # Manual fallback for expiry date
    if not expiry_date or "not readable" in expiry_date.lower() or "ambiguous" in expiry_date.lower():
        expiry_date = st.text_input('Enter Expiry Date (manual override):', value="")
        if expiry_date:
            st.info(f"Expiry date is: {expiry_date}")

    # Input fields for medicine details
    st.subheader("🧺 Enter Medicine Details")
    med_name = st.text_input("Medicine Name")
    patient_id = st.number_input("Patient ID (if exists)", min_value=1, step=1)

    if st.button("Save & Upload"):
        if med_name and expiry_date != "Not detected":
            # Insert into SQLite database
            insert_medicine(med_name, expiry_date, patient_id)

            # Upload to Firebase
            data = {"name": med_name, "expiry_date": expiry_date}
            url = upload_to_firebase(image_path, data)

            st.success("✅ Data Saved Successfully!")
            st.write("🗃️ Image uploaded to Firebase:")
            st.write(url)
        else:
            st.warning("Please fill all fields and ensure expiry date is detected.")

else:
    st.info("Please upload a medicine image to continue.")
