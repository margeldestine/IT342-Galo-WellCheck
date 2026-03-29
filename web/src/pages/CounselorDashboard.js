import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/CounselorDashboard.css';

function CounselorDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Counselor';
  const lastName = user.lastName || '';
  const [activeTab, setActiveTab] = useState('dashboard');

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  return (
    <div className="counselor-wrapper">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="navbar-logo">♥</div>
          <div>
            <div className="navbar-title">WellCheck</div>
            <div className="sidebar-subtitle">Counselor Portal</div>
          </div>
        </div>
        <nav className="sidebar-nav">
          <div
            className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`}
            onClick={() => setActiveTab('dashboard')}
          >
            <span className="nav-icon">⊞</span> Dashboard
          </div>
          <div
            className={`nav-item ${activeTab === 'slots' ? 'active' : ''}`}
            onClick={() => setActiveTab('slots')}
          >
            <span className="nav-icon">📅</span> Manage Slots
          </div>
          <div
            className={`nav-item ${activeTab === 'requests' ? 'active' : ''}`}
            onClick={() => setActiveTab('requests')}
          >
            <span className="nav-icon">📋</span> Requests
          </div>
          <div
            className={`nav-item ${activeTab === 'profile' ? 'active' : ''}`}
            onClick={() => setActiveTab('profile')}
          >
            <span className="nav-icon">👤</span> Profile
          </div>
        </nav>
        <div className="sidebar-logout" onClick={handleLogout}>
          <span className="nav-icon">↪</span> Log Out
        </div>
      </aside>

      {/* Main */}
      <main className="dashboard-main">
        {/* Topbar */}
        <div className="topbar">
          <div />
          <div className="topbar-user">
            <span className="topbar-name">{firstName} {lastName}</span>
            <div className="topbar-avatar">{firstName.charAt(0)}</div>
          </div>
        </div>

        {/* Content */}
        <div className="dashboard-content">

          {/* Dashboard Tab */}
          {activeTab === 'dashboard' && (
            <>
              <h1 className="greeting">Welcome, {firstName}!</h1>
              <p className="greeting-sub">Here is your counseling overview.</p>

              <div className="stats-row">
                <div className="stat-card">
                  <div className="stat-icon">📋</div>
                  <div className="stat-value">0</div>
                  <div className="stat-label">Pending Requests</div>
                </div>
                <div className="stat-card">
                  <div className="stat-icon">✅</div>
                  <div className="stat-value">0</div>
                  <div className="stat-label">Confirmed Sessions</div>
                </div>
                <div className="stat-card">
                  <div className="stat-icon">👥</div>
                  <div className="stat-value">0</div>
                  <div className="stat-label">Total</div>
                </div>
              </div>

              <div className="quick-actions-row">
                <div className="quick-action-card" onClick={() => setActiveTab('slots')}>
                  <div className="qa-icon">📅</div>
                  <div>
                    <div className="qa-title">Create Slot</div>
                    <div className="qa-sub">Add a new available time slot</div>
                  </div>
                </div>
                <div className="quick-action-card" onClick={() => setActiveTab('requests')}>
                  <div className="qa-icon">📋</div>
                  <div>
                    <div className="qa-title">Review Requests</div>
                    <div className="qa-sub">0 Pending Appointments</div>
                  </div>
                </div>
              </div>

              <div className="section-card">
                <h3 className="section-title">Recent Pending Requests</h3>
                <div className="empty-msg">No pending requests yet.</div>
              </div>
            </>
          )}

          {/* Manage Slots Tab */}
          {activeTab === 'slots' && (
            <>
              <h1 className="greeting">Manage Slots</h1>
              <p className="greeting-sub">Create and manage your available time slots.</p>
              <div className="section-card">
                <div className="empty-msg">Slot management coming soon.</div>
              </div>
            </>
          )}

          {/* Requests Tab */}
          {activeTab === 'requests' && (
            <>
              <h1 className="greeting">Appointment Requests</h1>
              <p className="greeting-sub">Review and manage student appointment requests.</p>
              <div className="section-card">
                <div className="empty-msg">Appointment requests coming soon.</div>
              </div>
            </>
          )}

          {/* Profile Tab */}
          {activeTab === 'profile' && (
            <>
              <h1 className="greeting">Profile</h1>
              <p className="greeting-sub">View and edit your counselor profile.</p>
              <div className="section-card">
                <div className="empty-msg">Profile management coming soon.</div>
              </div>
            </>
          )}

        </div>
      </main>
    </div>
  );
}

export default CounselorDashboard;