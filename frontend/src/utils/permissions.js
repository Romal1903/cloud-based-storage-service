const normalize = (p) => p || "OWNER";

export const canEdit = (p) => {
  p = normalize(p);
  return p === "OWNER" || p === "EDITOR";
};

export const canDelete = (p) => {
  p = normalize(p);
  return p === "OWNER" || p === "EDITOR";
};

export const canShare = (p) => {
  p = normalize(p);
  return p === "OWNER";
};

export const canStar = (p) => {
  p = normalize(p);
  return p !== "VIEWER";
};
