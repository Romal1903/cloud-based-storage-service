import React from "react";

export default function AuthLayout({ children }) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 text-white">
      <div className="relative">
        <div className="absolute -inset-1 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-2xl blur opacity-30"></div>
        <div className="relative bg-slate-900/90 backdrop-blur-xl rounded-2xl shadow-2xl p-8 w-[380px] transition-transform duration-300 hover:-translate-y-1 hover:shadow-indigo-500/20">
          {children}
        </div>
      </div>
    </div>
  );
}
