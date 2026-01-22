import { useEffect, useState } from "react";
import DashboardLayout from "@/components/dashboard/DashboardLayout";
import {
  restoreFileApi,
  permanentDeleteFileApi
} from "@/api/files";
import {
  restoreFolderApi,
  permanentDeleteFolderApi
} from "@/api/folders";
import api from "@/api/axios";

export default function RecycleBin() {
  const [items, setItems] = useState([]);
  const [selected, setSelected] = useState(new Set());

  const loadRecycleBin = () => {
    api.get("/api/recycle-bin").then(res => {
      const response = res.data;

      let normalized = [];

      if (Array.isArray(response)) {
        normalized = response;
      } else {
        const files = (response.files || []).map(f => ({
          ...f,
          type: "FILE"
        }));

        const folders = (response.folders || []).map(f => ({
          ...f,
          type: "FOLDER"
        }));

        normalized = [...files, ...folders];
      }

      setItems(normalized);
      setSelected(new Set());
    });
  };

  useEffect(() => {
    loadRecycleBin();
  }, []);

  const toggleSelect = (id) => {
    setSelected(prev => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };

  const selectAll = () => {
    if (selected.size === items.length) {
      setSelected(new Set());
    } else {
      setSelected(new Set(items.map(i => i.id)));
    }
  };

  const restoreSelected = async () => {
    for (const item of items) {
      if (!selected.has(item.id)) continue;

      if (item.type === "FILE") {
        await restoreFileApi(item.id);
      } else {
        await restoreFolderApi(item.id);
      }
    }
    loadRecycleBin();
  };

  const deleteSelected = async () => {
    const ok = window.confirm("Permanently delete selected items?");
    if (!ok) return;

    for (const item of items) {
      if (!selected.has(item.id)) continue;

      if (item.type === "FILE") {
        await permanentDeleteFileApi(item.id);
      } else {
        await permanentDeleteFolderApi(item.id);
      }
    }
    loadRecycleBin();
  };

  return (
    <DashboardLayout>
      <h1 className="text-2xl mb-4">Recycle Bin</h1>

      {items.length > 0 && (
        <div className="flex items-center gap-4 mb-4">
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={selected.size === items.length}
              onChange={selectAll}
            />
            Select All
          </label>

          <button
            disabled={selected.size === 0}
            onClick={restoreSelected}
            className="px-3 py-1 bg-slate-800 rounded disabled:opacity-40"
          >
            Restore Selected
          </button>

          <button
            disabled={selected.size === 0}
            onClick={deleteSelected}
            className="px-3 py-1 bg-red-600 rounded disabled:opacity-40"
          >
            Delete Selected
          </button>
        </div>
      )}

      {items.length === 0 && (
        <p className="text-slate-400">Recycle bin is empty</p>
      )}

      {items.map(item => (
        <div
          key={`${item.type}-${item.id}`}
          className="flex items-center justify-between p-3 border-b border-slate-800"
        >
          <div className="flex items-center gap-3">
            <input
              type="checkbox"
              checked={selected.has(item.id)}
              onChange={() => toggleSelect(item.id)}
            />
            <span>{item.name}</span>
          </div>

          <div className="flex gap-2">
            <button
              onClick={async () => {
                if (item.type === "FILE") {
                  await restoreFileApi(item.id);
                } else {
                  await restoreFolderApi(item.id);
                }
                loadRecycleBin();
              }}
            >
              Restore
            </button>

            <button
              className="text-red-400"
              onClick={async () => {
                const ok = window.confirm("Permanently delete this item?");
                if (!ok) return;

                if (item.type === "FILE") {
                  await permanentDeleteFileApi(item.id);
                } else {
                  await permanentDeleteFolderApi(item.id);
                }
                loadRecycleBin();
              }}
            >
              Delete
            </button>
          </div>
        </div>
      ))}
    </DashboardLayout>
  );
}
