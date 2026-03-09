import { useNavigate } from 'react-router-dom';
import '../styles/StudentDashboard.css';

function StudentDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Student';
  const lastName = user.lastName || '';

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good Morning';
    if (hour < 18) return 'Good Afternoon';
    return 'Good Evening';
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  return (
    <div className="dashboard-wrapper">

      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="navbar-logo">♥</div>
          <div>
            <div className="navbar-title">WellCheck</div>
            <div className="sidebar-subtitle">Student Portal</div>
          </div>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-item active">
            <span className="nav-icon">⊞</span> Dashboard
          </div>
          <div className="nav-item">
            <span className="nav-icon">👤</span> Browse Counselors
          </div>
          <div className="nav-item">
            <span className="nav-icon">📅</span> My Appointments
          </div>
          <div className="nav-item">
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
          <h1 className="greeting">{getGreeting()}, {firstName} {lastName}!</h1>
          <p className="greeting-sub">Here's your wellness overview for today</p>

          {/* Wellness Tip */}
          <div className="wellness-card">
            <div className="wellness-header">
              <span className="wellness-icon">👤</span>
              <span className="wellness-title">Today's Wellness Tip</span>
              <span className="wellness-arrow">↑</span>
            </div>
            <p className="wellness-quote">"Take 5 minutes today to breathe deeply and reset your focus."</p>
          </div>

          {/* Upcoming Appointment */}
          <div className="appointment-card">
            <h3 className="card-section-title">Upcoming Appointment</h3>
            <div className="appointment-empty">
              <p>No upcoming appointments.</p>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="quick-actions">
            <h3 className="card-section-title">Quick Actions</h3>
            <div className="actions-row">
             <button className="btn-action-primary">&#43; Book a new appointment</button>
              <button className="btn-action-secondary">📅 View my appointments</button>
            </div>
          </div>
        </div>
      </main>

    </div>
  );
}

export default StudentDashboard;