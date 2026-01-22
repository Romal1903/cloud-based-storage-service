import { useEffect, useState } from "react";
import DashboardLayout from "@/components/dashboard/DashboardLayout";
import { listStarredApi } from "@/api/starred";
import { previewFileApi } from "@/api/files";
import FileGrid from "@/components/dashboard/FileGrid";
import FilePreviewModal from "@/components/dashboard/FilePreviewModal";

export default function Starred() {
  const [files, setFiles] = useState([]);
  const [previewFile, setPreviewFile] = useState(null);

  const loadStarred = async () => {
    const res = await listStarredApi();
    setFiles(res.map(f => ({ ...f, starred: true })));
  };

  useEffect(() => {
    loadStarred();
  }, []);

  const openFile = async (file) => {
    try {
      const data = await previewFileApi(file.id);
      setPreviewFile({
        url: data.previewUrl,
        name: file.name,
        type: file.contentType,
      });
    } catch {
      alert("Failed to preview file");
    }
  };

  return (
    <DashboardLayout>
      <h1 className="text-2xl mb-6">Starred</h1>

      <FileGrid
        files={files}
        folders={[]}
        onFileOpen={openFile}
        onRefresh={loadStarred}
      />

      {previewFile && (
        <FilePreviewModal
          file={previewFile}
          onClose={() => setPreviewFile(null)}
        />
      )}
    </DashboardLayout>
  );
}
