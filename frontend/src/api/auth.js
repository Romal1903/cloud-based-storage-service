import api from "./axios";

export const loginApi = (data) =>
  api.post("/api/auth/login", data).then(res => res.data);

export const registerApi = (data) =>
  api.post("/api/auth/register", data).then(res => res.data);

export const forgotPasswordApi = (email) =>
  api.post("/api/auth/forgot-password", { email });

export const resetPasswordApi = (token, newPassword) =>
  api.post("/api/auth/reset-password", { token, newPassword });

export const meApi = () =>
  api.get("/api/auth/me").then(res => res.data);
