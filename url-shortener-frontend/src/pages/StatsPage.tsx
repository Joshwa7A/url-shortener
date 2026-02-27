import { useParams, Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { getStats } from "../api/shortUrlApi";

interface ShortUrlStats {
  originalUrl: string;
  clickCount: number;
  createdAt: string;
}

function StatsPage() {
  const { shortCode } = useParams();
  const [stats, setStats] = useState<ShortUrlStats | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await getStats(shortCode!);
        setStats(data);
      } catch {
        setError("Failed to load stats");
      }
    };

    fetchStats();
  }, [shortCode]);

  return (
    <div style={{ padding: "2rem", fontFamily: "Arial" }}>
      <h1>URL Stats</h1>

      {stats && (
        <div>
          <p><strong>Original URL:</strong> {stats.originalUrl}</p>
          <p><strong>Click Count:</strong> {stats.clickCount}</p>
          <p><strong>Created At:</strong> {stats.createdAt}</p>
        </div>
      )}

      {error && <p style={{ color: "red" }}>{error}</p>}

      <Link to="/">Go Back</Link>
    </div>
  );
}

export default StatsPage;