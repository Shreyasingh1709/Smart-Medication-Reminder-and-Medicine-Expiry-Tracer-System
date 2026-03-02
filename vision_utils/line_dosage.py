import cv2
import numpy as np

def count_lines(image_path):
    img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
    if img is None:
        return {'success': False, 'count': 0, 'error': f'Image not found: {image_path}'}
    # Preprocess: blur and threshold
    blurred = cv2.GaussianBlur(img, (5, 5), 0)
    _, thresh = cv2.threshold(blurred, 128, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    # Detect lines using Hough Transform
    lines = cv2.HoughLinesP(thresh, 1, np.pi / 180, threshold=50, minLineLength=30, maxLineGap=10)
    line_count = len(lines) if lines is not None else 0
    return {'success': True, 'count': line_count, 'error': None}

def lines_to_dosage(line_count):
    if line_count == 1:
        return "1 time per day"
    elif line_count == 2:
        return "2 times per day"
    elif line_count == 3:
        return "3 times per day"
    elif line_count == 0:
        return "No dosage lines detected"
    else:
        return f"{line_count} times per day"
