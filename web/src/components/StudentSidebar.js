import React from 'react';
import { useNavigate } from 'react-router-dom';
import { LayoutDashboard, Users, Calendar, User, LogOut } from 'lucide-react';
import logo from '../assets/wellcheck-logo.png';
import './StudentSidebar.css';

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
    <aside className="ss-sidebar">
      <div className="ss-logo">
        <img src={logo} alt="WellCheck" style={{ width: '34px', height: '34px' }} />
        <div className="ss-logo-words">
          <div className="ss-logo-name">WellCheck</div>
          <div className="ss-logo-tagline">Student Portal</div>
        </div>
      </div>

      <nav className="ss-nav">
        <button className={`ss-nav-link ${activeItem === 'dashboard' ? 'active' : ''}`}
          onClick={() => handleItemClick('dashboard', '/dashboard')}>
          <LayoutDashboard size={16} /> Dashboard
        </button>
        <button className={`ss-nav-link ${activeItem === 'browse-counselors' ? 'active' : ''}`}
          onClick={() => handleItemClick('browse-counselors', '/browse-counselors')}>
          <Users size={16} /> Browse Counselors
        </button>
        <button className={`ss-nav-link ${activeItem === 'appointments' ? 'active' : ''}`}
          onClick={() => handleItemClick('appointments', '/my-appointments')}>
          <Calendar size={16} /> My Appointments
        </button>
        <button className={`ss-nav-link ${activeItem === 'profile' ? 'active' : ''}`}
          onClick={() => handleItemClick('profile', '/studentprofile')}>
          <User size={16} /> Profile
        </button>
      </nav>

      <div className="ss-user">
        <div className="ss-user-card">
          <div className="ss-user-avatar">
            {firstName.charAt(0)}{lastName.charAt(0)}
          </div>
          <div className="ss-user-info">
            <div className="ss-user-name">{firstName} {lastName}</div>
            <div className="ss-user-meta">Student</div>
          </div>
        </div>
        <button className="ss-logout-btn" onClick={handleLogout}>
          <LogOut size={13} /> Log Out
        </button>
      </div>
    </aside>
  );
};

export default StudentSidebar;