import { useNavigate } from "react-router-dom";

function RoleSelect() {
  const navigate = useNavigate();

  return (
    <div className="container">
      <div className="card">
        <h2>💊 Smart Medication</h2>
        <p>Select your role</p>

        <button onClick={() => navigate("/login", { state: { role: "patient" } })}>
          👤 Patient
        </button>

        <button
          className="secondary"
          onClick={() => navigate("/login", { state: { role: "caregiver" } })}
        >
          🩺 Caregiver
        </button>
      </div>
    </div>
  );
}

export default RoleSelect;
