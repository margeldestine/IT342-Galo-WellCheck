import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import StudentTopbar from '../components/StudentTopbar';
import StudentSidebar from '../components/StudentSidebar';
import '../styles/StudentDashboard.css';

const API = process.env.REACT_APP_API_URL;

function MyAppointments() {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');

  const [appointments, setAppointments] = useState([]);
  const [loadingAppointments, setLoadingAppointments] = useState(false);
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancellingId, setCancellingId] = useState(null);

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
        <StudentSidebar activeItem="appointments" />

        <main className="dashboard-main">
          <div className="dashboard-content">
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
              <div className="empty-msg">
                <div className="empty-apt-icon">📭</div>
                <p>No appointments found.</p>
                <button className="btn-book-now" onClick={() => navigate('/browse-counselors')}>
                  Book your first appointment
                </button>
              </div>
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
                        <div className="apt-counselor">
                          {apt.counselorFirstName} {apt.counselorLastName}
                        </div>
                        <div className="apt-specialization">{apt.counselorSpecialization}</div>
                        <div className="apt-time">{formatTime(apt.startTime)}</div>
                      </div>
                    </div>
                    <div className="apt-right">
                      <span className={`apt-status ${getStatusColor(apt.status)}`}>
                        {apt.status}
                      </span>
                      {apt.status === 'PENDING' && (
                        <button 
                          className="btn-cancel-apt" 
                          onClick={() => {
                            setCancellingId(apt.id);
                            setShowCancelModal(true);
                          }}
                        >
                          Cancel
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
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
              <button 
                className="btn-modal-cancel" 
                onClick={() => setShowCancelModal(false)}
              >
                Keep it
              </button>
              <button 
                className="btn-modal-delete" 
                onClick={handleCancelAppointment}
              >
                Yes, Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default MyAppointments;