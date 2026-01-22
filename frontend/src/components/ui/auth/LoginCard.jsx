import { Eye, EyeOff } from "lucide-react";
import { useState } from "react";
import { loginApi } from "@/api/auth";

export default function LoginCard() {
  const [show, setShow] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const res = await loginApi({ email, password });
      localStorage.setItem("token", res.token);
      window.location.href = "/dashboard";

    } catch (err) {
      setError(
        err.response?.data?.message || "Invalid credentials"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <h1 className="text-2xl font-semibold text-center mb-1">
        Welcome Back
      </h1>
      <p className="text-sm text-slate-400 text-center mb-6">
        Sign in to continue
      </p>

      <form className="space-y-4" onSubmit={handleLogin}>
        <input
          type="email"
          placeholder="Email"
          className="auth-input"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <div className="relative">
          <input
            type={show ? "text" : "password"}
            placeholder="Password"
            className="auth-input pr-10"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <button
            type="button"
            onClick={() => setShow(!show)}
            className="absolute right-3 top-2.5 text-slate-400 hover:text-white"
          >
            {show ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        </div>

        {error && (
          <p className="text-red-400 text-sm text-center">
            {error}
          </p>
        )}

        <button
          disabled={loading}
          className="w-full bg-indigo-600 hover:bg-indigo-500 rounded-lg py-2 font-medium disabled:opacity-60"
        >
          {loading ? "Signing in..." : "Login"}
        </button>
      </form>

      <div className="flex justify-between text-sm mt-4 text-slate-400">
        <a href="/forgot" className="hover:text-indigo-400">
          Forgot password?
        </a>
        <a href="/register" className="hover:text-indigo-400">
          Create account
        </a>
      </div>
    </>
  );
}
