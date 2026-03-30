import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/AdminDashboard.css';

const API = process.env.REACT_APP_API_URL;

function AdminDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const token = localStorage.getItem('token');

  const [stats, setStats] = useState(null);
  const [counselors, setCounselors] = useState([]);
  const [activeTab, setActiveTab] = useState('dashboard');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const authHeader = { headers: { Authorization: `Bearer ${token}` } };

  useEffect(() => {
    fetchStats();
    fetchCounselors();
  }, []);

  const fetchStats = async () => {
    try {
      const res = await axios.get(`${API}/admin/dashboard`, authHeader);
      setStats(res.data);
    } catch (err) {
      console.error('Failed to fetch stats');
    }
  };

  const fetchCounselors = async () => {
    try {
      const res = await axios.get(`${API}/admin/counselors`, authHeader);
      setCounselors(res.data);
    } catch (err) {
      console.error('Failed to fetch counselors');
    }
  };

  const handleApprove = async (id) => {
    setLoading(true);
    try {
      await axios.put(`${API}/admin/counselors/${id}/approve`, {}, authHeader);
      setMessage('Counselor approved successfully!');
      fetchCounselors();
      fetchStats();
    } catch (err) {
      setMessage('Failed to approve counselor.');
    }
    setLoading(false);
    setTimeout(() => setMessage(''), 3000);
  };

  const handleReject = async (id) => {
    if (!window.confirm('Are you sure you want to reject this counselor?')) return;
    setLoading(true);
    try {
      await axios.put(`${API}/admin/counselors/${id}/reject`, {}, authHeader);
      setMessage('Counselor rejected.');
      fetchCounselors();
      fetchStats();
    } catch (err) {
      setMessage('Failed to reject counselor.');
    }
    setLoading(false);
    setTimeout(() => setMessage(''), 3000);
  };

  const handleDeactivate = async (id) => {
    if (!window.confirm('Are you sure you want to deactivate this counselor?')) return;
    setLoading(true);
    try {
      await axios.put(`${API}/admin/counselors/${id}/deactivate`, {}, authHeader);
      setMessage('Counselor deactivated.');
      fetchCounselors();
      fetchStats();
    } catch (err) {
      setMessage('Failed to deactivate counselor.');
    }
    setLoading(false);
    setTimeout(() => setMessage(''), 3000);
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  const pendingCounselors = counselors.filter(c => c.status === 'PENDING');
  const activeCounselors = counselors.filter(c => c.status === 'ACTIVE');

  return (
    <div className="admin-wrapper">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="navbar-logo">♥</div>
          <div>
            <div className="navbar-title">WellCheck</div>
            <div className="sidebar-subtitle">Admin Portal</div>
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
            className={`nav-item ${activeTab === 'counselors' ? 'active' : ''}`}
            onClick={() => setActiveTab('counselors')}
          >
            <span className="nav-icon">👤</span> Counselors
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
            <span className="topbar-name">Admin</span>
            <div className="topbar-avatar">A</div>
          </div>
        </div>

        <div className="dashboard-content">

          {/* Toast message */}
          {message && <div className="toast-msg">{message}</div>}

          {/* Dashboard Tab */}
          {activeTab === 'dashboard' && (
            <>
              <h1 className="greeting">Dashboard</h1>
              <p className="greeting-sub">System overview and management</p>

              <div className="stats-grid">
                <div className="stat-card">
                  <div className="stat-icon">👤</div>
                  <div className="stat-label">Total Students</div>
                  <div className="stat-value">{stats?.totalStudents ?? '—'}</div>
                  <div className="stat-sub">Registered accounts</div>
                </div>
                <div className="stat-card">
                  <div className="stat-icon">👥</div>
                  <div className="stat-label">Total Counselors</div>
                  <div className="stat-value">{stats?.totalCounselors ?? '—'}</div>
                  <div className="stat-sub">Active counselors</div>
                </div>
                <div className="stat-card highlight">
                  <div className="stat-icon">⏳</div>
                  <div className="stat-label">Pending Approvals</div>
                  <div className="stat-value">{stats?.pendingCounselorApprovals ?? '—'}</div>
                  <div className="stat-sub">Awaiting review</div>
                </div>
                <div className="stat-card">
                  <div className="stat-icon">📅</div>
                  <div className="stat-label">Total Appointments</div>
                  <div className="stat-value">{stats?.totalAppointments ?? '—'}</div>
                  <div className="stat-sub">All time</div>
                </div>
              </div>

              {/* Pending approvals preview */}
              {pendingCounselors.length > 0 && (
                <div className="section-card">
                  <h3 className="section-title">Pending Counselor Approvals</h3>
                  {pendingCounselors.map(c => (
                    <div className="counselor-row" key={c.id}>
                      <div className="counselor-avatar">{c.firstName.charAt(0)}{c.lastName.charAt(0)}</div>
                      <div className="counselor-info">
                        <div className="counselor-name">{c.firstName} {c.lastName}</div>
                        <div className="counselor-meta">{c.email} · {c.specialization}</div>
                      </div>
                      <div className="counselor-actions">
                        <button className="btn-approve" onClick={() => handleApprove(c.id)} disabled={loading}>
                          Approve
                        </button>
                        <button className="btn-reject" onClick={() => handleReject(c.id)} disabled={loading}>
                          Reject
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}

          {/* Counselors Tab */}
          {activeTab === 'counselors' && (
            <>
              <h1 className="greeting">Counselors</h1>
              <p className="greeting-sub">Manage counselor accounts and approvals</p>

              {/* Pending */}
              {pendingCounselors.length > 0 && (
                <div className="section-card">
                  <h3 className="section-title">Pending Approval ({pendingCounselors.length})</h3>
                  {pendingCounselors.map(c => (
                    <div className="counselor-row" key={c.id}>
                      <div className="counselor-avatar">{c.firstName.charAt(0)}{c.lastName.charAt(0)}</div>
                      <div className="counselor-info">
                        <div className="counselor-name">{c.firstName} {c.lastName}</div>
                        <div className="counselor-meta">{c.email}</div>
                        <div className="counselor-meta">{c.employeeNumber} · {c.specialization}</div>
                      </div>
                      <div className="badge-pending">Pending</div>
                      <div className="counselor-actions">
                        <button className="btn-approve" onClick={() => handleApprove(c.id)} disabled={loading}>
                          Approve
                        </button>
                        <button className="btn-reject" onClick={() => handleReject(c.id)} disabled={loading}>
                          Reject
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* Active */}
              <div className="section-card">
                <h3 className="section-title">Active Counselors ({activeCounselors.length})</h3>
                {activeCounselors.length === 0 && (
                  <p className="empty-msg">No active counselors yet.</p>
                )}
                {activeCounselors.map(c => (
                  <div className="counselor-row" key={c.id}>
                    <div className="counselor-avatar">{c.firstName.charAt(0)}{c.lastName.charAt(0)}</div>
                    <div className="counselor-info">
                      <div className="counselor-name">{c.firstName} {c.lastName}</div>
                      <div className="counselor-meta">{c.email}</div>
                      <div className="counselor-meta">{c.employeeNumber} · {c.specialization}</div>
                    </div>
                    <div className="badge-active">Active</div>
                    <div className="counselor-actions">
                      <button className="btn-deactivate" onClick={() => handleDeactivate(c.id)} disabled={loading}>
                        Deactivate
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}

        </div>
      </main>
    </div>
  );
}

export default AdminDashboard;