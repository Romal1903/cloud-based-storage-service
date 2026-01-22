import { useEffect, useState } from "react";
import DashboardLayout from "@/components/dashboard/DashboardLayout";
import { initUploadApi } from "@/api/files";
import { listFoldersApi, createFolderApi } from "@/api/folders";

export default function Upload() {
  const [file, setFile] = useState(null);
  const [folders, setFolders] = useState([]);
  const [folderId, setFolderId] = useState("");
  const [newFolder, setNewFolder] = useState("");
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    listFoldersApi().then(setFolders);
  }, []);

  const upload = async () => {
    if (!file) return alert("Select a file");

    let targetFolderId = folderId || null;

    if (newFolder.trim()) {
      const folder = await createFolderApi({ name: newFolder });
      targetFolderId = folder.id;
    }

    setUploading(true);

    const { uploadUrl, headers } = await initUploadApi({
      name: file.name,
      size: file.size,
      contentType: file.type,
      folderId: targetFolderId,
    });

    await fetch(uploadUrl, {
      method: "PUT",
      body: file,
      headers
    });

    window.location.href = "/dashboard";
  };

  return (
    <DashboardLayout>
      <div className="max-w-xl mx-auto space-y-6">
        <h1 className="text-2xl font-semibold">Upload File</h1>

        <input
          type="file"
          onChange={e => setFile(e.target.files[0])}
        />

        <select
          value={folderId}
          onChange={e => setFolderId(e.target.value)}
          className="w-full bg-slate-900 border border-slate-800 p-2 rounded"
        >
          <option value="">Root Folder</option>
          {folders.map(f => (
            <option key={f.id} value={f.id}>{f.name}</option>
          ))}
        </select>

        <input
          placeholder="Or create new folder"
          value={newFolder}
          onChange={e => setNewFolder(e.target.value)}
          className="w-full bg-slate-900 border border-slate-800 p-2 rounded"
        />

        <button
          onClick={upload}
          disabled={uploading}
          className="px-4 py-2 bg-indigo-600 rounded hover:bg-indigo-700"
        >
          {uploading ? "Uploading..." : "Upload"}
        </button>
      </div>
    </DashboardLayout>
  );
}
