function Result() {
  return (
    <div style={{ textAlign: "center", marginTop: "50px" }}>
      <h2>✅ Medicine Analysis Result</h2>

      <div style={{ marginTop: "20px" }}>
        <p><strong>Medicine Name:</strong> Paracetamol</p>
        <p><strong>Expiry Date:</strong> 20 March 2026</p>
        <p><strong>Dosage Time:</strong></p>
        <ul style={{ listStyle: "none" }}>
          <li>🕗 8:00 AM</li>
          <li>🌙 9:00 PM</li>
        </ul>
      </div>

      <p style={{ marginTop: "30px", color: "green" }}>
        ✔️ Reminder will be sent before expiry
      </p>
    </div>
  );
}

export default Result;

