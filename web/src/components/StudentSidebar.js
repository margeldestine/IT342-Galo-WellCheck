import React from 'react';
import { useNavigate } from 'react-router-dom';
import { LayoutDashboard, Users, Calendar, User, LogOut } from 'lucide-react';
import logo from '../assets/wellcheck-logo.png';
import '../styles/StudentSidebar.css';

const StudentSidebar = ({ activeItem, onTabChange }) => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Student';
  const lastName = user.lastName || '';

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  const handleItemClick = (item, path) => {
    if (item === 'dashboard' && onTabChange) {
      onTabChange(item);
      if (window.location.pathname !== '/dashboard') navigate('/dashboard');
    } else {
      navigate(path);
    }
  };

  return (
    <aside className="student-sidebar">
      <div className="sidebar-logo">
        <img src={logo} alt="WellCheck" style={{ width: '34px', height: '34px' }} />
        <div className="logo-words">
          <div className="logo-name">WellCheck</div>
          <div className="logo-tagline">Student Portal</div>
        </div>
      </div>

      <nav className="sidebar-nav">
        <button className={`nav-link ${activeItem === 'dashboard' ? 'active' : ''}`}
          onClick={() => handleItemClick('dashboard', '/dashboard')}>
          <LayoutDashboard size={16} /> Dashboard
        </button>
        <button className={`nav-link ${activeItem === 'browse-counselors' ? 'active' : ''}`}
          onClick={() => handleItemClick('browse-counselors', '/browse-counselors')}>
          <Users size={16} /> Browse Counselors
        </button>
        <button className={`nav-link ${activeItem === 'appointments' ? 'active' : ''}`}
          onClick={() => handleItemClick('appointments', '/my-appointments')}>
          <Calendar size={16} /> My Appointments
        </button>
        <button className={`nav-link ${activeItem === 'profile' ? 'active' : ''}`}
          onClick={() => handleItemClick('profile', '/studentprofile')}>
          <User size={16} /> Profile
        </button>
      </nav>

      <div className="sidebar-user">
        <div className="user-card">
          <div className="user-avatar">
            {firstName.charAt(0)}{lastName.charAt(0)}
          </div>
          <div className="user-info">
            <div className="user-name">{firstName} {lastName}</div>
            <div className="user-meta">Student</div>
          </div>
        </div>
        <button className="logout-btn" onClick={handleLogout}>
          <LogOut size={13} /> Log Out
        </button>
      </div>
    </aside>
  );
};

export default StudentSidebar;