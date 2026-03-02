function PatientDashboard() {
  return (
    <div className="dashboard-grid">
      <div className="dashboard-card">
        <h3>💊 Active Medicines</h3>
        <p>3 Medicines</p>
      </div>

      <div className="dashboard-card">
        <h3>⏰ Upcoming Reminders</h3>
        <p>2 Today</p>
      </div>

      <div className="dashboard-card">
        <h3>📅 Expiring Soon</h3>
        <p>1 Medicine</p>
      </div>
    </div>
  );
}

export default PatientDashboard;