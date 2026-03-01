import { useNavigate } from "react-router-dom";
import { useState } from "react";

function Register() {
  const navigate = useNavigate();
  const [name, setName] = useState("");

  return (
    <div className="container">
      <div className="card">
        <h2>📝 Register</h2>

        <input
          type="text"
          placeholder="Full Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />

        <input type="email" placeholder="Email" />
        <input type="password" placeholder="Password" />

        <button onClick={() => navigate("/login")}>
          Create Account
        </button>
      </div>
    </div>
  );
}

export default Register;
