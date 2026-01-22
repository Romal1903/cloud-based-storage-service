import { useState } from "react";
import { registerApi } from "@/api/auth";

export default function RegisterCard() {
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleRegister = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    try {
      await registerApi(form);
      setSuccess("Account created. Please login.");
    } catch (err) {
      setError(
        err.response?.data?.message || "Registration failed"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <h1 className="text-2xl font-semibold text-center mb-1">
        Create Account
      </h1>
      <p className="text-sm text-slate-400 text-center mb-6">
        Name, email & password
      </p>

      <form className="space-y-4" onSubmit={handleRegister}>
        <input
          placeholder="Full Name"
          className="auth-input"
          value={form.name}
          onChange={(e) =>
            setForm({ ...form, name: e.target.value })
          }
          required
        />
        <input
          type="email"
          placeholder="Email"
          className="auth-input"
          value={form.email}
          onChange={(e) =>
            setForm({ ...form, email: e.target.value })
          }
          required
        />
        <input
          type="password"
          placeholder="Password"
          className="auth-input"
          value={form.password}
          onChange={(e) =>
            setForm({ ...form, password: e.target.value })
          }
          required
        />

        {error && (
          <p className="text-red-400 text-sm text-center">
            {error}
          </p>
        )}
        {success && (
          <p className="text-green-400 text-sm text-center">
            {success}
          </p>
        )}

        <button
          disabled={loading}
          className="w-full bg-indigo-600 hover:bg-indigo-500 rounded-lg py-2 disabled:opacity-60"
        >
          {loading ? "Creating..." : "Register"}
        </button>
      </form>

      <p className="text-sm text-center mt-4 text-slate-400">
        Already have an account?{" "}
        <a href="/" className="text-indigo-400 hover:underline">
          Login
        </a>
      </p>
    </>
  );
}
