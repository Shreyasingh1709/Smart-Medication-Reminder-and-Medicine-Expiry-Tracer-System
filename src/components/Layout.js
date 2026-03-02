import { useNavigate } from "react-router-dom";

function Layout({ children }) {
  const navigate = useNavigate();

  return (
    <div className="app-layout">
      <div className="sidebar">
        <h2>💊 MediCare Pro</h2>

        <div className="sidebar-item" onClick={() => navigate("/patient-dashboard")}>
          Dashboard
        </div>

        <div className="sidebar-item" onClick={() => navigate("/expiry")}>
          Expiry Tracker
        </div>

        <div className="sidebar-item" onClick={() => navigate("/reminders")}>
          Reminders
        </div>

        <div className="sidebar-item" onClick={() => navigate("/help")}>
          Help
        </div>

        <div className="sidebar-item" onClick={() => navigate("/")}>
          Logout
        </div>
      </div>

      <div className="main-content">
        <div className="topbar">
          <h3>Welcome Back 👋</h3>
        </div>

        {children}
      </div>
    </div>
  );
}

export default Layout;