# Smart-Medication-Reminder-and-Medicine-Expiry-Tracer-System

## Project Modules (in order)

1. [Module 1 Ideation.docx](Module%201%20Ideation.docx)
2. [Module 2 Description.docx](Module%202%20Description.docx)
3. [Module 3 Learning.docx](Module%203%20Learning.docx)
4. [Module 4 Basics of project.docx](Module%204%20Basics%20of%20project.docx)
5. [Module 5 Originality of the Project.docx](Module%205%20Originality%20of%20the%20Project.docx)
6. [Module 6 GUI Dashboard.mp4](Module%206%20GUI%20Dashboard.mp4)
7. [Module 7 Implemenattation](Module%207%20Implemenattation/)
8. [Module 8 Result and Conclusion](Module%208%20Result%20and%20Conclusion.docx)

## Design Justification – Embossed Foil OCR

Reflective aluminium foil, which lacks ink contrast and creates specular highlights, is frequently used to emboss medicine expiration details. Because of this, even with contemporary vision systems, accurate OCR is a known open problem.
In order to overcome this, our system employs a specific preprocessing pipeline for metallic surfaces that consists of the following: Contrast Limited Adaptive Histogram Equalisation (CLAHE) to improve local contrast and normalise illumination,
Sobel filters are used for edge enhancement to highlight embossed text. Adaptive thresholding is used to binarize the image for improved text separation, while denoising is used to reduce background noise.
We calculate confidence scores at both the OCR and field level (expiry date) rather than relying solely on OCR output.
The system automatically asks for manual user verification if the confidence drops below a safe threshold. This hybrid human-in-the-loop method avoids inaccurate expiry detection and puts patient safety ahead of automation accuracy.
The system is dependable for use in actual healthcare settings because the design purposefully avoids hallucinations and guesswork.
