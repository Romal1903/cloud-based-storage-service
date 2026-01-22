import api from "./axios";

export const listFilesApi = (folderId, page = 0, size = 20) =>
  api.get("/api/files", { params: { folderId, page, size }}).then(res => res.data);

export const initUploadApi = (data) =>
  api.post("/api/files/init-upload", data).then(res => res.data);

export const previewFileApi = (id) =>
  api.get(`/api/files/${id}/preview`).then(res => res.data);

export const previewSharedFileApi = (id) =>
  api.get(`/api/shares/file/${id}/preview`).then(res => res.data);

export const downloadFileApi = (id) =>
  api.get(`/api/files/${id}/download`).then(res => res.data);

export const renameFileApi = (id, name) =>
  api.put(`/api/files/${id}/rename`, { name });

export const moveFileApi = (id, folderId) =>
  api.put(`/api/files/${id}/move`, { folderId });

export const deleteFileApi = (id) =>
  api.delete(`/api/files/${id}`);

export const restoreFileApi = (id) =>
  api.put(`/api/files/recycle-bin/${id}/restore`);

export const permanentDeleteFileApi = (id) =>
  api.delete(`/api/files/recycle-bin/${id}/permanent`);

export const listSharedFolderApi = (folderId) =>
  api.get(`/api/shares/shared-folder/${folderId}`).then(res => res.data);
