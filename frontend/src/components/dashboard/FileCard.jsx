import { FileText, MoreVertical, Star, Share2 } from "lucide-react";
import { useState } from "react";
import { starFileApi, unstarFileApi } from "@/api/starred";
import { renameFileApi, moveFileApi, deleteFileApi } from "@/api/files";
import ShareModal from "@/components/share/ShareModal";
import MoveModal from "@/components/dashboard/MoveModal";
import { canEdit, canDelete, canShare, canStar } from "@/utils/permissions";

export default function FileCard({ file, onOpen, onRefresh }) {
  const [hover, setHover] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [shareOpen, setShareOpen] = useState(false);
  const [moveOpen, setMoveOpen] = useState(false);

  const toggleStar = async (e) => {
    e.stopPropagation();
    if (!canStar(file.permission)) return;

    file.starred
      ? await unstarFileApi(file.id)
      : await starFileApi(file.id);

    onRefresh?.();
    setMenuOpen(false);
  };

  const rename = async () => {
    if (!canEdit(file.permission)) return;
    const name = prompt("New name", file.name);
    if (!name || name === file.name) return;

    await renameFileApi(file.id, name);
    onRefresh?.();
    setMenuOpen(false);
  };

  const move = async (folder) => {
    await moveFileApi(file.id, folder.id);
    setMoveOpen(false);
    onRefresh?.();
  };

  const remove = async () => {
    if (!canDelete(file.permission)) return;
    if (!confirm("Move file to recycle bin?")) return;

    await deleteFileApi(file.id);
    onRefresh?.();
    setMenuOpen(false);
  };

  return (
    <div
      onMouseEnter={() => setHover(true)}
      onMouseLeave={() => {
        setHover(false);
        setMenuOpen(false);
      }}
      className="relative rounded-xl border border-slate-800 bg-slate-900/60 hover:border-indigo-500/30 transition"
    >
      <div onClick={onOpen} className="h-32 flex items-center justify-center cursor-pointer">
        <FileText size={40} className="text-slate-400" />
      </div>

      <div className="px-3 pb-3">
        <p className="text-sm truncate">{file.name}</p>
        <p className="text-xs text-slate-400">{formatSize(file.size)}</p>
      </div>

      {file.starred && <Star size={16} className="absolute top-2 left-2 text-yellow-400" />}
      {file.shared && <Share2 size={14} className="absolute top-2 left-7 text-indigo-400" />}

      {hover && (
        <button
          onClick={(e) => {
            e.stopPropagation();
            setMenuOpen(v => !v);
          }}
          className="absolute top-2 right-2 p-1 rounded hover:bg-slate-800"
        >
          <MoreVertical size={16} />
        </button>
      )}

      {menuOpen && (
        <div className="absolute right-2 top-10 bg-slate-900 border border-slate-800 rounded-lg w-32 z-20">
          <button onClick={toggleStar} className="w-full px-3 py-2 text-left hover:bg-slate-800">
            {file.starred ? "Unstar" : "Star"}
          </button>

          <button
            onClick={() => {
              setShareOpen(true);
              setMenuOpen(false);
            }}
            disabled={!canShare(file.permission)}
            className="w-full px-3 py-2 text-left hover:bg-slate-800 disabled:opacity-40"
          >
            Share
          </button>

          <button
            onClick={rename}
            disabled={!canEdit(file.permission)}
            className="w-full px-3 py-2 text-left hover:bg-slate-800 disabled:opacity-40"
          >
            Rename
          </button>

          <button
            onClick={() => setMoveOpen(true)}
            disabled={!canEdit(file.permission)}
            className="w-full px-3 py-2 text-left hover:bg-slate-800 disabled:opacity-40"
          >
            Move
          </button>

          <button
            onClick={remove}
            disabled={!canDelete(file.permission)}
            className="w-full px-3 py-2 text-left text-red-400 hover:bg-slate-800 disabled:opacity-40"
          >
            Delete
          </button>
        </div>
      )}

      {shareOpen && (
        <ShareModal resource={file} type="FILE" onClose={() => setShareOpen(false)} />
      )}

      {moveOpen && (
        <MoveModal onClose={() => setMoveOpen(false)} onMove={move} />
      )}
    </div>
  );
}

function formatSize(bytes) {
  if (!bytes) return "—";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}
