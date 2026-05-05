import { BrowserRouter, Routes, Route } from 'react-router-dom';

import Login from './features/auth/Login';
import AuthCallback from './features/auth/AuthCallback';
import StudentRegister from './features/auth/StudentRegister';
import CounselorRegister from './features/auth/CounselorRegister';
import CompleteProfile from './features/auth/CompleteProfile';
import CompleteCounselorProfile from './features/auth/CompleteCounselorProfile';

import PrivateRoute from './components/PrivateRoute';

import LandingPage from './pages/LandingPage';
import StudentDashboard from './pages/StudentDashboard';
import CounselorDashboard from './pages/CounselorDashboard';
import AdminDashboard from './pages/AdminDashboard';
import Pending from './pages/Pending';
import BookAppointment from './pages/BookAppointment';
import StudentProfile from './pages/StudentProfile';
import CounselorProfile from './pages/CounselorProfile';
import BrowseCounselors from './pages/BrowseCounselors';
import MyAppointments from './pages/MyAppointments';
import Counselorview from './pages/Counselorview';
import AppointmentHistory from './pages/AppointmentHistory';

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
        <Route path="/counselorprofile" element={<CounselorProfile />} />
        <Route path="/browse-counselors" element={<BrowseCounselors />} />
        <Route path="/my-appointments" element={<MyAppointments />} />
        <Route path="/counselor/:id" element={<Counselorview />} />
        <Route path="/counselor/appointments/history" element={<AppointmentHistory />} />

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