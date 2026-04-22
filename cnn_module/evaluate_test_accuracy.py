import os
import numpy as np
from cnn_module.predict_cnn import predict_medicine_from_image, CLASS_NAMES
from sklearn.metrics import accuracy_score, confusion_matrix, classification_report
import matplotlib.pyplot as plt

# Set this to your test images directory, with subfolders for each class
TEST_DIR = "test_images/"

def get_test_data(test_dir):
    image_paths = []
    labels = []
    for class_name in CLASS_NAMES:
        class_dir = os.path.join(test_dir, class_name)
        if not os.path.isdir(class_dir):
            continue
        for fname in os.listdir(class_dir):
            if fname.lower().endswith(('.jpg', '.jpeg', '.png')):
                image_paths.append(os.path.join(class_dir, fname))
                labels.append(class_name)
    return image_paths, labels

def evaluate_model():
    image_paths, true_labels = get_test_data(TEST_DIR)
    print("CLASS_NAMES:", CLASS_NAMES)
    print("Number of test images found:", len(image_paths))
    print("Test image paths:", image_paths)
    pred_labels = []
    for img_path in image_paths:
        result = predict_medicine_from_image(img_path)
        pred_labels.append(result['prediction'] if result['success'] else None)
    
    # Filter out failed predictions
    valid_idx = [i for i, p in enumerate(pred_labels) if p is not None]
    y_true = [true_labels[i] for i in valid_idx]
    y_pred = [pred_labels[i] for i in valid_idx]
    
    acc = accuracy_score(y_true, y_pred)
    print(f"Test Accuracy: {acc:.4f}")
    print("\nClassification Report:")
    from sklearn.preprocessing import LabelEncoder
    le = LabelEncoder()
    le.fit(CLASS_NAMES)
    y_true_enc = le.transform(y_true) if y_true else []
    y_pred_enc = le.transform(y_pred) if y_pred else []
    print(classification_report(
        y_true_enc, y_pred_enc,
        labels=range(len(CLASS_NAMES)),
        target_names=CLASS_NAMES,
        zero_division=0
    ))

    # Save test accuracy for plotting
    with open("cnn_module/test_metrics.json", "w") as f:
        import json
        json.dump({"test_accuracy": acc}, f)

    # Plot confusion matrix
    from sklearn.metrics import ConfusionMatrixDisplay
    cm = confusion_matrix(y_true_enc, y_pred_enc, labels=range(len(CLASS_NAMES)))
    disp = ConfusionMatrixDisplay(confusion_matrix=cm, display_labels=CLASS_NAMES)
    disp.plot(cmap='Blues')
    plt.title('Confusion Matrix')
    plt.tight_layout()
    plt.savefig('test_confusion_matrix.png')
    plt.show()

if __name__ == "__main__":
    evaluate_model()
