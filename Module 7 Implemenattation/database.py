import sqlite3

def create_tables():
    conn = sqlite3.connect('medication.db')
    c = conn.cursor()

    # Patient table
    c.execute('''CREATE TABLE IF NOT EXISTS Patient (
                    patient_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    age INTEGER,
                    contact TEXT)''')

    # Caregiver table
    c.execute('''CREATE TABLE IF NOT EXISTS Caregiver (
                    caregiver_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    contact TEXT)''')

    # Medicine table
    c.execute('''CREATE TABLE IF NOT EXISTS Medicine (
                    medicine_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    expiry_date TEXT,
                    patient_id INTEGER,
                    FOREIGN KEY(patient_id) REFERENCES Patient(patient_id))''')

    # DosageTrack table
    c.execute('''CREATE TABLE IF NOT EXISTS DosageTrack (
                    track_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    medicine_id INTEGER,
                    time_taken TEXT,
                    FOREIGN KEY(medicine_id) REFERENCES Medicine(medicine_id))''')

    conn.commit()
    conn.close()

def insert_medicine(name, expiry_date, patient_id):
    conn = sqlite3.connect('medication.db')
    c = conn.cursor()
    c.execute("INSERT INTO Medicine (name, expiry_date, patient_id) VALUES (?, ?, ?)",
              (name, expiry_date, patient_id))
    conn.commit()
    conn.close()
