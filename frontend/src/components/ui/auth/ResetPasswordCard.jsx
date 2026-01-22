import { useState } from "react";
import { resetPasswordApi } from "@/api/auth";
import { useSearchParams, useNavigate } from "react-router-dom";

export default function ResetPasswordCard() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const token = params.get("token");

  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMsg("");

    if (!token) {
      setError("Invalid or missing reset token");
      return;
    }

    if (password !== confirm) {
      setError("Passwords do not match");
      return;
    }

    setLoading(true);
    try {
      await resetPasswordApi(token, password);
      setMsg("Password updated successfully. Redirecting to login...");
      setTimeout(() => navigate("/"), 2000);
    } catch {
      setError("Reset link is invalid or expired");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <h1 className="text-2xl font-semibold text-center mb-1">
        Set New Password
      </h1>
      <p className="text-sm text-slate-400 text-center mb-6">
        Enter your new password
      </p>

      <form className="space-y-4" onSubmit={handleSubmit}>
        <input
          type="password"
          placeholder="New password"
          className="auth-input"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <input
          type="password"
          placeholder="Confirm password"
          className="auth-input"
          value={confirm}
          onChange={(e) => setConfirm(e.target.value)}
          required
        />

        {error && (
          <p className="text-red-400 text-sm text-center">{error}</p>
        )}
        {msg && (
          <p className="text-green-400 text-sm text-center">{msg}</p>
        )}

        <button
          disabled={loading}
          className="w-full bg-indigo-600 hover:bg-indigo-500 rounded-lg py-2 disabled:opacity-60"
        >
          {loading ? "Updating..." : "Reset Password"}
        </button>
      </form>
    </>
  );
}
