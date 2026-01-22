import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { accessPublicShareApi } from "@/api/share";
import FileGrid from "@/components/dashboard/FileGrid";

const API = "http://localhost:8080";

export default function PublicShare() {
  const { token } = useParams();
  const [data, setData] = useState(null);
  const [preview, setPreview] = useState(null);

  useEffect(() => {
    const load = async () => {
      const meta = await accessPublicShareApi(token);

      if (meta.type === "FILE") {
        const res = await fetch(
          `${API}/api/files/public/files/${meta.resourceId}?token=${token}`
        );
        const d = await res.json();

        setPreview({
          url: d.previewUrl,
          name: meta.name || "File",
          type: d.contentType
        });
      }

      if (meta.type === "FOLDER") {
        const res = await fetch(
          `${API}/api/folders/public/folders/${meta.resourceId}?token=${token}`
        );
        if (!res.ok) throw new Error("Access denied");
        const d = await res.json();
        setData({ type: "FOLDER", folders: d });
      }
    };

    load().catch(console.error);
  }, [token]);

  if (!data) return <div>Loading...</div>;

  if (data.type === "FILE") {
    return (
      <iframe
        src={data.previewUrl}
        className="w-full h-screen"
        title="Preview"
      />
    );
  }

  return (
    <div className="p-6">
      <h1 className="text-xl mb-4">Shared Folder</h1>

      <FileGrid
        folders={data.folders.folders}
        files={data.folders.files}
        onFolderOpen={(f) => {
          fetch(`${API}/api/folders/public/folders/${f.id}?token=${token}`)
            .then(r => {
              if (!r.ok) throw new Error("Access denied");
              return r.json();
            })
            .then(d => setData({ type: "FOLDER", folders: d }))
            .catch(console.error);
        }}
        onFileOpen={(file) => {
          fetch(`${API}/api/files/public/files/${file.id}?token=${token}`)
            .then(r => r.json())
            .then(d =>
              setPreview({
                url: d.previewUrl,
                name: file.name,
                type: file.contentType
              })
            );
        }}
      />

      {preview && (
        <FilePreviewModal
          file={preview}
          onClose={() => setPreview(null)}
        />
      )}

    </div>
  );
}
