"""
SQLite Database Module for Smart Medication Reminder
Stores medicine metadata and reminders.
"""
import sqlite3
from datetime import datetime

DB_PATH = 'medicine_reminder.db'

# Database initialization

def init_db():
    """Create tables if they don't exist."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS medicine (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            image_path TEXT,
            medicine_name TEXT,
            expiry_date TEXT,
            dosage_info TEXT,
            detected_time TEXT
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS reminder (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            medicine_id INTEGER,
            reminder_time TEXT,
            frequency TEXT,
            status TEXT,
            FOREIGN KEY(medicine_id) REFERENCES medicine(id)
        )
    ''')
    conn.commit()
    conn.close()

# Insert medicine metadata

def add_medicine(image_path, medicine_name, expiry_date, dosage_info):
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    detected_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    cursor.execute('''
        INSERT INTO medicine (image_path, medicine_name, expiry_date, dosage_info, detected_time)
        VALUES (?, ?, ?, ?, ?)
    ''', (image_path, medicine_name, expiry_date, dosage_info, detected_time))
    medicine_id = cursor.lastrowid
    conn.commit()
    conn.close()
    return medicine_id

# Insert reminder

def add_reminder(medicine_id, reminder_time, frequency, status='pending'):
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute('''
        INSERT INTO reminder (medicine_id, reminder_time, frequency, status)
        VALUES (?, ?, ?, ?)
    ''', (medicine_id, reminder_time, frequency, status))
    conn.commit()
    conn.close()

# Get reminders due now

def get_due_reminders(current_time):
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute('''
        SELECT reminder.id, medicine.medicine_name, medicine.image_path, reminder.reminder_time, reminder.frequency, reminder.status
        FROM reminder
        JOIN medicine ON reminder.medicine_id = medicine.id
        WHERE reminder.reminder_time <= ? AND reminder.status = 'pending'
    ''', (current_time,))
    reminders = cursor.fetchall()
    conn.close()
    return reminders

# Example usage
if __name__ == '__main__':
    init_db()
    # Add a medicine and reminder
    med_id = add_medicine('images/paracetamol.jpg', 'Paracetamol', '2026-12-01', 'Twice a day')
    add_reminder(med_id, '2026-02-07 09:00:00', 'daily')
    # Get reminders due now
    now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    due = get_due_reminders(now)
    for r in due:
        print(f"Reminder: Take {r[1]} (see image: {r[2]}) at {r[3]} [{r[4]}]")
