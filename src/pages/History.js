function History() {
  const cardStyle = {
    border: "1px solid #ccc",
    borderRadius: "10px",
    padding: "15px",
    marginBottom: "15px"
  };

  return (
    <div style={{ padding: "20px", maxWidth: "600px", margin: "auto" }}>
      <h1>📊 Medicine History</h1>

      <div style={cardStyle}>
        <h3>01 Feb 2026</h3>
        <p>Paracetamol – ✔ Taken</p>
        <p>Vitamin D – ✔ Taken</p>
      </div>

      <div style={cardStyle}>
        <h3>31 Jan 2026</h3>
        <p>BP Tablet – ❌ Missed</p>
        <p>Insulin – ✔ Taken</p>
      </div>
    </div>
  );
}

export default History;
