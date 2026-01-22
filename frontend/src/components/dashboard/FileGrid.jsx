import FolderCard from "./FolderCard";
import FileCard from "./FileCard";

export default function FileGrid({
  folders = [],
  files = [],
  onFolderOpen,
  onFileOpen,
  onStarToggle,
  onRefresh
}) {
  if (folders.length === 0 && files.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-slate-400">
        <p className="text-lg">This folder is empty</p>
        <p className="text-sm mt-1">
          Upload files or create a new folder
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-5 gap-5">
      {folders.map(folder => (
        <FolderCard
          key={folder.id}
          folder={folder}
          onOpen={() => onFolderOpen(folder)}
          onRefresh={onRefresh}
          onStarToggle={onStarToggle}
        />
      ))}

      {files.map(file => (
        <FileCard
          key={file.id}
          file={file}
          onOpen={() => onFileOpen(file)}
          onStarToggle={onStarToggle}
          onRefresh={onRefresh}
        />
      ))}
    </div>
  );
}
