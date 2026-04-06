import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LandingPage from './pages/LandingPage';
import Login from './pages/Login';
import StudentRegister from './pages/StudentRegister';
import CounselorRegister from './pages/CounselorRegister';
import StudentDashboard from './pages/StudentDashboard';
import CounselorDashboard from './pages/CounselorDashboard';
import AdminDashboard from './pages/AdminDashboard';
import PrivateRoute from './components/PrivateRoute';
import AuthCallback from './pages/AuthCallback';
import CompleteProfile from './pages/CompleteProfile';
import CompleteCounselorProfile from './pages/CompleteCounselorProfile';
import Pending from './pages/Pending';
import BookAppointment from './pages/BookAppointment';
import StudentProfile from './pages/StudentProfile';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/auth/callback" element={<AuthCallback />} />
        <Route path="/register/student" element={<StudentRegister />} />
        <Route path="/register/counselor" element={<CounselorRegister />} />
        <Route path="/complete-profile" element={<CompleteProfile />} />
        <Route path="/complete-counselor-profile" element={<CompleteCounselorProfile />} />
        <Route path="/pending" element={<Pending />} />
        <Route path="/book-appointment" element={<BookAppointment />} />
        <Route path="/studentprofile" element={<StudentProfile />} />


        {/* Protected routes */}
        <Route path="/dashboard" element={
          <PrivateRoute allowedRoles={['STUDENT']}>
            <StudentDashboard />
          </PrivateRoute>
        } />

        <Route path="/counselor/dashboard" element={
          <PrivateRoute allowedRoles={['COUNSELOR']}>
            <CounselorDashboard />
          </PrivateRoute>
        } />

        <Route path="/admin/dashboard" element={
          <PrivateRoute allowedRoles={['ADMIN']}>
            <AdminDashboard />
          </PrivateRoute>
        } />

      </Routes>
    </BrowserRouter>
  );
}

export default App;

