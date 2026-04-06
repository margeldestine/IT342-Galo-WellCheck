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

  // Counselors state
  const [counselors, setCounselors] = useState([]);
  const [loadingCounselors, setLoadingCounselors] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedSpecialization, setSelectedSpecialization] = useState('');

  // Slots modal state
  const [showSlotsModal, setShowSlotsModal] = useState(false);
  const [selectedCounselor, setSelectedCounselor] = useState(null);
  const [slots, setSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);

  // Multi-step booking state
  const [bookingStep, setBookingStep] = useState(0); // 0=none, 1=slot selected, 2=profile, 3=review, 4=confirmed
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [studentProfile, setStudentProfile] = useState(null);
  const [bookingNote, setBookingNote] = useState('');
  const [bookingLoading, setBookingLoading] = useState(false);
  const [bookingError, setBookingError] = useState('');

  // School ID upload state
  const [schoolIdFile, setSchoolIdFile] = useState(null);
  const [schoolIdPreview, setSchoolIdPreview] = useState(null);
  const [uploadingId, setUploadingId] = useState(false);
  const [uploadError, setUploadError] = useState('');

  // Appointments state
  const [appointments, setAppointments] = useState([]);
  const [loadingAppointments, setLoadingAppointments] = useState(false);
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    if (activeTab === 'counselors') fetchCounselors();
    if (activeTab === 'appointments') fetchAppointments();
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

  const fetchSlots = async (counselorId) => {
    setLoadingSlots(true);
    try {
      const res = await axios.get(`${API}/slots/counselor/${counselorId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      const sorted = res.data.sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
      setSlots(sorted);
    } catch (err) {
      console.error('Failed to fetch slots:', err);
    }
    setLoadingSlots(false);
  };

  const fetchStudentProfile = async () => {
    try {
      const res = await axios.get(`${API}/auth/profile/student`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setStudentProfile(res.data);
    } catch (err) {
      console.error('Failed to fetch student profile:', err);
    }
  };

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

  const handleViewSlots = (counselor) => {
    setSelectedCounselor(counselor);
    setShowSlotsModal(true);
    fetchSlots(counselor.id);
  };

  const handleSelectSlot = (slot) => {
  localStorage.setItem('selectedSlot', JSON.stringify(slot));
  localStorage.setItem('selectedCounselor', JSON.stringify(selectedCounselor));
  setShowSlotsModal(false);
  navigate('/book-appointment');
};

  const handleSchoolIdChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSchoolIdFile(file);
      setSchoolIdPreview(URL.createObjectURL(file));
    }
  };

  const handleUploadSchoolId = async () => {
    if (!schoolIdFile) return;
    setUploadingId(true);
    setUploadError('');
    try {
      const formData = new FormData();
      formData.append('file', schoolIdFile);
      const res = await axios.post(`${API}/upload/school-id`, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        }
      });
      setStudentProfile(prev => ({ ...prev, schoolIdPhotoUrl: res.data }));
      setSchoolIdFile(null);
    } catch (err) {
      setUploadError(err.response?.data || 'Failed to upload school ID.');
    }
    setUploadingId(false);
  };

  const handleBookAppointment = async () => {
    setBookingError('');
    setBookingLoading(true);
    try {
      await axios.post(`${API}/appointments`, {
        slotId: selectedSlot.id,
        note: bookingNote
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setBookingStep(4);
      fetchCounselors();
    } catch (err) {
      setBookingError(err.response?.data || 'Failed to book appointment.');
      setBookingStep(3);
    }
    setBookingLoading(false);
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

  const resetBooking = () => {
    setBookingStep(0);
    setSelectedSlot(null);
    setSelectedCounselor(null);
    setBookingNote('');
    setBookingError('');
    setSchoolIdFile(null);
    setSchoolIdPreview(null);
  };

  const filteredCounselors = counselors.filter(c => {
    const fullName = `${c.firstName} ${c.lastName}`.toLowerCase();
    const matchesSearch = fullName.includes(searchQuery.toLowerCase());
    const matchesSpec = selectedSpecialization === '' || c.specialization === selectedSpecialization;
    return matchesSearch && matchesSpec;
  });

  const filteredAppointments = appointments.filter(a =>
    filterStatus === 'ALL' || a.status === filterStatus
  );

  const specializations = [...new Set(counselors.map(c => c.specialization))];

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good Morning';
    if (hour < 18) return 'Good Afternoon';
    return 'Good Evening';
  };

  const formatDate = (dt) => new Date(dt).toLocaleString('en-US', {
    weekday: 'long', month: 'long', day: 'numeric', year: 'numeric'
  });

  const formatTime = (dt) => new Date(dt).toLocaleString('en-US', {
    hour: '2-digit', minute: '2-digit'
  });

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

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
          <div className={`nav-item ${activeTab === 'profile' ? 'active' : ''}`} 
            onClick={() => navigate('/studentprofile')}>
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

          {/* Dashboard Tab */}
          {activeTab === 'dashboard' && bookingStep === 0 && (
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
                <div className="appointment-empty"><p>No upcoming appointments.</p></div>
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

          {/* Browse Counselors Tab */}
          {activeTab === 'counselors' && bookingStep === 0 && (
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
                          <div className="counselor-name">{counselor.firstName} {counselor.lastName}</div>
                          <div className="counselor-specialization">{counselor.specialization}</div>
                        </div>
                      </div>
                      <p className="counselor-bio">{counselor.bio || 'No bio available.'}</p>
                      <div className="counselor-slots-count">
                        📅 {counselor.availableSlots} available slot{counselor.availableSlots !== 1 ? 's' : ''}
                      </div>
                      <button
                        className="btn-book-slot"
                        onClick={() => handleViewSlots(counselor)}
                        disabled={counselor.availableSlots === 0}
                      >
                        {counselor.availableSlots === 0 ? 'No slots available' : 'View available slots'}
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}

          {/* STEP 2 — Confirm Profile + School ID */}
          {bookingStep === 2 && (
            <div className="booking-flow">
              <div className="booking-steps">
                <div className="step done">1. Slot Selected</div>
                <div className="step-divider" />
                <div className="step active">2. Confirm Profile</div>
                <div className="step-divider" />
                <div className="step">3. Review</div>
              </div>

              <div className="booking-section-card">
                <h2 className="booking-section-title">Confirm Your Profile</h2>
                <p className="booking-section-sub">Please verify your information before booking.</p>

                <div className="profile-info-grid">
                  <div className="profile-info-item">
                    <span className="profile-info-label">Full Name</span>
                    <span className="profile-info-value">{firstName} {lastName}</span>
                  </div>
                  <div className="profile-info-item">
                    <span className="profile-info-label">Student ID</span>
                    <span className="profile-info-value">{studentProfile?.studentIdNumber || '—'}</span>
                  </div>
                  <div className="profile-info-item">
                    <span className="profile-info-label">Program</span>
                    <span className="profile-info-value">{studentProfile?.program || '—'}</span>
                  </div>
                  <div className="profile-info-item">
                    <span className="profile-info-label">Year Level</span>
                    <span className="profile-info-value">{studentProfile?.yearLevel ? `Year ${studentProfile.yearLevel}` : '—'}</span>
                  </div>
                  <div className="profile-info-item">
                    <span className="profile-info-label">Gender</span>
                    <span className="profile-info-value">{studentProfile?.gender || '—'}</span>
                  </div>
                  <div className="profile-info-item">
                    <span className="profile-info-label">Birthdate</span>
                    <span className="profile-info-value">{studentProfile?.birthdate || '—'}</span>
                  </div>
                </div>

                {/* School ID Section */}
                <div className="school-id-section">
                  <h3 className="school-id-title">School ID Verification</h3>
                  {studentProfile?.schoolIdPhotoUrl ? (
                    <div className="school-id-uploaded">
                      <img src={studentProfile.schoolIdPhotoUrl} alt="School ID" className="school-id-preview" />
                      <div className="school-id-verified">✅ School ID uploaded</div>
                      <button className="btn-reupload" onClick={() => setStudentProfile(prev => ({ ...prev, schoolIdPhotoUrl: null }))}>
                        Replace Photo
                      </button>
                    </div>
                  ) : (
                    <div className="school-id-upload-area">
                      <p className="school-id-required">⚠️ Please upload your School ID to proceed.</p>
                      <input
                        type="file"
                        accept="image/*"
                        id="school-id-input"
                        style={{ display: 'none' }}
                        onChange={handleSchoolIdChange}
                      />
                      {schoolIdPreview && (
                        <img src={schoolIdPreview} alt="Preview" className="school-id-preview" />
                      )}
                      {!schoolIdFile ? (
                        <label htmlFor="school-id-input" className="btn-upload-id">
                          📷 Choose Photo
                        </label>
                      ) : (
                        <div className="upload-actions">
                          <span className="upload-filename">{schoolIdFile.name}</span>
                          <button className="btn-confirm-upload" onClick={handleUploadSchoolId} disabled={uploadingId}>
                            {uploadingId ? 'Uploading...' : 'Upload School ID'}
                          </button>
                          <label htmlFor="school-id-input" className="btn-change-photo">Change Photo</label>
                        </div>
                      )}
                      {uploadError && <div className="upload-error">{uploadError}</div>}
                    </div>
                  )}
                </div>

                <div className="booking-flow-actions">
                  <button className="btn-flow-back" onClick={resetBooking}>← Back</button>
                  <button
                    className="btn-flow-next"
                    onClick={() => setBookingStep(3)}
                    disabled={!studentProfile?.schoolIdPhotoUrl}
                  >
                    Next: Review →
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* STEP 3 — Review & Submit */}
          {bookingStep === 3 && (
            <div className="booking-flow">
              <div className="booking-steps">
                <div className="step done">1. Slot Selected</div>
                <div className="step-divider" />
                <div className="step done">2. Profile Confirmed</div>
                <div className="step-divider" />
                <div className="step active">3. Review</div>
              </div>

              <div className="booking-section-card">
                <h2 className="booking-section-title">Review Your Booking</h2>
                <p className="booking-section-sub">Please review the details before submitting.</p>

                <div className="review-summary">
                  <div className="review-item">
                    <span className="review-label">Counselor</span>
                    <span className="review-value">{selectedCounselor?.firstName} {selectedCounselor?.lastName}</span>
                  </div>
                  <div className="review-item">
                    <span className="review-label">Specialization</span>
                    <span className="review-value">{selectedCounselor?.specialization}</span>
                  </div>
                  <div className="review-item">
                    <span className="review-label">Date</span>
                    <span className="review-value">{selectedSlot && formatDate(selectedSlot.startTime)}</span>
                  </div>
                  <div className="review-item">
                    <span className="review-label">Time</span>
                    <span className="review-value">
                      {selectedSlot && `${formatTime(selectedSlot.startTime)} → ${formatTime(selectedSlot.endTime)}`}
                    </span>
                  </div>
                  <div className="review-item">
                    <span className="review-label">Student</span>
                    <span className="review-value">{firstName} {lastName}</span>
                  </div>
                  <div className="review-item">
                    <span className="review-label">Student ID</span>
                    <span className="review-value">{studentProfile?.studentIdNumber}</span>
                  </div>
                </div>

                <div className="booking-note-group">
                  <label className="booking-note-label">Note (optional)</label>
                  <textarea
                    className="booking-note-input"
                    placeholder="Add a note or reason for your visit..."
                    value={bookingNote}
                    onChange={(e) => setBookingNote(e.target.value)}
                    rows={3}
                  />
                </div>

                {bookingError && <div className="booking-error">{bookingError}</div>}

                <div className="booking-flow-actions">
                  <button className="btn-flow-back" onClick={() => setBookingStep(2)}>← Back</button>
                  <button className="btn-flow-submit" onClick={handleBookAppointment} disabled={bookingLoading}>
                    {bookingLoading ? 'Submitting...' : '✓ Confirm Booking'}
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* STEP 4 — Booking Confirmed */}
          {bookingStep === 4 && (
            <div className="booking-confirmed">
              <div className="confirmed-icon">🎉</div>
              <h2 className="confirmed-title">Booking Submitted!</h2>
              <p className="confirmed-sub">Your appointment request has been sent to the counselor.</p>

              <div className="confirmed-details">
                <div className="review-item">
                  <span className="review-label">Counselor</span>
                  <span className="review-value">{selectedCounselor?.firstName} {selectedCounselor?.lastName}</span>
                </div>
                <div className="review-item">
                  <span className="review-label">Date</span>
                  <span className="review-value">{selectedSlot && formatDate(selectedSlot.startTime)}</span>
                </div>
                <div className="review-item">
                  <span className="review-label">Time</span>
                  <span className="review-value">
                    {selectedSlot && `${formatTime(selectedSlot.startTime)} → ${formatTime(selectedSlot.endTime)}`}
                  </span>
                </div>
                <div className="review-item">
                  <span className="review-label">Status</span>
                  <span className="review-value status-pending-text">⏳ Pending Approval</span>
                </div>
              </div>

              <div className="confirmed-actions">
                <button className="btn-view-appointments" onClick={() => {
                  resetBooking();
                  setActiveTab('appointments');
                }}>
                  📅 View My Appointments
                </button>
                <button className="btn-back-dashboard" onClick={() => {
                  resetBooking();
                  setActiveTab('dashboard');
                }}>
                  Back to Dashboard
                </button>
              </div>
            </div>
          )}

          {/* My Appointments Tab */}
          {activeTab === 'appointments' && bookingStep === 0 && (
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
                          <div className="apt-time">{formatTime(apt.startTime)} → {formatTime(apt.endTime)}</div>
                          {apt.note && <div className="apt-note">📝 {apt.note}</div>}
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

          {/* Profile Tab */}
          {activeTab === 'profile' && bookingStep === 0 && (
            <>
              <h1 className="greeting">Profile</h1>
              <p className="greeting-sub">View and edit your profile.</p>
              <div className="appointment-card">
                <div className="appointment-empty"><p>Profile management coming soon.</p></div>
              </div>
            </>
          )}

        </div>
      </main>

      {/* Slots Modal */}
      {showSlotsModal && (
        <div className="modal-overlay">
          <div className="modal-box modal-large">
            <div className="modal-header">
              <div>
                <h3 className="modal-title">Available Slots</h3>
                <p className="modal-subtitle">
                  {selectedCounselor?.firstName} {selectedCounselor?.lastName} · {selectedCounselor?.specialization}
                </p>
              </div>
              <button className="modal-close" onClick={() => setShowSlotsModal(false)}>✕</button>
            </div>
            {loadingSlots ? (
              <div className="empty-msg">Loading slots...</div>
            ) : slots.length === 0 ? (
              <div className="empty-msg">No available slots.</div>
            ) : (
              <div className="slots-modal-list">
                {slots.map(slot => (
                  <div key={slot.id} className="slot-modal-item" onClick={() => handleSelectSlot(slot)}>
                    <div className="slot-modal-date-badge">
                      <span className="slot-modal-month">
                        {new Date(slot.startTime).toLocaleString('en-US', { month: 'short' })}
                      </span>
                      <span className="slot-modal-day">{new Date(slot.startTime).getDate()}</span>
                    </div>
                    <div className="slot-modal-details">
                      <div className="slot-modal-time">{formatTime(slot.startTime)} → {formatTime(slot.endTime)}</div>
                      <div className="slot-modal-date">{formatDate(slot.startTime)}</div>
                    </div>
                    <button className="btn-select-slot">Select</button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Cancel Modal */}
      {showCancelModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-icon">⚠️</div>
            <h3 className="modal-title">Cancel Appointment</h3>
            <p className="modal-message">Are you sure you want to cancel this appointment?</p>
            <div className="modal-actions">
              <button className="btn-modal-cancel" onClick={() => {
                setShowCancelModal(false);
                setCancellingId(null);
              }}>Keep it</button>
              <button className="btn-modal-delete" onClick={handleCancelAppointment}>Yes, Cancel</button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}

export default StudentDashboard;