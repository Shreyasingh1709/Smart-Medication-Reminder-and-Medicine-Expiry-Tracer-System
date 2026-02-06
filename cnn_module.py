"""
CNN Module for Medicine Image Classification
Uses MobileNetV2 for lightweight, mobile-friendly inference.
"""
import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D
from tensorflow.keras.models import Model

# Path to dataset
DATASET_PATH = 'medicine_dataset/'
IMG_SIZE = (128, 128)  # Small input size for mobile
BATCH_SIZE = 32
NUM_CLASSES = 3  # Adjust based on your dataset

# Data generators
train_datagen = ImageDataGenerator(
    rescale=1./255,
    validation_split=0.2,
    horizontal_flip=True,
    rotation_range=20
)
train_gen = train_datagen.flow_from_directory(
    DATASET_PATH,
    target_size=IMG_SIZE,
    batch_size=BATCH_SIZE,
    class_mode='categorical',
    subset='training'
)
val_gen = train_datagen.flow_from_directory(
    DATASET_PATH,
    target_size=IMG_SIZE,
    batch_size=BATCH_SIZE,
    class_mode='categorical',
    subset='validation'
)

# MobileNetV2 base
base_model = MobileNetV2(weights='imagenet', include_top=False, input_shape=IMG_SIZE + (3,))
base_model.trainable = False  # Transfer learning

# Custom head
x = base_model.output
x = GlobalAveragePooling2D()(x)
x = Dense(64, activation='relu')(x)
predictions = Dense(NUM_CLASSES, activation='softmax')(x)
model = Model(inputs=base_model.input, outputs=predictions)

model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

# Training function
def train_model(epochs=10):
    """Train the MobileNetV2 model."""
    history = model.fit(
        train_gen,
        validation_data=val_gen,
        epochs=epochs
    )
    return history

# Export for mobile deployment (.tflite)
def export_tflite(filename='mobilenetv2_medicine.tflite'):
    """Export trained model to TensorFlow Lite format."""
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()
    with open(filename, 'wb') as f:
        f.write(tflite_model)
    print(f"Model exported to {filename}")

if __name__ == '__main__':
    # Example usage
    train_model(epochs=10)
    export_tflite()
