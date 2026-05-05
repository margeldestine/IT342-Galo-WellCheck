import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/wellcheck-logo.png';

const CounselorSidebar = ({ activeItem, onTabChange, pendingCount }) => {
  const navigate = useNavigate();
  const [user, setUser] = useState(JSON.parse(localStorage.getItem('user') || '{}'));
  const firstName = user.firstName || 'Counselor';
  const lastName = user.lastName || '';
  const initials = `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();

  useEffect(() => {
    const handleStorage = () => {
      setUser(JSON.parse(localStorage.getItem('user') || '{}'));
    };
    window.addEventListener('storage', handleStorage);
    const interval = setInterval(() => {
      setUser(JSON.parse(localStorage.getItem('user') || '{}'));
    }, 1000);
    return () => {
      window.removeEventListener('storage', handleStorage);
      clearInterval(interval);
    };
  }, []);

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  const handleItemClick = (item, path) => {
    if (onTabChange && ['dashboard', 'slots', 'requests'].includes(item)) {
      onTabChange(item);
      if (window.location.pathname !== '/counselor/dashboard') {
        navigate(`/counselor/dashboard?tab=${item}`);
      }
    } else if (['dashboard', 'slots', 'requests'].includes(item)) {
      navigate(`/counselor/dashboard?tab=${item}`);
    } else {
      navigate(path);
    }
  };

  return (
    <aside className="cp-sidebar">
      <div className="cp-sidebar-logo">
        <img src={logo} alt="WellCheck" style={{ width: '34px', height: '34px' }} />
        <div className="cp-logo-words">
          <div className="cp-logo-name">WellCheck</div>
          <div className="cp-logo-tagline">Counselor Portal</div>
        </div>
      </div>

      <div className="cp-counselor-strip">
        <div className="cp-strip-avatar" style={{ overflow: 'hidden', padding: 0 }}>
          {user.profilePhoto
            ? <img src={user.profilePhoto} alt="avatar"
                style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '50%' }} />
            : initials
          }
        </div>
        <div>
          <div className="cp-strip-name">{firstName} {lastName}</div>
          <div className="cp-strip-role">{user.specialization || 'Counselor'}</div>
        </div>
        <div className="cp-online-dot" title="Online"></div>
      </div>

      <nav className="cp-nav">
        <button
          className={`cp-nav-link ${activeItem === 'dashboard' ? 'cp-active' : ''}`}
          onClick={() => handleItemClick('dashboard', '/counselor/dashboard')}
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/></svg>
          Dashboard
        </button>

        <button
          className={`cp-nav-link ${activeItem === 'slots' ? 'cp-active' : ''}`}
          onClick={() => handleItemClick('slots', '/counselor/dashboard')}
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><rect x="3" y="4" width="18" height="18" rx="2.5"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          Manage Slots
        </button>

        <button
          className={`cp-nav-link ${activeItem === 'requests' ? 'cp-active' : ''}`}
          onClick={() => handleItemClick('requests', '/counselor/dashboard')}
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/><line x1="9" y1="12" x2="15" y2="12"/><line x1="9" y1="16" x2="13" y2="16"/></svg>
          Requests
          {pendingCount > 0 && <span className="cp-nav-badge">{pendingCount}</span>}
        </button>

        <button
          className={`cp-nav-link ${activeItem === 'profile' ? 'cp-active' : ''}`}
          onClick={() => handleItemClick('profile', '/counselorprofile')}
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.58-7 8-7s8 3 8 7"/></svg>
          Profile
        </button>
      </nav>

      <div className="cp-sidebar-footer">
        <button className="cp-logout-btn" onClick={handleLogout}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          Log Out
        </button>
      </div>
    </aside>
  );
};

export default CounselorSidebar;