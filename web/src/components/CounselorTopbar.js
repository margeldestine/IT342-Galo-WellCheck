import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/CounselorTopbar.css';
import logo from '../assets/wellcheck-logo.png';

const CounselorTopbar = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Counselor';
  const lastName = user.lastName || '';

  return (
    <div className="counselor-topbar">
      <div className="topbar-brand" onClick={() => navigate('/counselor/dashboard')}>
        <img src={logo} alt="WellCheck" style={{ width: '36px', height: '36px' }} />
        <div className="topbar-brand-text">
          <div className="navbar-title">WellCheck</div>
          <div className="navbar-subtitle">Counselor Portal</div>
        </div>
      </div>
      <div className="topbar-user" onClick={() => navigate('/counselorprofile')} style={{ cursor: 'pointer' }}>
        <span className="topbar-name">{firstName} {lastName}</span>
        <div className="topbar-avatar">{firstName.charAt(0)}</div>
      </div>
    </div>
  );
};

export default CounselorTopbar;