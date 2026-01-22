import { Folder, Upload, Share2, Star, Trash2, LogOut } from "lucide-react";
import { NavLink, useNavigate } from "react-router-dom";

export default function Sidebar() {
  const navigate = useNavigate();

  const logout = () => {
    const ok = window.confirm("Are you sure you want to logout?");
    if (!ok) return;

    localStorage.removeItem("token");
    navigate("/");
  };

  const linkClass =
    "flex items-center gap-3 px-4 py-2 rounded-lg text-slate-400 hover:bg-slate-800 hover:text-white transition";

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 p-4 flex flex-col">
      <h1 className="text-xl font-semibold mb-8 text-indigo-400">
        Cloud Storage
      </h1>

      <nav className="space-y-2 flex-1">
        <NavLink to="/dashboard" className={linkClass}>
          <Folder size={18} />
          My Files
        </NavLink>

        <NavLink to="/upload" className={linkClass}>
          <Upload size={18} />
          Upload
        </NavLink>

        <NavLink to="/starred" className={linkClass}>
          <Star size={18} />
          Starred
        </NavLink>

        <NavLink to="/shared" className={linkClass}>
          <Share2 size={18} />
          Shared
        </NavLink>

        <NavLink to="/recycle-bin" className={linkClass}>
          <Trash2 size={18} />
          Recycle Bin
        </NavLink>
      </nav>

      <button
        onClick={logout}
        className="flex items-center gap-3 px-4 py-2 rounded-lg text-red-400 hover:bg-red-500/10 transition"
      >
        <LogOut size={18} />
        Logout
      </button>
    </aside>
  );
}
