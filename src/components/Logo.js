function Logo() {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
      <div
        style={{
          background: "#2E8B57",
          width: "35px",
          height: "35px",
          borderRadius: "8px",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          color: "white",
          fontWeight: "bold",
        }}
      >
        💊
      </div>
      <h2 style={{ margin: 0, color: "#2E8B57" }}>
        MediCare Pro
      </h2>
    </div>
  );
}

export default Logo;