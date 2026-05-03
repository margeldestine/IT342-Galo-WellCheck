import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import { ArrowLeft, Calendar, Clock, Star, MapPin, BookOpen } from 'lucide-react';
import StudentSidebar from '../components/StudentSidebar';
import '../styles/Counselorview.css';

const API = process.env.REACT_APP_API_URL;

function CounselorView() {
  const { id } = useParams();
  const navigate = useNavigate();
  const token = localStorage.getItem('token');

  const [counselor, setCounselor] = useState(null);
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [slotsLoading, setSlotsLoading] = useState(true);
  const [selectedDay, setSelectedDay] = useState(null);
  const [selectedSlot, setSelectedSlot] = useState(null);

  useEffect(() => {
    fetchCounselor();
    fetchSlots();
  }, [id]);

  const fetchCounselor = async () => {
    setLoading(true);
    try {
      const res = await axios.get(`${API}/counselors/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setCounselor(res.data);
    } catch (err) {
      console.error('Failed to fetch counselor:', err);
    }
    setLoading(false);
  };

  const fetchSlots = async () => {
    setSlotsLoading(true);
    try {
      const res = await axios.get(`${API}/slots/counselor/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setSlots(res.data.sort((a, b) => new Date(a.startTime) - new Date(b.startTime)));
    } catch (err) {
      console.error('Failed to fetch slots:', err);
    }
    setSlotsLoading(false);
  };

  // Group slots by "Monday|May 6"
  const slotsByDay = slots.reduce((acc, slot) => {
    const date = new Date(slot.startTime);
    const dayName = date.toLocaleDateString('en-US', { weekday: 'long' });
    const monthDay = date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    const key = `${dayName}|${monthDay}`;
    if (!acc[key]) acc[key] = [];
    acc[key].push(slot);
    return acc;
  }, {});

  const availableDayKeys = Object.keys(slotsByDay);

  const formatTime = (dt) =>
    new Date(dt).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });

  const getInitials = (c) =>
    `${c?.firstName?.charAt(0) || ''}${c?.lastName?.charAt(0) || ''}`;

  const handleSlotClick = (slot) => {
    localStorage.setItem('selectedSlot', JSON.stringify(slot));
    localStorage.setItem('selectedCounselor', JSON.stringify(counselor));
    navigate('/book-appointment');
  };

  if (loading) {
    return (
      <div className="cv-layout">
        <StudentSidebar activeItem="browse-counselors" />
        <main className="cv-main">
          <div className="cv-loading">Loading counselor profile…</div>
        </main>
      </div>
    );
  }

  if (!counselor) {
    return (
      <div className="cv-layout">
        <StudentSidebar activeItem="browse-counselors" />
        <main className="cv-main">
          <div className="cv-loading">Counselor not found.</div>
        </main>
      </div>
    );
  }

  return (
    <div className="cv-layout">
      <StudentSidebar activeItem="browse-counselors" />
      <main className="cv-main">
        <div className="cv-content">

          <button className="cv-back-btn" onClick={() => navigate(-1)}>
            <ArrowLeft size={14} /> Back
          </button>

          <div className="cv-grid">

            {/* ── Left: Profile card ── */}
            <div className="cv-profile-card">

              <div className="cv-avatar-wrap">
                {counselor.profilePhoto ? (
                  <img src={counselor.profilePhoto} alt="Profile" className="cv-avatar-img" />
                ) : (
                  <div className="cv-avatar-initials">{getInitials(counselor)}</div>
                )}
              </div>

              <span className="cv-available-badge">● Available</span>

              <h2 className="cv-name">{counselor.firstName} {counselor.lastName}</h2>

              <div className="cv-meta-pills">
                {counselor.specialization && (
                  <span className="cv-pill"><BookOpen size={12} /> {counselor.specialization}</span>
                )}
                {counselor.location && (
                  <span className="cv-pill"><MapPin size={12} /> {counselor.location}</span>
                )}
              </div>

              <div className="cv-divider" />

              <div className="cv-stat-row">
                <div className="cv-stat">
                  <div className="cv-stat-val">
                    <Star size={14} fill="#f59e0b" color="#f59e0b" /> 4.9
                  </div>
                  <div className="cv-stat-lbl">Rating</div>
                </div>
                <div className="cv-stat">
                  <div className="cv-stat-val">3 yrs</div>
                  <div className="cv-stat-lbl">Experience</div>
                </div>
              </div>

              <div className="cv-divider" />

              <div className="cv-bio-section">
                <div className="cv-section-label">About</div>
                <p className="cv-bio">
                  {counselor.bio ||
                    `${counselor.firstName} is a dedicated counselor specializing in ${counselor.specialization}. They provide a safe, non-judgmental space for students to explore their concerns and develop strategies for wellbeing.`}
                </p>
              </div>
            </div>

            {/* ── Right: Booking panel ── */}
            <div className="cv-booking-panel">

              {/* Day selector */}
              <div className="cv-panel-card">
                <div className="cv-panel-section-label">
                  <Calendar size={13} /> Select a Day
                </div>

                {slotsLoading ? (
                  <p className="cv-no-slots">Loading available slots…</p>
                ) : availableDayKeys.length === 0 ? (
                  <p className="cv-no-slots">No available days at the moment.</p>
                ) : (
                  <div className="cv-day-grid">
                    {availableDayKeys.map((key) => {
                      const [dayName, monthDay] = key.split('|');
                      return (
                        <button
                          key={key}
                          className={`cv-day-btn ${selectedDay === key ? 'selected' : ''}`}
                          onClick={() => { setSelectedDay(key); setSelectedSlot(null); }}
                        >
                          <span className="cv-day-name">{dayName.slice(0, 3)}</span>
                          <span className="cv-day-date">{monthDay}</span>
                        </button>
                      );
                    })}
                  </div>
                )}
              </div>

              {/* Time slots */}
              {selectedDay && (
                <div className="cv-panel-card">
                  <div className="cv-panel-section-label">
                    <Clock size={13} />
                    {selectedDay.split('|')[0]}, {selectedDay.split('|')[1]}
                  </div>
                  <div className="cv-slot-grid">
                    {slotsByDay[selectedDay].map((slot) => (
                      <button
                        key={slot.id}
                        className={`cv-slot-btn ${selectedSlot === slot.id ? 'selected' : ''}`}
                        onClick={() => { setSelectedSlot(slot.id); handleSlotClick(slot); }}
                      >
                        {formatTime(slot.startTime)}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {!selectedDay && !slotsLoading && availableDayKeys.length > 0 && (
                <div className="cv-panel-hint">
                  <Calendar size={28} color="#ccc" />
                  <p>Select a day to see available times<br />with {counselor.firstName}.</p>
                </div>
              )}

            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default CounselorView;