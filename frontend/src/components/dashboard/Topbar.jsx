import { User } from "lucide-react";
import { useEffect, useState } from "react";
import { meApi } from "@/api/auth";

export default function Topbar() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    meApi()
      .then(setUser)
      .catch((err) => {
      if (err?.response?.status === 401) {
        localStorage.removeItem("token");
        window.location.href = "/";
      }
    });
  }, []);

  return (
    <header className="h-16 border-b border-slate-800 bg-slate-900/80 backdrop-blur flex items-center justify-between px-6">
      <h2 className="text-lg font-medium">
        Dashboard
      </h2>

      <div className="flex items-center gap-3 text-slate-300">
        <User size={18} />
        <span className="text-sm">
          {user ? user.name : "Loading..."}
        </span>
      </div>
    </header>
  );
}
