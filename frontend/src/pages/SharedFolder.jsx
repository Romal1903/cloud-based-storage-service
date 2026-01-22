import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import DashboardLayout from "@/components/dashboard/DashboardLayout";
import FileGrid from "@/components/dashboard/FileGrid";
import { listSharedFolderApi } from "@/api/share";
import { previewSharedFileApi } from "@/api/files";
import FilePreviewModal from "@/components/dashboard/FilePreviewModal";

export default function SharedFolder() {
  const { folderId } = useParams();
  const [folders, setFolders] = useState([]);
  const [files, setFiles] = useState([]);
  const [preview, setPreview] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    listSharedFolderApi(folderId).then(res => {
      setFolders(res.folders || []);
      setFiles(res.files || []);
    });
  }, [folderId]);

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
      <div className="space-y-6">

        <div>
          <h1 className="text-2xl font-semibold text-white">
            Shared Folder
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Files shared with you
          </p>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 min-h-[300px]">
          <FileGrid
            folders={folders}
            files={files}
            onFolderOpen={(f) =>
              navigate(`/shared/folder/${f.id}`)
            }
            onFileOpen={openFile}
          />
        </div>

      </div>

      {preview && (
        <FilePreviewModal
          file={preview}
          onClose={() => setPreview(null)}
        />
      )}
    </DashboardLayout>
  );
}
