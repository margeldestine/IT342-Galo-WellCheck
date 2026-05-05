import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import { ArrowLeft, Calendar, Clock, Star, BookOpen, Award, Shield } from 'lucide-react';
import StudentSidebar from '../../components/StudentSidebar'; 
import './Counselorview.css'; 

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
  const [showAllCreds, setShowAllCreds] = useState(false);

  const [hoveredStar, setHoveredStar] = useState(0);
  const [userRating, setUserRating] = useState(0);
  const [ratingSubmitted, setRatingSubmitted] = useState(false);
  const [ratingLoading, setRatingLoading] = useState(false);

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

  const handleSubmitRating = async (star) => {
    setRatingLoading(true);
    try {
      await axios.post(
        `${API}/counselors/${id}/rate`,
        { rating: star },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setUserRating(star);
      setRatingSubmitted(true);
      fetchCounselor();
    } catch (err) {
      alert(err.response?.data || 'Failed to submit rating.');
    }
    setRatingLoading(false);
  };

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

  const ratingAvg = counselor?.averageRating || 0;
  const ratingCount = counselor?.ratingCount || 0;
  const credentials = counselor?.credentials || [];
  const visibleCreds = showAllCreds ? credentials : credentials.slice(0, 3);

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

            {/* ── LEFT: Compact profile card ─────────────────────── */}
            <div className="cv-left-col">
              <div className="cv-profile-card">
                <div className="cv-card-banner" />

                <div className="cv-avatar-wrap">
                  {counselor.profilePhoto ? (
                    <img src={counselor.profilePhoto} alt="Profile" className="cv-avatar-img" />
                  ) : (
                    <div className="cv-avatar-initials">{getInitials(counselor)}</div>
                  )}
                </div>

                <span className="cv-available-badge">● Available</span>

                <h2 className="cv-name">{counselor.firstName} {counselor.lastName}</h2>

                {counselor.specialization && (
                  <div className="cv-spec-pill">
                    <BookOpen size={11} /> {counselor.specialization}
                  </div>
                )}

                <div className="cv-divider" />

                {/* Stats row */}
                <div className="cv-stat-row">
                  <div className="cv-stat">
                    <div className="cv-stat-val">
                      <Star size={14} fill="#f59e0b" color="#f59e0b" />
                      {ratingAvg.toFixed(1)}
                    </div>
                    <div className="cv-stat-lbl">{ratingCount} review{ratingCount !== 1 ? 's' : ''}</div>
                  </div>
                  <div className="cv-stat-sep" />
                  <div className="cv-stat">
                    <div className="cv-stat-val">
                      {counselor.yearsExperience ? `${counselor.yearsExperience} yrs` : '—'}
                    </div>
                    <div className="cv-stat-lbl">Experience</div>
                  </div>
                </div>

                {counselor.licenseNumber && (
                  <div className="cv-license-tag">
                    <Shield size={11} />
                    PRC: {counselor.licenseNumber}
                  </div>
                )}

                <div className="cv-divider" />

                {/* Rating widget */}
                <div className="cv-rating-widget">
                  <div className="cv-section-label">Rate this Counselor</div>
                  {ratingSubmitted ? (
                    <div className="cv-rating-thanks">  
                      Thanks for your feedback!
                    </div>
                  ) : (
                    <>
                      <div className="cv-rate-stars">
                        {[1, 2, 3, 4, 5].map(star => (
                          <button key={star}
                            className={`cv-rate-star-btn ${star <= (hoveredStar || userRating) ? 'active' : ''}`}
                            onMouseEnter={() => setHoveredStar(star)}
                            onMouseLeave={() => setHoveredStar(0)}
                            onClick={() => handleSubmitRating(star)}
                            disabled={ratingLoading}>
                            <svg viewBox="0 0 24 24"
                              fill={star <= (hoveredStar || userRating) ? 'currentColor' : 'none'}
                              stroke="currentColor" strokeWidth="1.5">
                              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                            </svg>
                          </button>
                        ))}
                      </div>
                      <div className="cv-rate-hint">
                        {hoveredStar === 1 ? 'Poor' :
                         hoveredStar === 2 ? 'Fair' :
                         hoveredStar === 3 ? 'Good' :
                         hoveredStar === 4 ? 'Very good' :
                         hoveredStar === 5 ? 'Excellent!' :
                         'Tap a star to rate'}
                      </div>
                    </>
                  )}
                </div>

              </div>
            </div>

            {/* ── RIGHT: About + Credentials + Booking ───────────── */}
            <div className="cv-booking-panel">

              {/* About card */}
              <div className="cv-panel-card">
                <div className="cv-panel-section-label">
                  About {counselor.firstName}
                </div>
                <p className="cv-bio">
                  {counselor.bio ||
                    `${counselor.firstName} is a dedicated counselor specializing in ${counselor.specialization}. They provide a safe, non-judgmental space for students to explore their concerns and develop strategies for wellbeing.`}
                </p>

                {/* Credentials inside about card */}
                {credentials.length > 0 && (
                  <>
                    <div className="cv-inner-divider" />
                    <div className="cv-panel-section-label" style={{ marginBottom: 12 }}>
                      <Award size={12} style={{ display: 'inline', marginRight: 5 }} />
                      Credentials & Education
                    </div>
                    <div className="cv-creds-grid">
                      {visibleCreds.map((cred, i) => (
                        <div key={i} className="cv-cred-item">
                          <div className="cv-cred-dot" />
                          <div className="cv-cred-body">
                            <div className="cv-cred-title">{cred.title}</div>
                            {cred.year && <div className="cv-cred-year">{cred.year}</div>}
                          </div>
                        </div>
                      ))}
                    </div>
                    {credentials.length > 3 && (
                      <button className="cv-show-more-btn"
                        onClick={() => setShowAllCreds(v => !v)}>
                        {showAllCreds ? 'Show less' : `+${credentials.length - 3} more`}
                      </button>
                    )}
                  </>
                )}
              </div>

              {/* Select a Day */}
              <div className="cv-panel-card">
                <div className="cv-panel-section-label">
                  <Calendar size={13} /> Book an Appointment
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
                        <button key={key}
                          className={`cv-day-btn ${selectedDay === key ? 'selected' : ''}`}
                          onClick={() => { setSelectedDay(key); setSelectedSlot(null); }}>
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
                      <button key={slot.id}
                        className={`cv-slot-btn ${selectedSlot === slot.id ? 'selected' : ''}`}
                        onClick={() => { setSelectedSlot(slot.id); handleSlotClick(slot); }}>
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