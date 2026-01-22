import api from "./axios";

export const searchAllApi = ({ q, folderId, page, size }) =>
    api.get("/api/search/all", {
        params: { q, folderId, page, size }
    }).then(res => res.data);
