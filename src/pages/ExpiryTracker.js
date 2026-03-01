import { useRef, useState } from "react";

function ExpiryTracker() {
  const fileRef = useRef(null);
  const [image, setImage] = useState(null);

  return (
    <div>
      <div className="card">
        <h2>Upload Medicine</h2>

        <button onClick={() => fileRef.current.click()}>
          Open Camera
        </button>

        <input
          type="file"
          accept="image/*"
          capture="environment"
          ref={fileRef}
          style={{ display: "none" }}
          onChange={(e) => setImage(e.target.files[0])}
        />

        {image && <p>{image.name}</p>}
      </div>
    </div>
  );
}

export default ExpiryTracker;
