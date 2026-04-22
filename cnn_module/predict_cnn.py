"""
CNN Prediction Module (Frontend-safe, Mock-friendly)
"""

import tensorflow as tf
import numpy as np
from tensorflow.keras.preprocessing import image
import os

MODEL_PATH = "cnn_module/mobilenetv2_medicine.tflite"
IMG_SIZE = (128, 128)

CLASS_NAMES = ["Tablet", "Syrup", "Injection"]  # Adjust to your trained order


# ---------------------------
# Load model safely
# ---------------------------
def load_tflite_model():
    if not os.path.exists(MODEL_PATH):
        print(f"[ERROR] Model not found at: {MODEL_PATH}")
        return None

    interpreter = tf.lite.Interpreter(model_path=MODEL_PATH)
    interpreter.allocate_tensors()
    return interpreter


interpreter = load_tflite_model()

if interpreter:
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
else:
    input_details = output_details = None


# ---------------------------
# Predict function
# ---------------------------
def predict_medicine_from_image(img_path):
    """
    Returns:
      {
        'success': True/False,
        'prediction': "<Tablet/Syrup/Injection>",
        'error': "<error message if any>"
      }
    """

    # Check if model loaded
    if interpreter is None:
        return {
            "success": False,
            "prediction": None,
            "error": "Model not loaded. Check file path."
        }

    # Check if image exists
    if not os.path.exists(img_path):
        return {
            "success": False,
            "prediction": None,
            "error": f"Image not found: {img_path}"
        }

    try:
        # Load & preprocess image
        img = image.load_img(img_path, target_size=IMG_SIZE)
        img_array = image.img_to_array(img) / 255.0
        img_array = np.expand_dims(img_array, axis=0).astype(np.float32)

        # Run inference
        interpreter.set_tensor(input_details[0]['index'], img_array)
        interpreter.invoke()

        output = interpreter.get_tensor(output_details[0]['index'])
        class_index = np.argmax(output)

        return {
            "success": True,
            "prediction": CLASS_NAMES[class_index],
            "error": None
        }

    except Exception as e:
        return {
            "success": False,
            "prediction": None,
            "error": str(e)
        }


# ---------------------------
# Manual test (only for debugging)
# ---------------------------
if __name__ == "__main__":
    test_image = "test_images/Tablet/sample1.jpeg"  # Updated path
    result = predict_medicine_from_image(test_image)
    print(result)
