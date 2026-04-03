import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/StudentDashboard.css';

const API = process.env.REACT_APP_API_URL;

function StudentDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Student';
  const lastName = user.lastName || '';
  const token = localStorage.getItem('token');
  const [activeTab, setActiveTab] = useState('dashboard');

  const [counselors, setCounselors] = useState([]);
  const [loadingCounselors, setLoadingCounselors] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedSpecialization, setSelectedSpecialization] = useState('');

  useEffect(() => {
    if (activeTab === 'counselors') fetchCounselors();
  }, [activeTab]);

  const fetchCounselors = async () => {
    setLoadingCounselors(true);
    try {
      const res = await axios.get(`${API}/counselors`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setCounselors(res.data);
    } catch (err) {
      console.error('Failed to fetch counselors:', err);
    }
    setLoadingCounselors(false);
  };

  const filteredCounselors = counselors.filter(c => {
    const fullName = `${c.firstName} ${c.lastName}`.toLowerCase();
    const matchesSearch = fullName.includes(searchQuery.toLowerCase());
    const matchesSpec = selectedSpecialization === '' || c.specialization === selectedSpecialization;
    return matchesSearch && matchesSpec;
  });

  const specializations = [...new Set(counselors.map(c => c.specialization))];

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

      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="navbar-logo">♥</div>
          <div>
            <div className="navbar-title">WellCheck</div>
            <div className="sidebar-subtitle">Student Portal</div>
          </div>
        </div>

        <nav className="sidebar-nav">
          <div className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`} onClick={() => setActiveTab('dashboard')}>
            <span className="nav-icon">🏠</span> Dashboard
          </div>
          <div className={`nav-item ${activeTab === 'counselors' ? 'active' : ''}`} onClick={() => setActiveTab('counselors')}>
            <span className="nav-icon">👥</span> Browse Counselors
          </div>
          <div className={`nav-item ${activeTab === 'appointments' ? 'active' : ''}`} onClick={() => setActiveTab('appointments')}>
            <span className="nav-icon">📅</span> My Appointments
          </div>
          <div className={`nav-item ${activeTab === 'profile' ? 'active' : ''}`} onClick={() => setActiveTab('profile')}>
            <span className="nav-icon">👤</span> Profile
          </div>
        </nav>

        <div className="sidebar-logout" onClick={handleLogout}>
          <span className="nav-icon">↪</span> Log Out
        </div>
      </aside>

      <main className="dashboard-main">
        <div className="topbar">
          <div />
          <div className="topbar-user">
            <span className="topbar-name">{firstName} {lastName}</span>
            <div className="topbar-avatar">{firstName.charAt(0)}</div>
          </div>
        </div>

        <div className="dashboard-content">

          {activeTab === 'dashboard' && (
            <>
              <h1 className="greeting">{getGreeting()}, {firstName} {lastName}!</h1>
              <p className="greeting-sub">Here's your wellness overview for today</p>

              <div className="wellness-card">
                <div className="wellness-header">
                  <span className="wellness-icon">💚</span>
                  <span className="wellness-title">Today's Wellness Tip</span>
                </div>
                <p className="wellness-quote">"Take 5 minutes today to breathe deeply and reset your focus."</p>
              </div>

              <div className="appointment-card">
                <h3 className="card-section-title">Upcoming Appointment</h3>
                <div className="appointment-empty">
                  <p>No upcoming appointments.</p>
                </div>
              </div>

              <div className="quick-actions">
                <h3 className="card-section-title">Quick Actions</h3>
                <div className="actions-row">
                  <button className="btn-action-primary" onClick={() => setActiveTab('counselors')}>
                    + Book a new appointment
                  </button>
                  <button className="btn-action-secondary" onClick={() => setActiveTab('appointments')}>
                    📅 View my appointments
                  </button>
                </div>
              </div>
            </>
          )}

          {activeTab === 'counselors' && (
            <>
              <h1 className="greeting">Browse Counselors</h1>
              <p className="greeting-sub">Find and book a session with a guidance counselor.</p>

              <div className="counselors-toolbar">
                <input
                  className="counselor-search"
                  type="text"
                  placeholder="🔍 Search by name..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
                <select
                  className="counselor-filter"
                  value={selectedSpecialization}
                  onChange={(e) => setSelectedSpecialization(e.target.value)}
                >
                  <option value="">All Specializations</option>
                  {specializations.map(spec => (
                    <option key={spec} value={spec}>{spec}</option>
                  ))}
                </select>
              </div>

              {loadingCounselors ? (
                <div className="empty-msg">Loading counselors...</div>
              ) : filteredCounselors.length === 0 ? (
                <div className="empty-msg">No counselors found.</div>
              ) : (
                <div className="counselors-grid">
                  {filteredCounselors.map(counselor => (
                    <div key={counselor.id} className="counselor-card">
                      <div className="counselor-card-header">
                        <div className="counselor-avatar">
                          {counselor.firstName.charAt(0)}{counselor.lastName.charAt(0)}
                        </div>
                        <div className="counselor-header-info">
                          <div className="counselor-name">
                            {counselor.firstName} {counselor.lastName}
                          </div>
                          <div className="counselor-specialization">
                            {counselor.specialization}
                          </div>
                        </div>
                      </div>
                      <p className="counselor-bio">
                        {counselor.bio || 'No bio available.'}
                      </p>
                      <div className="counselor-slots-count">
                        📅 {counselor.availableSlots} available slot{counselor.availableSlots !== 1 ? 's' : ''}
                      </div>
                      <button className="btn-book-slot">
                        View available slots
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}

          {activeTab === 'appointments' && (
            <>
              <h1 className="greeting">My Appointments</h1>
              <p className="greeting-sub">View and manage your appointments.</p>
              <div className="appointment-card">
                <div className="appointment-empty">
                  <p>Appointments coming soon.</p>
                </div>
              </div>
            </>
          )}

          {activeTab === 'profile' && (
            <>
              <h1 className="greeting">Profile</h1>
              <p className="greeting-sub">View and edit your profile.</p>
              <div className="appointment-card">
                <div className="appointment-empty">
                  <p>Profile management coming soon.</p>
                </div>
              </div>
            </>
          )}

        </div>
      </main>
    </div>
  );
}

export default StudentDashboard;