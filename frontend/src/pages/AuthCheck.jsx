import { useEffect, useState } from "react";
import { meApi } from "@/api/auth";

export default function AuthCheck({ children }) {
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    meApi()
      .then(() => setLoading(false))
      .catch((err) => {
        if (err?.response?.status === 401) {
          localStorage.removeItem("token");
          window.location.href = "/";
        } else {
          setLoading(false);
        }
      });
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 text-white flex items-center justify-center">
        Loading...
      </div>
    );
  }

  return children;
}
