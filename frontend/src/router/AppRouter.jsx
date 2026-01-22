import { BrowserRouter, Routes, Route } from "react-router-dom";
import Login from "@/pages/Login";
import Register from "@/pages/Register";
import ForgotPassword from "@/pages/ForgotPassword";
import ResetPassword from "@/pages/ResetPassword";
import Dashboard from "@/pages/Dashboard";
import AuthCheck from "@/pages/AuthCheck";
import Upload from "@/pages/Upload";
import Starred from "@/pages/Starred";
import RecycleBin from "@/pages/RecycleBin";
import Shared from "@/pages/Shared";
import PublicShare from "@/pages/PublicShare";
import SharedFolder from "@/pages/SharedFolder";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/dashboard" element={<AuthCheck> <Dashboard /> </AuthCheck>} />
        <Route path="/starred" element={<AuthCheck><Starred /></AuthCheck>} />
        <Route path="/upload" element={<AuthCheck><Upload /></AuthCheck>} />
        <Route path="/recycle-bin" element={<AuthCheck><RecycleBin /></AuthCheck>} />
        <Route path="/shared" element={<AuthCheck><Shared /></AuthCheck>} />
        <Route path="/public/:token" element={<PublicShare />} />
        <Route path="/shared/folder/:folderId" element={<AuthCheck><SharedFolder /></AuthCheck>} />
      </Routes>
    </BrowserRouter>
  );
}
