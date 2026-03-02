import { useNavigate } from "react-router-dom";
import Logo from "./Logo";

function Navbar() {
  const navigate = useNavigate();

  return (
    <div
      style={{
        width: "100%",
        padding: "15px 40px",
        background: "white",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        boxShadow: "0 2px 10px rgba(0,0,0,0.1)",
      }}
    >
      <Logo />

      <div style={{ display: "flex", gap: "20px" }}>
        <span style={{ cursor: "pointer" }} onClick={() => navigate("/")}>
          Home
        </span>

        <span
          style={{ cursor: "pointer" }}
          onClick={() => navigate("/patient-dashboard")}
        >
          Dashboard
        </span>

        <span
          style={{ cursor: "pointer" }}
          onClick={() => navigate("/help")}
        >
          Help
        </span>
      </div>
    </div>
  );
}

export default Navbar;