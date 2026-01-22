import { useState } from "react";
import { forgotPasswordApi } from "@/api/auth";

export default function ForgotPasswordCard() {
  const [email, setEmail] = useState("");
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMsg("");
    setError("");

    try {
      await forgotPasswordApi(email);
      setMsg("Reset link sent to your email");
    } catch {
      setError("Failed to send reset link");
    }
  };

  return (
    <>
      <h1 className="text-2xl font-semibold text-center mb-1">
        Reset Password
      </h1>
      <p className="text-sm text-slate-400 text-center mb-6">
        Enter your email
      </p>

      <form className="space-y-4" onSubmit={handleSubmit}>
        <input
          type="email"
          placeholder="Email"
          className="auth-input"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        {msg && <p className="text-green-400 text-sm text-center">{msg}</p>}
        {error && <p className="text-red-400 text-sm text-center">{error}</p>}

        <button className="w-full bg-indigo-600 hover:bg-indigo-500 rounded-lg py-2">
          Send reset link
        </button>
      </form>

      <p className="text-sm text-center mt-4">
        <a href="/" className="text-indigo-400 hover:underline">
          Back to login
        </a>
      </p>
    </>
  );
}
