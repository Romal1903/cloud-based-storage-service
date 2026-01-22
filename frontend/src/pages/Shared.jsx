import { useEffect, useState } from "react";
import DashboardLayout from "@/components/dashboard/DashboardLayout";
import FileGrid from "@/components/dashboard/FileGrid";
import { listSharedWithMeApi, listMySharesApi } from "@/api/share";
import ShareManagerModal from "@/components/share/ShareManagerModal";
import { Navigate, useNavigate } from "react-router-dom";
import FilePreviewModal from "@/components/dashboard/FilePreviewModal"
import { previewSharedFileApi } from "@/api/files";

export default function Shared() {
  const [files, setFiles] = useState([]);
  const [folders, setFolders] = useState([]);
  const [myShares, setMyShares] = useState([]);
  const [selected, setSelected] = useState(null);
  const navigate = useNavigate();
  const [preview, setPreview] = useState(null);

  useEffect(() => {
    listSharedWithMeApi().then(res => {
      setFiles(res.files || []);
      setFolders(res.folders || []);
    });

    listMySharesApi().then(setMyShares);
  }, []);

  const loadShares = async () => {
    const res = await listSharedWithMeApi();

    setFiles(res.files || []);
    setFolders(res.folders || []);

  };

  useEffect(() => {
    loadShares();
  }, []);

  const openFolder = (folder) => {
    navigate(`/shared/folder/${folder.id}`);
  };

  const openFile = async (file) => {
    const data = await previewSharedFileApi(file.id);
    setPreview({
      url: data.previewUrl,
      name: file.name,
      type: file.contentType
    });
  };

  return (
    <DashboardLayout>
      <h1 className="text-2xl mb-6">Shared</h1>

      <h2 className="text-lg mb-3">Shared by me</h2>

      <div className="space-y-2 mb-10">
        {myShares.length === 0 && (
          <p className="text-slate-400 text-sm">
            You haven’t shared anything
          </p>
        )}

        {myShares.map(s => (
          <div
            key={s.id}
            className="flex justify-between items-center bg-slate-900 border border-slate-800 rounded-lg px-4 py-3"
          >
            <div className="text-sm">
              <p className="font-medium">{s.name}</p>
              <p className="text-slate-400 text-xs">
                {s.email || "Public link"} · {s.permission}
                {s.expiresAt && ` · Expires ${new Date(s.expiresAt).toLocaleString()}`}
              </p>
            </div>

            <button
              onClick={() => setSelected(s)}
              className="text-indigo-400 text-sm"
            >
              Manage
            </button>
          </div>
        ))}
      </div>

      <h2 className="text-lg mb-3">Shared with me</h2>

      <FileGrid
        files={files}
        folders={folders}
        onFolderOpen={openFolder}
        onFileOpen={openFile}
      />

      {selected && (
        <ShareManagerModal
          resource={selected}
          type={selected.resourceType}
          onClose={() => setSelected(null)}
          onUpdated={loadShares}
        />
      )}

      {preview && (
        <FilePreviewModal
          file={preview}
          onClose={() => setPreview(null)}
        />
      )}

    </DashboardLayout>
  );
}
