import { useState } from "react";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { createShortUrl } from "../api/shortUrlApi";
import { Link } from "react-router-dom";

function HomePage() {
  const [originalUrl, setOriginalUrl] = useState("");
  const [expiry, setExpiry] = useState<Date | null>(null);
  const [shortUrl, setShortUrl] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setShortUrl("");
    setLoading(true);

    try {
      const data = await createShortUrl({
        originalUrl,
        expiryDate: expiry ? expiry.toISOString() : null,
      });

      setShortUrl(data.shortUrl);
    } catch {
      setError("Failed to create short URL");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: 40, textAlign: "center", fontFamily: "Arial" }}>
      <h1>URL Shortener</h1>

      <form
        onSubmit={handleSubmit}
        style={{
          display: "flex",
          flexDirection: "column",
          gap: 12,
          width: 320,
          margin: "auto",
        }}
      >
        <input
          type="text"
          placeholder="Enter original URL"
          value={originalUrl}
          onChange={(e) => setOriginalUrl(e.target.value)}
          required
          style={{
            padding: 8,
            width: "100%",
            boxSizing: "border-box",
          }}
        />

        <DatePicker
          selected={expiry}
          onChange={setExpiry}
          showTimeSelect
          timeFormat="HH:mm"
          timeIntervals={15}
          dateFormat="yyyy-MM-dd HH:mm"
          placeholderText="Select expiry date (optional)"
          minDate={new Date()}
          isClearable
          wrapperClassName="date-wrapper"
          customInput={
            <input
              style={{
                padding: 8,
                width: "100%",
                boxSizing: "border-box",
              }}
            />
          }
        />

        <button
          type="submit"
          disabled={loading}
          style={{
            padding: 8,
            width: "100%",
            cursor: "pointer",
          }}
        >
          {loading ? "Creating..." : "Shorten"}
        </button>
      </form>

      {shortUrl && (
        <div style={{ marginTop: 20 }}>
          <a href={shortUrl} target="_blank" rel="noopener noreferrer">
            {shortUrl}
          </a>

          <div style={{ marginTop: 8 }}>
            <Link to={`/stats/${shortUrl.split("/").pop()}`}>
              View Stats
            </Link>
          </div>
        </div>
      )}

      {error && (
        <p style={{ color: "red", marginTop: 10 }}>
          {error}
        </p>
      )}
    </div>
  );
}

export default HomePage;