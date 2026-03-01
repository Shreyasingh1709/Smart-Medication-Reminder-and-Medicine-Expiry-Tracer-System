import { useNavigate } from "react-router-dom";
import { useState } from "react";

function Login() {
  const navigate = useNavigate();
  const [role, setRole] = useState("patient");

  const handleLogin = () => {
    role === "patient"
      ? navigate("/patient-dashboard")
      : navigate("/caregiver-dashboard");
  };

  return (
    <div className="auth-container">
      <h2>Login</h2>

      <select
        value={role}
        onChange={(e) => setRole(e.target.value)}
      >
        <option value="patient">Patient</option>
        <option value="caregiver">Caregiver</option>
      </select>

      <input placeholder="Email" />
      <input type="password" placeholder="Password" />

      <button onClick={handleLogin}>Login</button>
    </div>
  );
}

export default Login;