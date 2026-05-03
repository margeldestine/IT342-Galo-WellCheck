import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Calendar, Clock, Inbox, Heart } from 'lucide-react';
import StudentSidebar from '../components/StudentSidebar';
import '../styles/StudentDashboard.css';

const API = process.env.REACT_APP_API_URL;

const wellnessTips = [
  "Take 5 minutes today to breathe deeply and reset your focus.",
  "Remember to drink water and take short breaks between tasks.",
  "Talking to someone about how you feel is a sign of strength.",
  "A short walk outside can do wonders for your mental clarity.",
  "You don't have to have it all figured out. Take it one day at a time."
];

const moods = [
  { label: 'Awful', emoji: '😣' },
  { label: 'Low',   emoji: '😟' },
  { label: 'Okay',  emoji: '😐' },
  { label: 'Good',  emoji: '🙂' },
  { label: 'Great', emoji: '😊' },
];

const getMoodResponse = (mood) => {
  if (mood === 'Awful' || mood === 'Low') {
    return {
      type: 'low',
      message: "We're sorry to hear that. It's okay not to be okay.",
      sub: "Would you like to talk to a counselor today?",
      cta: 'Book a session',
      ctaPath: '/browse-counselors',
    };
  }
  if (mood === 'Okay') {
    return {
      type: 'okay',
      message: "Hang in there — you're doing okay, and that's enough.",
      sub: "A counselor is always here if you need to talk.",
      cta: 'Browse counselors',
      ctaPath: '/browse-counselors',
    };
  }
  return {
    type: 'good',
    message: "That's wonderful to hear! Keep taking care of yourself. 💚",
    sub: null,
    cta: null,
    ctaPath: null,
  };
};

function StudentDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Student';
  const token = localStorage.getItem('token');

  const [appointments, setAppointments] = useState([]);
  const [counselors, setCounselors] = useState([]);
  const [loadingAppointments, setLoadingAppointments] = useState(false);
  const [selectedMood, setSelectedMood] = useState(null);

  const tip = wellnessTips[new Date().getDay() % wellnessTips.length];
  const moodResponse = selectedMood ? getMoodResponse(selectedMood) : null;

  // Auto-dismiss after 2s for Good / Great
  useEffect(() => {
    if (selectedMood === 'Good' || selectedMood === 'Great') {
      const timer = setTimeout(() => setSelectedMood(null), 2000);
      return () => clearTimeout(timer);
    }
  }, [selectedMood]);

  useEffect(() => {
    fetchAppointments();
    fetchCounselors();
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

  const fetchCounselors = async () => {
    try {
      const res = await axios.get(`${API}/counselors`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setCounselors(res.data.slice(0, 3));
    } catch (err) {
      console.error('Failed to fetch counselors:', err);
    }
  };

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  };

  const formatTime = (dt) => new Date(dt).toLocaleString('en-US', {
    hour: '2-digit', minute: '2-digit'
  });

  const formatDate = (dt) => new Date(dt).toLocaleDateString('en-US', {
    month: 'long', day: 'numeric', year: 'numeric'
  });

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':   return 'sd-status-pending';
      case 'CONFIRMED': return 'sd-status-confirmed';
      case 'REJECTED':  return 'sd-status-rejected';
      case 'CANCELLED': return 'sd-status-cancelled';
      default: return '';
    }
  };

  const upcomingAppointments = appointments
    .filter(a => a.status === 'CONFIRMED' || a.status === 'PENDING')
    .sort((a, b) => new Date(a.startTime) - new Date(b.startTime));

  const upcoming = upcomingAppointments[0];

  return (
    <div className="sd-layout">
      <StudentSidebar activeItem="dashboard" />
      <main className="sd-main">
        <div className="sd-content">

          {/* ── Header ── */}
          <div className="sd-page-header">
            <div>
              <h1 className="sd-greeting">{getGreeting()}, {firstName}.</h1>
              <p className="sd-greeting-sub">Here's your wellness overview for today</p>
            </div>
            <div className="sd-date-chip">
              {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}
            </div>
          </div>

          {/* ── Wellness tip ── */}
          <div className="sd-wellness-card">
            <div className="sd-wellness-icon">
              <Heart size={16} fill="#4caf82" color="#4caf82" />
            </div>
            <div>
              <div className="sd-wellness-tag">Wellness Tip</div>
              <div className="sd-wellness-quote">{tip}</div>
            </div>
          </div>

          {/* ── Two col ── */}
          <div className="sd-two-col">

            {/* Upcoming appointment */}
            <div className="sd-card">
              <div className="sd-card-label">Upcoming Appointment</div>
              {loadingAppointments ? (
                <div className="sd-empty"><p>Loading...</p></div>
              ) : !upcoming ? (
                <div className="sd-empty">
                  <Inbox size={28} color="#ccc" />
                  <p className="sd-empty-text">No upcoming appointments</p>
                  <p className="sd-empty-sub">Book a session to get started</p>
                </div>
              ) : (
                <div className="sd-apt-item">
                  <div className="sd-apt-left">
                    <div className="sd-apt-avatar">
                      {upcoming.counselorFirstName?.charAt(0)}{upcoming.counselorLastName?.charAt(0)}
                    </div>
                    <div className="sd-apt-info">
                      <div className="sd-apt-name">
                        {upcoming.counselorFirstName} {upcoming.counselorLastName}
                      </div>
                      <div className="sd-apt-spec">{upcoming.counselorSpecialization}</div>
                      <div className="sd-apt-meta">
                        <span className="sd-meta-item">
                          <Calendar size={12} /> {formatDate(upcoming.startTime)}
                        </span>
                        <span className="sd-meta-item">
                          <Clock size={12} /> {formatTime(upcoming.startTime)}
                        </span>
                      </div>
                    </div>
                  </div>
                  <span className={`sd-apt-status ${getStatusColor(upcoming.status)}`}>
                    {upcoming.status}
                  </span>
                </div>
              )}
            </div>

            {/* Mood + Quick actions */}
            <div className="sd-card">
              <div className="sd-card-label">How are you feeling today?</div>

              <div className="sd-mood-row">
                {moods.map((mood) => (
                  <button
                    key={mood.label}
                    className={`sd-mood-btn ${selectedMood === mood.label ? 'selected' : ''}`}
                    onClick={() => setSelectedMood(mood.label)}
                  >
                    <span className="sd-mood-emoji">{mood.emoji}</span>
                    <span className="sd-mood-label">{mood.label}</span>
                  </button>
                ))}
              </div>

              {/* ── Mood response ── */}
              {moodResponse && (
                <div className={`sd-mood-response sd-mood-response-${moodResponse.type}`}>
                  <div className="sd-mood-response-header">
                    <p className="sd-mood-response-msg">{moodResponse.message}</p>
                    {moodResponse.type !== 'good' && (
                      <button
                        className="sd-mood-response-dismiss"
                        onClick={() => setSelectedMood(null)}
                      >
                        ×
                      </button>
                    )}
                  </div>
                  {moodResponse.sub && (
                    <p className="sd-mood-response-sub">{moodResponse.sub}</p>
                  )}
                  {moodResponse.cta && (
                    <button
                      className="sd-mood-response-btn"
                      onClick={() => navigate(moodResponse.ctaPath)}
                    >
                      {moodResponse.cta}
                    </button>
                  )}
                </div>
              )}

              {/* Only show quick actions if no mood selected */}
              {!selectedMood && (
                <>
                  <div className="sd-mood-divider" />
                  <div className="sd-card-label" style={{ marginTop: '16px' }}>Quick Actions</div>
                  <div className="sd-quick-row">
                    <button className="sd-quick-btn-primary" onClick={() => navigate('/browse-counselors')}>
                      + Book a new appointment
                    </button>
                    <button className="sd-quick-btn-secondary" onClick={() => navigate('/my-appointments')}>
                      <Calendar size={14} /> View my appointments
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>

          {/* ── Available Counselors ── */}
          <div className="sd-card">
            <div className="sd-card-label-row">
              <div className="sd-card-label">Available Counselors</div>
              <button className="sd-btn-view-all" onClick={() => navigate('/browse-counselors')}>
                View all
              </button>
            </div>
            {counselors.length === 0 ? (
              <div className="sd-empty">
                <p className="sd-empty-sub">No counselors available right now</p>
              </div>
            ) : (
              <div className="sd-counselor-list">
                {counselors.map((c) => (
                  <div key={c.id} className="sd-counselor-row">
                    <div className="sd-counselor-avatar">
                      {c.firstName?.charAt(0)}{c.lastName?.charAt(0)}
                    </div>
                    <div className="sd-counselor-info">
                      <div className="sd-counselor-name">{c.firstName} {c.lastName}</div>
                      <div className="sd-counselor-spec">{c.specialization}</div>
                    </div>
                    <div className="sd-counselor-status">
                      <span className="sd-available-dot" />
                      Available
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

        </div>
      </main>
    </div>
  );
}

export default StudentDashboard;