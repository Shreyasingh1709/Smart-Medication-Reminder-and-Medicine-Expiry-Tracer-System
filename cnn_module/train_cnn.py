"""
CNN Training Module for Medicine Image Classification
Exports TFLite model compatible with predict_cnn.py
"""

import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D
from tensorflow.keras.models import Model
import json
import os
import matplotlib.pyplot as plt
import numpy as np
from sklearn.metrics import accuracy_score, f1_score
# ---------------------------
# Config
# ---------------------------
DATASET_PATH = "medicine_dataset/"
IMG_SIZE = (128, 128)
BATCH_SIZE = 32
EPOCHS = 10
MODEL_OUTPUT_DIR = "cnn_module"
MODEL_NAME = "mobilenetv2_medicine.tflite"

os.makedirs(MODEL_OUTPUT_DIR, exist_ok=True)

# ---------------------------
# Data generators
# ---------------------------
datagen = ImageDataGenerator(
    rescale=1.0 / 255,
    validation_split=0.2,
    horizontal_flip=True,
    rotation_range=20
)

train_gen = datagen.flow_from_directory(
    DATASET_PATH,
    target_size=IMG_SIZE,
    batch_size=BATCH_SIZE,
    class_mode="categorical",
    subset="training"
)

val_gen = datagen.flow_from_directory(
    DATASET_PATH,
    target_size=IMG_SIZE,
    batch_size=BATCH_SIZE,
    class_mode="categorical",
    subset="validation"
)

NUM_CLASSES = train_gen.num_classes
CLASS_NAMES = list(train_gen.class_indices.keys())

print("Class order:", CLASS_NAMES)

# ---------------------------
# Model
# ---------------------------
base_model = MobileNetV2(
    weights="imagenet",
    include_top=False,
    input_shape=IMG_SIZE + (3,)
)

base_model.trainable = False

x = base_model.output
x = GlobalAveragePooling2D()(x)
x = Dense(64, activation="relu")(x)
outputs = Dense(NUM_CLASSES, activation="softmax")(x)

model = Model(inputs=base_model.input, outputs=outputs)

model.compile(
    optimizer="adam",
    loss="categorical_crossentropy",
    metrics=["accuracy"]
)

# ---------------------------
# Train
# ---------------------------

# Train and capture history
history = model.fit(
    train_gen,
    validation_data=val_gen,
    epochs=EPOCHS
)

# ---------------------------
# Plot and save accuracy/loss graphs
# ---------------------------
plt.figure(figsize=(12, 5))

# Accuracy plot
plt.subplot(1, 2, 1)
plt.plot(history.history['accuracy'], label='Train Accuracy')
plt.plot(history.history['val_accuracy'], label='Val Accuracy')
# Add test accuracy if available
import json
test_metrics_path = os.path.join(MODEL_OUTPUT_DIR, 'test_metrics.json')
if os.path.exists(test_metrics_path):
    with open(test_metrics_path, 'r') as f:
        test_metrics = json.load(f)
    test_acc = test_metrics.get('test_accuracy', None)
    if test_acc is not None:
        plt.axhline(y=test_acc, color='g', linestyle='--', label='Test Accuracy')
plt.title('Model Accuracy')
plt.xlabel('Epoch')
plt.ylabel('Accuracy')
plt.legend()

# Loss plot
plt.subplot(1, 2, 2)
plt.plot(history.history['loss'], label='Train Loss')
plt.plot(history.history['val_loss'], label='Val Loss')
plt.title('Model Loss')
plt.xlabel('Epoch')
plt.ylabel('Loss')
plt.legend()

plt.tight_layout()
plot_path = os.path.join(MODEL_OUTPUT_DIR, 'training_curves.png')
plt.savefig(plot_path)
plt.close()
print(f"\U0001F4C8 Training curves saved to {plot_path}")

# ---------------------------
# Print final accuracy in terminal
# ---------------------------
final_train_acc = history.history['accuracy'][-1]
final_val_acc = history.history['val_accuracy'][-1]
print(f"\nFinal Training Accuracy: {final_train_acc:.4f}")
print(f"Final Validation Accuracy: {final_val_acc:.4f}\n")

# ---------------------------
# Export TFLite
# ---------------------------
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

tflite_path = os.path.join(MODEL_OUTPUT_DIR, MODEL_NAME)
with open(tflite_path, "wb") as f:
    f.write(tflite_model)

# Save class labels (VERY IMPORTANT)
with open(os.path.join(MODEL_OUTPUT_DIR, "class_names.json"), "w") as f:
    json.dump(CLASS_NAMES, f)

# ---------------------------
# Compute F1 Score and Accuracy for Train and Validation
# ---------------------------
def get_preds_and_labels(generator, model):
    y_true = []
    y_pred = []
    for i in range(len(generator)):
        x_batch, y_batch = generator[i]
        preds = model.predict(x_batch)
        y_true.extend(np.argmax(y_batch, axis=1))
        y_pred.extend(np.argmax(preds, axis=1))
    return np.array(y_true), np.array(y_pred)

train_true, train_pred = get_preds_and_labels(train_gen, model)
val_true, val_pred = get_preds_and_labels(val_gen, model)

train_acc = accuracy_score(train_true, train_pred)
val_acc = accuracy_score(val_true, val_pred)
train_f1 = f1_score(train_true, train_pred, average='weighted')
val_f1 = f1_score(val_true, val_pred, average='weighted')

print(f"\nFinal Training Accuracy: {final_train_acc:.4f} (sklearn: {train_acc:.4f})")
print(f"Final Validation Accuracy: {final_val_acc:.4f} (sklearn: {val_acc:.4f})")
print(f"Final Training F1 Score: {train_f1:.4f}")
print(f"Final Validation F1 Score: {val_f1:.4f}\n")
