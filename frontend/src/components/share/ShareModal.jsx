import { useEffect, useState } from "react";
import { shareWithUserApi, createPublicShareApi } from "@/api/share";

export default function ShareModal({ resource, type, onClose }) {
  const [email, setEmail] = useState("");
  const [permission, setPermission] = useState("VIEWER");
  const [expiry, setExpiry] = useState("");
  const [publicLink, setPublicLink] = useState(false);

  useEffect(() => {
    if (publicLink) {
      setPermission("VIEWER");
      setEmail("");
    }
  }, [publicLink]);

  const submit = async () => {
    if (!expiry) {
      alert("Expiry is required");
      return;
    }

    if (publicLink) {
      const res = await createPublicShareApi({
        resourceType: type,
        resourceId: resource.id,
        expiresAt: expiry
      });

      const link = `${window.location.origin}/public/${res.token}`;

      await navigator.clipboard.writeText(link);
      alert("Public link copied to clipboard");
    } 
    
    else {
      if (!email) {
        alert("Email is required");
        return;
      }

      await shareWithUserApi(type, resource.id, {
        email,
        permission,
        expiresAt: expiry
      });
    }

    onClose();
  };

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 w-96">
        <h2 className="text-lg font-semibold mb-4">
          Share {resource.name}
        </h2>

        {!publicLink && (
          <input
            className="w-full mb-3 p-2 bg-slate-800 rounded"
            placeholder="User email"
            value={email}
            onChange={e => setEmail(e.target.value)}
          />
        )}

        {!publicLink && (
          <select
            className="w-full mb-3 p-2 bg-slate-800 rounded"
            value={permission}
            onChange={e => setPermission(e.target.value)}
          >
            <option value="VIEWER">Viewer</option>
            <option value="EDITOR">Editor</option>
          </select>
        )}

        {publicLink && (
          <div className="mb-2 text-sm text-slate-400">
            Public links are read-only (Viewer)
          </div>
        )}

        <input
          type="datetime-local"
          className="w-full mb-3 p-2 bg-slate-800 rounded"
          value={expiry}
          onChange={e => setExpiry(e.target.value)}
        />

        <label className="flex items-center gap-2 text-sm mb-4">
          <input
            type="checkbox"
            checked={publicLink}
            onChange={e => setPublicLink(e.target.checked)}
          />
          Public link
        </label>

        <div className="flex justify-end gap-2">
          <button onClick={onClose} className="px-3 py-1 text-slate-400">
            Cancel
          </button>
          <button onClick={submit} className="px-3 py-1 bg-indigo-600 rounded">
            Share
          </button>
        </div>
      </div>
    </div>
  );
}
