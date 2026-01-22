import { useEffect, useState } from "react";
import { listFoldersApi } from "@/api/folders";

export default function MoveModal({ onClose, onMove }) {
  const [folders, setFolders] = useState([]);
  const [target, setTarget] = useState(null);

  useEffect(() => {
    listFoldersApi(null).then(setFolders);
  }, []);

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
      <div className="bg-slate-900 border border-slate-800 rounded-xl w-96 p-4">
        <h2 className="text-lg mb-3">Move to folder</h2>

        <div className="max-h-60 overflow-y-auto space-y-2">
          {folders.map(f => (
            <button
              key={f.id}
              onClick={() => setTarget(f)}
              className={`w-full text-left px-3 py-2 rounded
                ${target?.id === f.id ? "bg-indigo-500/20" : "hover:bg-slate-800"}`}
            >
              {f.name}
            </button>
          ))}
        </div>

        <div className="flex justify-end gap-2 mt-4">
          <button onClick={onClose} className="px-3 py-1 bg-slate-800 rounded">
            Cancel
          </button>
          <button
            disabled={!target}
            onClick={() => onMove(target)}
            className="px-3 py-1 bg-indigo-600 rounded disabled:opacity-40"
          >
            Move
          </button>
        </div>
      </div>
    </div>
  );
}
