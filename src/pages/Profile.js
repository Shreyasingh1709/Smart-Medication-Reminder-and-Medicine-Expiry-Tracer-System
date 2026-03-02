import { useNavigate } from "react-router-dom";

function Profile() {
  const navigate = useNavigate();

  return (
    <div style={{ padding: "20px", maxWidth: "500px", margin: "auto" }}>
      <h1>👤 Profile & Settings</h1>

      <div
        style={{
          border: "1px solid #ccc",
          borderRadius: "10px",
          padding: "20px",
          marginTop: "20px"
        }}
      >
        <h3>User Information</h3>
        <p><strong>Name:</strong> Demo User</p>
        <p><strong>Email:</strong> demo@email.com</p>
        <p><strong>Role:</strong> Patient</p>
      </div>

      <div
        style={{
          border: "1px solid #ccc",
          borderRadius: "10px",
          padding: "20px",
          marginTop: "20px"
        }}
      >
        <h3>Settings</h3>
        <label>
          <input type="checkbox" defaultChecked /> Enable Notifications
        </label>
      </div>

      <div style={{ textAlign: "center", marginTop: "30px" }}>
        <button
          onClick={() => navigate("/")}
          style={{
            padding: "12px 20px",
            fontSize: "16px",
            borderRadius: "6px",
            cursor: "pointer"
          }}
        >
          🚪 Logout
        </button>
      </div>
    </div>
  );
}

export default Profile;
