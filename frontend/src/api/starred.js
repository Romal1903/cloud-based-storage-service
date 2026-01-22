import api from "./axios";

export const listStarredApi = () =>
  api.get("/api/starred").then(res => res.data);

export const starFileApi = (fileId) =>
  api.post(`/api/starred/${fileId}`);

export const unstarFileApi = (fileId) =>
  api.delete(`/api/starred/${fileId}`);
