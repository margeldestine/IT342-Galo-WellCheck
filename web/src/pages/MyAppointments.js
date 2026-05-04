import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import StudentSidebar from '../components/StudentSidebar';
import '../styles/MyAppointments.css';

const API = process.env.REACT_APP_API_URL;

function MyAppointments() {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');

  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    fetchAppointments();
  }, []);

  const fetchAppointments = async () => {
    setLoading(true);
    try {
      const res = await axios.get(`${API}/appointments/my`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setAppointments(res.data);
    } catch (err) {
      console.error('Failed to fetch appointments:', err);
    }
    setLoading(false);
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

  const filtered = appointments.filter(a =>
    filterStatus === 'ALL' || a.status === filterStatus
  );

  const getMonth = (dt) =>
    new Date(dt).toLocaleString('en-US', { month: 'short' }).toUpperCase();

  const getDay = (dt) => new Date(dt).getDate();

  const formatTime = (dt) =>
    new Date(dt).toLocaleString('en-US', { hour: '2-digit', minute: '2-digit' });

  const statusLabel = (s) =>
    s.charAt(0).toUpperCase() + s.slice(1).toLowerCase();

  const statusClass = {
    PENDING:   'ma-badge-pending',
    CONFIRMED: 'ma-badge-confirmed',
    REJECTED:  'ma-badge-rejected',
    CANCELLED: 'ma-badge-cancelled',
  };

  const tabs = ['ALL', 'PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED'];

  return (
    <div className="ma-root">
      <StudentSidebar activeItem="appointments" />

      <main className="ma-main">
        <div className="ma-inner">

          {/* ── Header ── */}
          <div className="ma-header">
            <div>
              <h1 className="ma-title">My Appointments</h1>
              <p className="ma-sub">View and manage your counseling sessions.</p>
            </div>
            <button className="ma-btn-book" onClick={() => navigate('/browse-counselors')}>
              + Book new session
            </button>
          </div>

          {/* ── Filter tabs ── */}
          <div className="ma-tabs">
            {tabs.map(t => (
              <button
                key={t}
                className={`ma-tab ${filterStatus === t ? 'ma-tab-active' : ''}`}
                onClick={() => setFilterStatus(t)}
              >
                {t === 'ALL' ? 'All' : statusLabel(t)}
              </button>
            ))}
          </div>

          {/* ── Content ── */}
          {loading ? (
            <div className="ma-loading">Loading appointments…</div>
          ) : filtered.length === 0 ? (
            <div className="ma-empty">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" strokeWidth="1.4" className="ma-empty-icon">
                <rect x="3" y="4" width="18" height="18" rx="3"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
              </svg>
              <p className="ma-empty-title">No appointments found</p>
              <small className="ma-empty-sub">Try a different filter or book a new session.</small>
            </div>
          ) : (
            <div className="ma-list">
              {filtered.map(apt => (
                <div key={apt.id} className={`ma-card ${apt.status === 'REJECTED' && apt.rejectionReason ? 'ma-card-has-reason' : ''}`}>

                  {/* Date badge */}
                  <div className="ma-date">
                    <span className="ma-date-month">{getMonth(apt.startTime)}</span>
                    <span className="ma-date-day">{getDay(apt.startTime)}</span>
                  </div>

                  {/* Info */}
                  <div className="ma-info">
                    <div className="ma-counselor">
                      {apt.counselorFirstName} {apt.counselorLastName}
                    </div>
                    <div className="ma-spec">{apt.counselorSpecialization}</div>
                    <div className="ma-time">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none"
                        stroke="currentColor" strokeWidth="2">
                        <circle cx="12" cy="12" r="9"/>
                        <polyline points="12 7 12 12 15 15"/>
                      </svg>
                      {formatTime(apt.startTime)}
                    </div>

                    {/* Rejection reason — shown only for rejected appointments */}
                    {apt.status === 'REJECTED' && apt.rejectionReason && (
                      <div className="ma-rejection-reason">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"
                          stroke="currentColor" strokeWidth="2">
                          <circle cx="12" cy="12" r="9"/>
                          <line x1="12" y1="8" x2="12" y2="12"/>
                          <line x1="12" y1="16" x2="12.01" y2="16"/>
                        </svg>
                        <span><strong>Reason: </strong>{apt.rejectionReason}</span>
                      </div>
                    )}
                  </div>

                  {/* Right: badge + cancel */}
                  <div className="ma-actions">
                    <span className={`ma-badge ${statusClass[apt.status] || ''}`}>
                      {statusLabel(apt.status)}
                    </span>
                    {apt.status === 'PENDING' && (
                      <button
                        className="ma-btn-cancel"
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

      {/* ── Cancel Modal ── */}
      {showCancelModal && (
        <div
          className="ma-overlay"
          onClick={(e) => e.target === e.currentTarget && setShowCancelModal(false)}
        >
          <div className="ma-modal">
            <div className="ma-modal-head">
              <div>
                <div className="ma-modal-title">Cancel Appointment</div>
                <div className="ma-modal-sub">This action cannot be undone.</div>
              </div>
              <button className="ma-modal-x" onClick={() => setShowCancelModal(false)}>✕</button>
            </div>
            <p className="ma-modal-msg">
              Are you sure you want to cancel this appointment?
            </p>
            <div className="ma-modal-foot">
              <button className="ma-btn-keep" onClick={() => setShowCancelModal(false)}>
                Keep it
              </button>
              <button className="ma-btn-confirm-cancel" onClick={handleCancelAppointment}>
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