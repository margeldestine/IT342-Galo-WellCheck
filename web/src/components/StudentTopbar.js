import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/StudentTopbar.css';

const StudentTopbar = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Student';
  const lastName = user.lastName || '';

  return (
    <div className="student-topbar">
      <div className="topbar-brand" onClick={() => navigate('/dashboard')}>
        <div className="navbar-logo">♥</div>
        <div className="topbar-brand-text">
          <div className="navbar-title">WellCheck</div>
          <div className="navbar-subtitle">Student Portal</div>
        </div>
      </div>
      <div className="topbar-user" onClick={() => navigate('/studentprofile')} style={{ cursor: 'pointer' }}>
        <span className="topbar-name">{firstName} {lastName}</span>
        <div className="topbar-avatar">{firstName.charAt(0)}</div>
      </div>
    </div>
  );
};

export default StudentTopbar;
