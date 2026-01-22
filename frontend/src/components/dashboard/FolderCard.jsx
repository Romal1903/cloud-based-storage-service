import { Folder, MoreVertical, Share2, Star } from "lucide-react";
import { useState } from "react";
import ShareModal from "@/components/share/ShareModal";
import MoveModal from "@/components/dashboard/MoveModal";
import { renameFolderApi, moveFolderApi, deleteFolderApi } from "@/api/folders";
import { canEdit, canDelete, canShare } from "@/utils/permissions";

export default function FolderCard({ folder, onOpen, onRefresh }) {
  const [hover, setHover] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [shareOpen, setShareOpen] = useState(false);
  const [moveOpen, setMoveOpen] = useState(false);

  const rename = async () => {
    if (!canEdit(folder.permission)) return;
    const name = prompt("New folder name", folder.name);
    if (!name || name === folder.name) return;

    await renameFolderApi(folder.id, name);
    onRefresh?.();
    setMenuOpen(false);
  };

  const move = async (target) => {
    await moveFolderApi(folder.id, target.id);
    setMoveOpen(false);
    onRefresh?.();
  };

  const remove = async () => {
    if (!canDelete(folder.permission)) return;
    if (!confirm("Move folder to recycle bin?")) return;

    await deleteFolderApi(folder.id);
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
      className="relative cursor-pointer rounded-xl border border-slate-800 bg-slate-900/60 p-4 hover:border-indigo-500/40 transition"
    >
      <div onClick={onOpen} className="flex items-center gap-3">
        <div className="h-10 w-10 rounded-lg bg-indigo-500/10 flex items-center justify-center">
          <Folder className="text-indigo-400" size={22} />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium truncate">{folder.name}</p>
          <p className="text-xs text-slate-400">Folder</p>
        </div>
      </div>

      {folder.shared && <Share2 size={14} className="absolute top-2 left-7 text-indigo-400" />}

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
          <button
            onClick={() => {
              setShareOpen(true);
              setMenuOpen(false);
            }}
            disabled={!canShare(folder.permission)}
            className="w-full px-3 py-2 text-left hover:bg-slate-800 disabled:opacity-40"
          >
            Share
          </button>

          <button
            onClick={rename}
            disabled={!canEdit(folder.permission)}
            className="w-full px-3 py-2 text-left hover:bg-slate-800 disabled:opacity-40"
          >
            Rename
          </button>

          <button
            onClick={() => setMoveOpen(true)}
            disabled={!canEdit(folder.permission)}
            className="w-full px-3 py-2 text-left hover:bg-slate-800 disabled:opacity-40"
          >
            Move
          </button>

          <button
            onClick={remove}
            disabled={!canDelete(folder.permission)}
            className="w-full px-3 py-2 text-left text-red-400 hover:bg-slate-800 disabled:opacity-40"
          >
            Delete
          </button>
        </div>
      )}

      {shareOpen && <ShareModal resource={folder} type="FOLDER" onClose={() => setShareOpen(false)} />}
      {moveOpen && <MoveModal onClose={() => setMoveOpen(false)} onMove={move} />}
    </div>
  );
}
