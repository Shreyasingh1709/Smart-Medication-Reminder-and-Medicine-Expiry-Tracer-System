function Message({ type, text }) {
  if (!text) return null;

  const styles = {
    success: {
      backgroundColor: "#d4edda",
      color: "#155724",
      border: "1px solid #c3e6cb"
    },
    error: {
      backgroundColor: "#f8d7da",
      color: "#721c24",
      border: "1px solid #f5c6cb"
    }
  };

  return (
    <div
      style={{
        padding: "10px",
        marginBottom: "15px",
        borderRadius: "5px",
        ...styles[type]
      }}
    >
      {text}
    </div>
  );
}

export default Message;
