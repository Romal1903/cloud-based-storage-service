import { useEffect, useState, useMemo } from "react";
import DashboardLayout from "@/components/dashboard/DashboardLayout";
import FileGrid from "@/components/dashboard/FileGrid";
import FilePreviewModal from "@/components/dashboard/FilePreviewModal";

import { listStarredApi } from "@/api/starred";
import { listFilesApi, previewFileApi } from "@/api/files";
import { listFoldersApi } from "@/api/folders";
import { getStorageUsageApi } from "@/api/storage";
import { searchAllApi } from "@/api/search";

const PAGE_SIZE = 15;

export default function Dashboard() {
  const [currentFolder, setCurrentFolder] = useState(null);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const [previewFile, setPreviewFile] = useState(null);
  const [storageUsed, setStorageUsed] = useState(0);

  const [page, setPage] = useState(0);
  const [query, setQuery] = useState("");

  const [breadcrumb, setBreadcrumb] = useState([]);

  useEffect(() => {
    getStorageUsageApi()
      .then(res => setStorageUsed(res.used))
      .catch(() => setStorageUsed(0));
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      let folders = [];
      let files = [];

      if (query.trim()) {
        const res = await searchAllApi({
          q: query,
          folderId: currentFolder?.id ?? null,
          page: 0,
          size: 1000
        });

        folders = res.folders?.content ?? [];
        files = res.files?.content ?? [];
      } else {
        const [foldersRes, filesRes, starredRes] = await Promise.all([
          listFoldersApi(currentFolder?.id ?? null),
          listFilesApi(currentFolder?.id ?? null, 0, 1000),
          listStarredApi()
        ]);

        const starredIds = new Set(
          Array.isArray(starredRes) ? starredRes.map(f => f.id) : []
        );

        folders = foldersRes ?? [];
        files = (filesRes?.content ?? filesRes ?? []).map(f => ({
          ...f,
          starred: starredIds.has(f.id)
        }));
      }

      const combined = [
        ...folders.map(f => ({ ...f, type: "FOLDER" })),
        ...files.map(f => ({ ...f, type: "FILE" }))
      ].sort((a, b) =>
        a.name.localeCompare(b.name, undefined, { sensitivity: "base" })
      );

      setItems(combined);
    } catch (err) {
      console.error("Failed to load dashboard data", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [currentFolder, query]);

  const totalPages = Math.ceil(items.length / PAGE_SIZE);
  const hasNext = page + 1 < totalPages;

  const pagedItems = useMemo(() => {
    const start = page * PAGE_SIZE;
    return items.slice(start, start + PAGE_SIZE);
  }, [items, page]);

  const folders = pagedItems.filter(i => i.type === "FOLDER");
  const files = pagedItems.filter(i => i.type === "FILE");

  const goToRoot = () => {
    setCurrentFolder(null);
    setBreadcrumb([]);
    setPage(0);
  };

  const openFolder = (folder) => {
    setCurrentFolder(folder);
    setBreadcrumb(prev => [...prev, folder]);
    setPage(0);
  };

  const openFile = async (file) => {
    try {
      const data = await previewFileApi(file.id);
      setPreviewFile({
        url: data.previewUrl,
        name: file.name,
        type: file.contentType
      });
    } catch {
      alert("Failed to preview file");
    }
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">

        <h1 className="text-2xl font-semibold flex flex-wrap gap-2">
          <span
            onClick={goToRoot}
            className="cursor-pointer text-indigo-400 hover:underline"
          >
            My Files
          </span>

          {breadcrumb.map((folder, index) => (
            <span key={folder.id} className="flex items-center gap-2">
              <span className="text-slate-400">{">"}</span>
              <span
                onClick={() => {
                  setCurrentFolder(folder);
                  setBreadcrumb(breadcrumb.slice(0, index + 1));
                  setPage(0);
                }}
                className="cursor-pointer text-indigo-400 hover:underline"
              >
                {folder.name}
              </span>
            </span>
          ))}
        </h1>

        <input
          value={query}
          onChange={e => {
            setQuery(e.target.value);
            setPage(0);
          }}
          placeholder="Search files and folders..."
          className="w-full bg-slate-900 border border-slate-800 rounded-lg px-4 py-2"
        />

        <div className="grid grid-cols-3 gap-6">
          <StatCard label="Items" value={items.length} />
          <StatCard label="Page Items" value={pagedItems.length} />
          <StatCard label="Storage Used" value={formatSize(storageUsed)} />
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 h-[350px] overflow-hidden">
          <div className="h-full overflow-y-auto pr-2">
            {loading ? (
              <div className="flex justify-center py-20 text-slate-400">
                Loading...
              </div>
            ) : (
              <FileGrid
                folders={folders}
                files={files}
                onFolderOpen={openFolder}
                onFileOpen={openFile}
                onRefresh={loadData}
              />
            )}
          </div>
        </div>

        <div className="flex justify-end gap-2">
          <button
            disabled={page === 0}
            onClick={() => setPage(p => p - 1)}
            className="px-3 py-1 bg-slate-800 rounded disabled:opacity-40"
          >
            Prev
          </button>

          <span className="text-sm text-slate-400 px-2">
            Page {page + 1} of {totalPages || 1}
          </span>

          <button
            disabled={!hasNext}
            onClick={() => setPage(p => p + 1)}
            className="px-3 py-1 bg-slate-800 rounded disabled:opacity-40"
          >
            Next
          </button>
        </div>
      </div>

      {previewFile && (
        <FilePreviewModal
          file={previewFile}
          onClose={() => setPreviewFile(null)}
        />
      )}
    </DashboardLayout>
  );
}


function StatCard({ label, value }) {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
      <p className="text-slate-400 text-sm">{label}</p>
      <p className="text-2xl font-bold mt-1">{value}</p>
    </div>
  );
}

function formatSize(bytes) {
  if (!bytes) return "—";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}
