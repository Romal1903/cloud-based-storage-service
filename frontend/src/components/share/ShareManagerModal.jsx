import { useState } from "react";
import { revokeShareApi, updateSharePermissionApi } from "@/api/share";

export default function ShareManagerModal({ resource, onClose, onUpdated }) {
  const [permission, setPermission] = useState(resource.permission);
  const [expiresAt, setExpiresAt] = useState(
    resource.expiresAt?.slice(0, 16)
  );
  const [loading, setLoading] = useState(false);

  const update = async () => {
    setLoading(true);
    await updateSharePermissionApi(resource.id, {
      permission,
      expiresAt
    });

    await onUpdated();
    onClose();
  };

  const revoke = async () => {
    await revokeShareApi(resource.id);
    await onUpdated();
    onClose();
  };

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 w-[420px]">
        <h2 className="text-lg mb-4">
          Access for {resource.name}
        </h2>

        <p className="text-sm mb-3">
          {resource.email}
        </p>

        {resource.email !== "Public link" && (
          <select
            value={permission}
            onChange={e => setPermission(e.target.value)}
            className="w-full bg-slate-800 p-2 rounded mb-3"
          >
            <option value="VIEWER">Viewer</option>
            <option value="EDITOR">Editor</option>
          </select>
        )}

        <input
          type="datetime-local"
          value={expiresAt}
          onChange={e => setExpiresAt(e.target.value)}
          className="w-full bg-slate-800 p-2 rounded mb-4"
        />

        <div className="flex justify-between items-center">
          <button
            onClick={revoke}
            className="text-red-400"
          >
            Remove
          </button>

          <div className="flex gap-3">
            <button
              onClick={onClose}
              className="text-slate-400"
            >
              Close
            </button>

            <button
              onClick={update}
              disabled={loading}
              className="bg-indigo-600 px-4 py-1 rounded disabled:opacity-50"
            >
              Update
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
