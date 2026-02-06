"""
NLP Module for Medicine Text Processing
Tokenization, embeddings, and simple extraction.
"""
import spacy
from typing import List, Dict

# Load spaCy model (small, fast)
nlp = spacy.load('en_core_web_sm')

def tokenize_text(text: str) -> List[str]:
    """Tokenize input text into words."""
    doc = nlp(text)
    return [token.text for token in doc]

import re

def extract_medicine_info(text: str) -> Dict[str, str]:
    """Extract medicine name, dosage, and expiry from text using regex and NLP."""
    doc = nlp(text)
    # Medicine name: first capitalized word or sequence
    name = ''
    name_match = re.search(r'([A-Z][a-zA-Z0-9]+(?: [A-Z][a-zA-Z0-9]+)*)', text)
    if name_match:
        name = name_match.group(1)
    # Dosage: e.g., 500mg, 10 ml, twice a day, once daily
    dosage = ''
    dosage_match = re.search(r'(\d+\s?(mg|ml|MCG|MG|ML))', text)
    if dosage_match:
        dosage = dosage_match.group(0)
    else:
        freq_match = re.search(r'(once|twice|thrice|\d+ times) (a|per) day', text, re.IGNORECASE)
        if freq_match:
            dosage = freq_match.group(0)
    # Expiry: Expiry: 12/2026, Exp: 01-02-2027, Expires 2027-01-02
    expiry = ''
    expiry_match = re.search(r'(Exp(?:iry)?[:\s]*[0-9]{1,2}[/-][0-9]{2,4})', text, re.IGNORECASE)
    if not expiry_match:
        expiry_match = re.search(r'(Expires?[:\s]*[0-9]{1,2}[/-][0-9]{2,4})', text, re.IGNORECASE)
    if not expiry_match:
        expiry_match = re.search(r'(Exp(?:iry)?[:\s]*[0-9]{4}-[0-9]{2}-[0-9]{2})', text, re.IGNORECASE)
    if expiry_match:
        expiry = expiry_match.group(0)
    return {'name': name, 'dosage': dosage, 'expiry': expiry}

if __name__ == '__main__':
    sample_text = "Paracetamol 500mg, take twice a day. Expiry: 12/2026"
    print("Tokens:", tokenize_text(sample_text))
    print("Extracted Info:", extract_medicine_info(sample_text))
