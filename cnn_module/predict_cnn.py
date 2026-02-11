"""
CNN Prediction Module (Frontend-safe)
"""
import tensorflow as tf
import numpy as np
from tensorflow.keras.preprocessing import image
import os

MODEL_PATH = "mobilenetv2_medicine.tflite"
IMG_SIZE = (128, 128)

CLASS_NAMES = ["Tablet", "Syrup", "Injection"]  # adjust order

# Load TFLite model ONCE
interpreter = tf.lite.Interpreter(model_path=MODEL_PATH)
interpreter.allocate_tensors()

input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

def predict_medicine_from_image(img_path):
    if not os.path.exists(img_path):
        return "Image not found"

    img = image.load_img(img_path, target_size=IMG_SIZE)
    img_array = image.img_to_array(img) / 255.0
    img_array = np.expand_dims(img_array, axis=0).astype(np.float32)

    interpreter.set_tensor(input_details[0]['index'], img_array)
    interpreter.invoke()

    output = interpreter.get_tensor(output_details[0]['index'])
    class_index = np.argmax(output)

    return CLASS_NAMES[class_index]

if __name__ == "__main__":
    test_image = "test_images/sample1.jpeg"  # change this path
    result = predict_medicine_from_image(test_image)
    print("Prediction:", result)

