import api from "./axios";

export const shareWithUserApi = (type, resourceId, data) =>
  api.post(`/api/shares/${type}/${resourceId}`, data).then(res => res.data);

export const createPublicShareApi = (data) =>
  api.post("/api/shares", data).then(res => res.data);

export const listSharedWithMeApi = () =>
  api.get("/api/shares/query/shared-with-me").then(res => res.data);

export const listMySharesApi = () =>
  api.get("/api/shares/mine").then(res => res.data);

export const revokeShareApi = (shareId) =>
  api.delete(`/api/shares/${shareId}`);

export const updateSharePermissionApi = (shareId, data) =>
  api.put(`/api/shares/${shareId}`, data);

export const accessPublicShareApi = (token) =>
  api.get(`/api/public/share/${token}`).then(res => res.data);

export const listSharesForResourceApi = (type, resourceId) =>
  api.get(`/api/shares/${type}/${resourceId}`).then(res => res.data);

export const listSharedFolderApi = (folderId) =>
  api.get(`/api/shares/shared-folder/${folderId}`).then(res => res.data);
