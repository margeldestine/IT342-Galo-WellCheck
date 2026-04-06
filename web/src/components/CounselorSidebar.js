import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/CounselorSidebar.css';

const CounselorSidebar = ({ activeItem, onTabChange, pendingCount }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  const handleItemClick = (item, path) => {
    if (onTabChange && (item === 'dashboard' || item === 'slots' || item === 'requests')) {
      onTabChange(item);
      if (window.location.pathname !== '/counselor/dashboard') {
        navigate(`/counselor/dashboard?tab=${item}`);
      }
    } else {
      if (item === 'dashboard' || item === 'slots' || item === 'requests') {
        navigate(`/counselor/dashboard?tab=${item}`);
      } else {
        navigate(path);
      }
    }
  };

  return (
    <aside className="counselor-sidebar">
      <nav className="sidebar-nav">
        <div className={`nav-item ${activeItem === 'dashboard' ? 'active' : ''}`}
          onClick={() => handleItemClick('dashboard', '/counselor/dashboard')}>
          <span className="nav-icon">🏠</span> Dashboard
        </div>
        <div className={`nav-item ${activeItem === 'slots' ? 'active' : ''}`}
          onClick={() => handleItemClick('slots', '/counselor/dashboard')}>
          <span className="nav-icon">📅</span> Manage Slots
        </div>
        <div className={`nav-item ${activeItem === 'requests' ? 'active' : ''}`}
          onClick={() => handleItemClick('requests', '/counselor/dashboard')}>
          <span className="nav-icon">📋</span> Requests
          {pendingCount > 0 && <span className="nav-badge">{pendingCount}</span>}
        </div>
        <div className={`nav-item ${activeItem === 'profile' ? 'active' : ''}`}
          onClick={() => handleItemClick('profile', '/counselorprofile')}>
          <span className="nav-icon">👤</span> Profile
        </div>
      </nav>
      <div className="sidebar-logout" onClick={handleLogout}>
        <span className="nav-icon">↪</span> Log Out
      </div>
    </aside>
  );
};

export default CounselorSidebar;
