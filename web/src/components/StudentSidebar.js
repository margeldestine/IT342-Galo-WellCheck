import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/StudentSidebar.css';

const StudentSidebar = ({ activeItem, onTabChange }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  const handleItemClick = (item, path) => {
    if (onTabChange && (item === 'dashboard' || item === 'appointments')) {
      onTabChange(item);
      if (window.location.pathname !== '/dashboard') {
        navigate(`/dashboard?tab=${item}`);
      }
    } else {
      if (item === 'dashboard' || item === 'appointments') {
        navigate(`/dashboard?tab=${item}`);
      } else {
        navigate(path);
      }
    }
  };

  return (
    <aside className="student-sidebar">
      <nav className="sidebar-nav">
        <div className={`nav-item ${activeItem === 'dashboard' ? 'active' : ''}`}
          onClick={() => handleItemClick('dashboard', '/dashboard')}>
          <span className="nav-icon">🏠</span> Dashboard
        </div>
        <div className={`nav-item ${activeItem === 'browse-counselors' ? 'active' : ''}`}
          onClick={() => handleItemClick('browse-counselors', '/browse-counselors')}>
          <span className="nav-icon">👥</span> Browse Counselors
        </div>
        <div className={`nav-item ${activeItem === 'appointments' ? 'active' : ''}`}
          onClick={() => handleItemClick('appointments', '/dashboard')}>
          <span className="nav-icon">📅</span> My Appointments
        </div>
        <div className={`nav-item ${activeItem === 'profile' ? 'active' : ''}`}
          onClick={() => handleItemClick('profile', '/studentprofile')}>
          <span className="nav-icon">👤</span> Profile
        </div>
      </nav>
      <div className="sidebar-logout" onClick={handleLogout}>
        <span className="nav-icon">↪</span> Log Out
      </div>
    </aside>
  );
};

export default StudentSidebar;
