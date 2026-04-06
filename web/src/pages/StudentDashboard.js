import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import StudentTopbar from '../components/StudentTopbar';
import StudentSidebar from '../components/StudentSidebar';
import '../styles/StudentDashboard.css';

const API = process.env.REACT_APP_API_URL;

function StudentDashboard() {
  const navigate = useNavigate();
  
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Student';
  const token = localStorage.getItem('token');

  const [appointments, setAppointments] = useState([]);
  const [loadingAppointments, setLoadingAppointments] = useState(false);

  useEffect(() => {
    fetchAppointments();
  }, []);

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
        <StudentSidebar activeItem="dashboard" />

        <main className="dashboard-main">
          <div className="dashboard-content">
            <h1 className="greeting">{getGreeting()}, {firstName}!</h1>
            <p className="greeting-sub">Here's your wellness overview for today</p>

            <div className="wellness-card">
              <div className="wellness-header">
                <div className="wellness-icon-wrapper">💚</div>
                <div>
                  <div className="wellness-title">Today's Wellness Tip</div>
                  <p className="wellness-quote">
                    "Take 5 minutes today to breathe deeply and reset your focus."
                  </p>
                </div>
              </div>
            </div>

            <div className="appointment-card">
              <h3 className="card-section-title">Upcoming Appointment</h3>
              {loadingAppointments ? (
                <div className="appointment-empty">
                  <p>Loading...</p>
                </div>
              ) : appointments.filter(a => a.status === 'CONFIRMED' || a.status === 'PENDING').length === 0 ? (
                <div className="appointment-empty">
                  <div className="empty-apt-icon">📭</div>
                  <p>No upcoming appointments.</p>
                  <button 
                    className="btn-book-now" 
                    onClick={() => navigate('/browse-counselors')}
                  >
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
                          <div className="upcoming-apt-spec">
                            {upcoming.counselorSpecialization}
                          </div>
                          <div className="upcoming-apt-meta">
                            <span>
                              📅 {new Date(upcoming.startTime).toLocaleDateString('en-US', {
                                month: 'long', day: 'numeric', year: 'numeric'
                              })}
                            </span>
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
                <button 
                  className="btn-action-primary" 
                  onClick={() => navigate('/browse-counselors')}
                >
                  + Book a new appointment
                </button>
                <button 
                  className="btn-action-secondary" 
                  onClick={() => navigate('/my-appointments')}
                >
                  📅 View my appointments
                </button>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}

export default StudentDashboard;