import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import StudentTopbar from '../components/StudentTopbar';
import StudentSidebar from '../components/StudentSidebar';
import '../styles/StudentDashboard.css';

const API = process.env.REACT_APP_API_URL;

function StudentDashboard() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const initialTab = searchParams.get('tab') || 'dashboard';
  
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Student';
  const lastName = user.lastName || '';
  const token = localStorage.getItem('token');
  const [activeTab, setActiveTab] = useState(initialTab);

  useEffect(() => {
    const tab = searchParams.get('tab');
    if (tab && tab !== activeTab) {
      setActiveTab(tab);
    }
  }, [searchParams]);

  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setSearchParams({ tab });
  };

  const [appointments, setAppointments] = useState([]);
  const [loadingAppointments, setLoadingAppointments] = useState(false);
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    fetchAppointments();
  }, [activeTab]);

  const fetchAppointments = async () => {
    setLoadingAppointments(true);
    try {
      const res = await axios.get(`${API}/appointments/my`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setAppointments(res.data);
    } catch (err) {
      console.error('Failed to fetch appointments:', err);
    }
    setLoadingAppointments(false);
  };

  const handleCancelAppointment = async () => {
    try {
      await axios.delete(`${API}/appointments/${cancellingId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setShowCancelModal(false);
      setCancellingId(null);
      fetchAppointments();
    } catch (err) {
      alert(err.response?.data || 'Failed to cancel appointment.');
    }
  };

  const filteredAppointments = appointments.filter(a =>
    filterStatus === 'ALL' || a.status === filterStatus
  );

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good Morning';
    if (hour < 18) return 'Good Afternoon';
    return 'Good Evening';
  };

  const formatTime = (dt) => new Date(dt).toLocaleString('en-US', {
    hour: '2-digit', minute: '2-digit'
  });

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING': return 'status-pending';
      case 'CONFIRMED': return 'status-confirmed';
      case 'REJECTED': return 'status-rejected';
      case 'CANCELLED': return 'status-cancelled';
      default: return '';
    }
  };

  return (
    <div className="dashboard-layout">
      <StudentTopbar />
      <div className="dashboard-wrapper">
        <StudentSidebar activeItem={activeTab} onTabChange={handleTabChange} />

        <main className="dashboard-main">
          <div className="dashboard-content">
            {activeTab === 'dashboard' && (
              <>
                <h1 className="greeting">{getGreeting()}, {firstName}!</h1>
                <p className="greeting-sub">Here's your wellness overview for today</p>

                <div className="wellness-card">
                  <div className="wellness-header">
                    <div className="wellness-icon-wrapper">💚</div>
                    <div>
                      <div className="wellness-title">Today's Wellness Tip</div>
                      <p className="wellness-quote">"Take 5 minutes today to breathe deeply and reset your focus."</p>
                    </div>
                  </div>
                </div>

                <div className="appointment-card">
                  <h3 className="card-section-title">Upcoming Appointment</h3>
                  {appointments.filter(a => a.status === 'CONFIRMED' || a.status === 'PENDING').length === 0 ? (
                    <div className="appointment-empty">
                      <div className="empty-apt-icon">📭</div>
                      <p>No upcoming appointments.</p>
                      <button className="btn-book-now" onClick={() => navigate('/browse-counselors')}>
                        Book an appointment
                      </button>
                    </div>
                  ) : (
                    (() => {
                      const upcoming = appointments
                        .filter(a => a.status === 'CONFIRMED' || a.status === 'PENDING')
                        .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))[0];
                      return (
                        <div className="upcoming-apt-item">
                          <div className="upcoming-apt-left">
                            <div className="upcoming-apt-avatar">
                              {upcoming.counselorFirstName?.charAt(0)}{upcoming.counselorLastName?.charAt(0)}
                            </div>
                            <div className="upcoming-apt-info">
                              <div className="upcoming-apt-name">
                                {upcoming.counselorFirstName} {upcoming.counselorLastName}
                              </div>
                              <div className="upcoming-apt-spec">{upcoming.counselorSpecialization}</div>
                              <div className="upcoming-apt-meta">
                                <span>📅 {new Date(upcoming.startTime).toLocaleDateString('en-US', {
                                  month: 'long', day: 'numeric', year: 'numeric'
                                })}</span>
                                <span>🕐 {formatTime(upcoming.startTime)}</span>
                              </div>
                            </div>
                          </div>
                          <span className={`apt-status ${getStatusColor(upcoming.status)}`}>
                            {upcoming.status}
                          </span>
                        </div>
                      );
                    })()
                  )}
                </div>

                <div className="quick-actions">
                  <h3 className="card-section-title">Quick Actions</h3>
                  <div className="actions-row">
                    <button className="btn-action-primary" onClick={() => navigate('/browse-counselors')}>
                      + Book a new appointment
                    </button>
                    <button className="btn-action-secondary" onClick={() => setActiveTab('appointments')}>
                      📅 View my appointments
                    </button>
                  </div>
                </div>
              </>
            )}

            {activeTab === 'appointments' && (
              <>
                <h1 className="greeting">My Appointments</h1>
                <p className="greeting-sub">View and manage your appointments.</p>
                <div className="filter-tabs">
                  {['ALL', 'PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED'].map(status => (
                    <button
                      key={status}
                      className={`filter-tab ${filterStatus === status ? 'active' : ''}`}
                      onClick={() => setFilterStatus(status)}
                    >
                      {status}
                    </button>
                  ))}
                </div>
                {loadingAppointments ? (
                  <div className="empty-msg">Loading appointments...</div>
                ) : filteredAppointments.length === 0 ? (
                  <div className="empty-msg">No appointments found.</div>
                ) : (
                  <div className="appointments-list">
                    {filteredAppointments.map(apt => (
                      <div key={apt.id} className="apt-item">
                        <div className="apt-left">
                          <div className="apt-date-badge">
                            <span className="apt-month">
                              {new Date(apt.startTime).toLocaleString('en-US', { month: 'short' })}
                            </span>
                            <span className="apt-day">{new Date(apt.startTime).getDate()}</span>
                          </div>
                          <div className="apt-details">
                            <div className="apt-counselor">{apt.counselorFirstName} {apt.counselorLastName}</div>
                            <div className="apt-specialization">{apt.counselorSpecialization}</div>
                            <div className="apt-time">{formatTime(apt.startTime)}</div>
                          </div>
                        </div>
                        <div className="apt-right">
                          <span className={`apt-status ${getStatusColor(apt.status)}`}>{apt.status}</span>
                          {apt.status === 'PENDING' && (
                            <button className="btn-cancel-apt" onClick={() => {
                              setCancellingId(apt.id);
                              setShowCancelModal(true);
                            }}>Cancel</button>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>
        </main>
      </div>

      {showCancelModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-icon">⚠️</div>
            <h3 className="modal-title">Cancel Appointment</h3>
            <p className="modal-message">Are you sure you want to cancel this appointment?</p>
            <div className="modal-actions">
              <button className="btn-modal-cancel" onClick={() => setShowCancelModal(false)}>Keep it</button>
              <button className="btn-modal-delete" onClick={handleCancelAppointment}>Yes, Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default StudentDashboard;