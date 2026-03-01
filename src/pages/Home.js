import { useNavigate } from "react-router-dom";

function Home() {
  const navigate = useNavigate();

  return (
    <>
      <div className="navbar">
        <div className="logo">💊 MediCare Pro</div>
        <div className="nav-links">
          <span onClick={() => navigate("/login")}>Login</span>
          <span onClick={() => navigate("/register")}>Register</span>
        </div>
      </div>

      <div className="hero">
        <div className="hero-text">
          <h1>Smart Medication Management</h1>
          <p>
            Scan prescriptions, track expiry, and never miss a dose again.
          </p>

          <button
            className="primary-btn"
            onClick={() => navigate("/register")}
          >
            Get Started
          </button>

          <button
            className="secondary-btn"
            onClick={() => navigate("/login")}
          >
            Login
          </button>
        </div>
      </div>

      <div className="features">
        <h2 style={{ textAlign: "center" }}>Key Features</h2>

        <div className="features-grid">
          <div className="feature-card">
            <h3>📸 Prescription Scanner</h3>
            <p>Upload medicine images for smart processing.</p>
          </div>

          <div className="feature-card">
            <h3>⏰ Smart Reminders</h3>
            <p>Automated schedule alerts for patients.</p>
          </div>

          <div className="feature-card">
            <h3>📅 Expiry Monitoring</h3>
            <p>Track medicine expiry dates easily.</p>
          </div>
        </div>
      </div>

      <div className="footer">
        © 2026 MediCare Pro. All rights reserved.
      </div>
    </>
  );
}

export default Home;