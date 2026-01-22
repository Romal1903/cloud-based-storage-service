import api from "./axios";

export const listFoldersApi = (parentId) =>
  api.get("/api/folders", {
    params: parentId ? { parentId } : {}
  }).then(res => res.data);

export const createFolderApi = (data) =>
  api.post("/api/folders", data).then(res => res.data);

export const renameFolderApi = (id, name) =>
  api.put(`/api/folders/${id}/rename`, { name });

export const moveFolderApi = (id, parentFolderId) =>
  api.put(`/api/folders/${id}/move`, { parentFolderId });

export const deleteFolderApi = (id) =>
  api.delete(`/api/folders/${id}`);

export const restoreFolderApi = (id) =>
  api.put(`/api/folders/recycle-bin/${id}/restore`);

export const permanentDeleteFolderApi = (id) =>
  api.delete(`/api/folders/recycle-bin/${id}/permanent`);
