function CaregiverDashboard() {
  return (
    <div className="container">
      <div className="card">
        <h2>🩺 Caregiver Dashboard</h2>

        <div className="dashboard-grid">
          <div className="dashboard-item">
            👤 View Patient Details
          </div>

          <div className="dashboard-item">
            ⏰ Manage Reminders
          </div>

          <div className="dashboard-item">
            📦 Track Expiry
          </div>
        </div>
      </div>
    </div>
  );
}

export default CaregiverDashboard;
