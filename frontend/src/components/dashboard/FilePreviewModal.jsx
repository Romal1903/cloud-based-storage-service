import { X } from "lucide-react";

export default function FilePreviewModal({ file, onClose }) {
  if (!file) return null;

  const isImage = file.type.startsWith("image/");
  const isPdf = file.type === "application/pdf";

  return (
    <div className="fixed inset-0 z-50 bg-black/70 flex items-center justify-center">
      <div className="bg-slate-900 rounded-xl w-[80%] h-[80%] relative border border-slate-800">

        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-white"
        >
          <X />
        </button>

        <div className="h-full flex items-center justify-center p-6">
          {isImage && (
            <img
              src={file.url}
              alt={file.name}
              className="max-h-full max-w-full rounded-lg"
            />
          )}

          {isPdf && (
            <iframe
              src={file.url}
              className="w-full h-full rounded-lg"
            />
          )}

          {!isImage && !isPdf && (
            <div className="text-center text-slate-400">
              <p className="mb-4">Preview not supported</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
