import api from "./axios";

export const getStorageUsageApi = () =>
  api.get("/api/storage/usage").then(res => res.data);
