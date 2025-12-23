# Smart-Medication-Reminder-and-Medicine-Expiry-Tracer-System
#Design Justification – Embossed Foil OCR
Reflective aluminium foil, which lacks ink contrast and creates specular highlights, is frequently used to emboss medicine expiration details. Because of this, even with contemporary vision systems, accurate OCR is a known open problem.
In order to overcome this, our system employs a specific preprocessing pipeline for metallic surfaces that consists of the following: Contrast Limited Adaptive Histogram Equalisation (CLAHE) to improve local contrast and normalise illumination,
Sobel filters are used for edge enhancement to highlight embossed text. Adaptive thresholding is used to binarize the image for improved text separation, while denoising is used to reduce background noise.
We calculate confidence scores at both the OCR and field level (expiry date) rather than relying solely on OCR output.
The system automatically asks for manual user verification if the confidence drops below a safe threshold. This hybrid human-in-the-loop method avoids inaccurate expiry detection and puts patient safety ahead of automation accuracy.
The system is dependable for use in actual healthcare settings because the design purposefully avoids hallucinations and guesswork.
