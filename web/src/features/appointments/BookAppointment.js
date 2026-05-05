import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { ArrowLeft, CheckCircle, AlertTriangle } from 'lucide-react';
import StudentSidebar from '../../components/StudentSidebar'; 
import './BookAppointment.css'; 
import logo from '../../assets/wellcheck-logo.png'; 

const API = process.env.REACT_APP_API_URL;

function BookAppointment() {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');
  const selectedSlot = JSON.parse(localStorage.getItem('selectedSlot') || '{}');
  const selectedCounselor = JSON.parse(localStorage.getItem('selectedCounselor') || '{}');

  const [step, setStep] = useState(1);
  const [studentProfile, setStudentProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [bookingNote, setBookingNote] = useState('');
  const [bookingLoading, setBookingLoading] = useState(false);
  const [bookingError, setBookingError] = useState('');

  useEffect(() => {
    fetchStudentProfile();
  }, []);

  const fetchStudentProfile = async () => {
    setLoading(true);
    try {
      const res = await axios.get(`${API}/auth/profile/student`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setStudentProfile(res.data);
    } catch (err) {
      console.error('Failed to fetch profile:', err);
    }
    setLoading(false);
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
      setStep(4);
    } catch (err) {
      setBookingError(err.response?.data || 'Failed to book appointment.');
    }
    setBookingLoading(false);
  };

  // "May 6, 2026 (Wednesday)"
  const formatDate = (dt) => {
    const d = new Date(dt);
    const date = d.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
    const weekday = d.toLocaleDateString('en-US', { weekday: 'long' });
    return `${date} (${weekday})`;
  };

  // "12:29 AM"
  const formatTime = (dt) => new Date(dt).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });

  // "12:29 AM to 12:59 AM"
  const formatTimeRange = (start, end) => `${formatTime(start)} to ${formatTime(end)}`;

  // short date for subheading "May 6, 2026 at 12:29 AM"
  const formatShortDate = (dt) => new Date(dt).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || '';
  const lastName = user.lastName || '';

  return (
    <div className="ba-layout">
      <StudentSidebar activeItem="browse-counselors" />
      <main className="ba-main">
        <div className="ba-content">

          {step !== 4 && (
            <>
              <button className="ba-btn-back" onClick={() => navigate('/browse-counselors')}>
                <ArrowLeft size={14} /> Back to Counselors
              </button>
              <div className="ba-header">
                <h1 className="ba-heading">Book Appointment</h1>
                <p className="ba-subheading">
                  With {selectedCounselor.firstName} {selectedCounselor.lastName} on {formatShortDate(selectedSlot.startTime)} at {formatTime(selectedSlot.startTime)}
                </p>
              </div>
              <div className="ba-steps">
                <div className={`ba-step-circle ${step >= 1 ? 'active' : ''} ${step > 1 ? 'done' : ''}`}>1</div>
                <div className={`ba-step-line ${step > 1 ? 'done' : ''}`} />
                <div className={`ba-step-circle ${step >= 2 ? 'active' : ''} ${step > 2 ? 'done' : ''}`}>2</div>
                <div className={`ba-step-line ${step > 2 ? 'done' : ''}`} />
                <div className={`ba-step-circle ${step >= 3 ? 'active' : ''}`}>3</div>
              </div>
            </>
          )}

          {/* ── Step 1: Personal Info ── */}
          {step === 1 && (
            <div className="ba-card">
              <div className="ba-card-header">
                <h2 className="ba-card-title">Personal Information</h2>
                <p className="ba-card-sub">Verify your details before proceeding.</p>
              </div>
              {loading ? (
                <div className="ba-loading">Loading your profile...</div>
              ) : (
                <>
                  <div className="ba-field">
                    <label className="ba-label">Student ID Number</label>
                    <input className="ba-input" value={studentProfile?.studentIdNumber || ''} readOnly />
                  </div>
                  <div className="ba-row">
                    <div className="ba-field">
                      <label className="ba-label">First Name</label>
                      <input className="ba-input" value={firstName} readOnly />
                    </div>
                    <div className="ba-field">
                      <label className="ba-label">Last Name</label>
                      <input className="ba-input" value={lastName} readOnly />
                    </div>
                  </div>
                  <div className="ba-field">
                    <label className="ba-label">Enrolled Program</label>
                    <input className="ba-input" value={studentProfile?.program || ''} readOnly />
                  </div>
                  <div className="ba-row">
                    <div className="ba-field">
                      <label className="ba-label">Year Level</label>
                      <input className="ba-input" value={studentProfile?.yearLevel ? `${studentProfile.yearLevel}${getOrdinal(studentProfile.yearLevel)} Year` : ''} readOnly />
                    </div>
                    <div className="ba-field">
                      <label className="ba-label">Gender</label>
                      <input className="ba-input" value={studentProfile?.gender || ''} readOnly />
                    </div>
                  </div>
                  <div className="ba-field">
                    <label className="ba-label">Birthdate</label>
                    <input className="ba-input" value={studentProfile?.birthdate || ''} readOnly />
                  </div>
                  <div className="ba-actions">
                    <button className="ba-btn-secondary" onClick={() => navigate('/studentprofile')}>Change Details</button>
                    <button className="ba-btn-primary" onClick={() => setStep(2)}>Confirm Details</button>
                  </div>
                </>
              )}
            </div>
          )}

          {/* ── Step 2: School ID + Note ── */}
          {step === 2 && (
            <SchoolIdStep
              token={token}
              studentProfile={studentProfile}
              onBack={() => setStep(1)}
              onNext={() => setStep(3)}
              API={API}
              bookingNote={bookingNote}
              setBookingNote={setBookingNote}
            />
          )}

          {/* ── Step 3: Review ── */}
          {step === 3 && (
            <div className="ba-card">
              <div className="ba-card-header">
                <h2 className="ba-card-title">Review & Submit</h2>
                <p className="ba-card-sub">Please review your booking details before submitting.</p>
              </div>

              <div className="ba-review-section">
                <div className="ba-review-section-label">Counselor Details</div>
                <div className="ba-review-box">
                  <div className="ba-review-row">
                    <span className="ba-review-label">Counselor</span>
                    <span className="ba-review-value">{selectedCounselor.firstName} {selectedCounselor.lastName}</span>
                  </div>
        
                  <div className="ba-review-row">
                    <span className="ba-review-label">Specialization</span>
                    <span className="ba-review-value">{selectedCounselor.specialization}</span>
                  </div>
                </div>
              </div>

              <div className="ba-review-section">
                <div className="ba-review-section-label">Schedule</div>
                <div className="ba-review-box">
                  <div className="ba-review-row">
                    <span className="ba-review-label">Date</span>
                    <span className="ba-review-value">{formatDate(selectedSlot.startTime)}</span>
                  </div>
                  <div className="ba-review-row">
                    <span className="ba-review-label">Time</span>
                    <span className="ba-review-value">{formatTimeRange(selectedSlot.startTime, selectedSlot.endTime)}</span>
                  </div>
                </div>
              </div>

              <div className="ba-review-section">
                <div className="ba-review-section-label">Student</div>
                <div className="ba-review-box">
                  <div className="ba-review-row">
                    <span className="ba-review-label">Name</span>
                    <span className="ba-review-value">{firstName} {lastName}</span>
                  </div>
                  <div className="ba-review-row">
                    <span className="ba-review-label">Student ID</span>
                    <span className="ba-review-value">{studentProfile?.studentIdNumber}</span>
                  </div>
                </div>
              </div>

              {bookingNote && (
                <div className="ba-review-section">
                  <div className="ba-review-section-label">Note</div>
                  <div className="ba-review-box">
                    <div className="ba-review-row">
                      <span className="ba-review-value" style={{ textAlign: 'left' }}>{bookingNote}</span>
                    </div>
                  </div>
                </div>
              )}

              {bookingError && <div className="ba-error">{bookingError}</div>}

              <div className="ba-actions">
                <button className="ba-btn-secondary" onClick={() => setStep(2)}>Back</button>
                <button className="ba-btn-primary" onClick={handleBookAppointment} disabled={bookingLoading}>
                  {bookingLoading ? 'Submitting…' : 'Confirm Booking'}
                </button>
              </div>
            </div>
          )}

          {/* ── Step 4: Success ── */}
          {step === 4 && (
            <div className="ba-confirmed-wrapper">
              <div className="ba-confirmed">
                <div className="ba-confirmed-icon-wrap">
                  <CheckCircle size={40} color="#1c3a2f" />
                </div>
                <h2 className="ba-confirmed-title">Booking Submitted!</h2>
                <p className="ba-confirmed-sub">
                  Your appointment request has been sent to {selectedCounselor.firstName} {selectedCounselor.lastName}. You'll be notified once it's confirmed.
                </p>

                <div className="ba-review-section" style={{ width: '100%', textAlign: 'left' }}>
                  <div className="ba-review-box">
                    <div className="ba-review-row">
                      <span className="ba-review-label">Counselor</span>
                      <span className="ba-review-value">{selectedCounselor.firstName} {selectedCounselor.lastName}</span>
                    </div>
                    <div className="ba-review-row">
                      <span className="ba-review-label">Date</span>
                      <span className="ba-review-value">{formatDate(selectedSlot.startTime)}</span>
                    </div>
                    <div className="ba-review-row">
                      <span className="ba-review-label">Time</span>
                      <span className="ba-review-value">{formatTimeRange(selectedSlot.startTime, selectedSlot.endTime)}</span>
                    </div>
                    <div className="ba-review-divider" />
                    <div className="ba-review-row">
                      <span className="ba-review-label">Status</span>
                      <span className="ba-status-pending">Pending Approval</span>
                    </div>
                  </div>
                </div>

                <div className="ba-confirmed-actions">
                  <button className="ba-btn-primary" onClick={() => navigate('/my-appointments')}>View My Appointments</button>
                  <button className="ba-btn-secondary" onClick={() => navigate('/dashboard')}>Back to Dashboard</button>
                </div>
              </div>
            </div>
          )}

        </div>
      </main>
    </div>
  );
}

function SchoolIdStep({ token, studentProfile, onBack, onNext, API, bookingNote, setBookingNote }) {
  const navigate = useNavigate();

  return (
    <div className="ba-card">
      <div className="ba-card-header">
        <h2 className="ba-card-title">Additional Info</h2>
        <p className="ba-card-sub">Make sure your school ID is on file before proceeding.</p>
      </div>

      {studentProfile?.schoolIdPhotoUrl ? (
        <div className="ba-id-status success">
          <div className="ba-id-icon-wrap success">
            <CheckCircle size={18} color="#1c3a2f" />
          </div>
          <div className="ba-id-text">
            <div className="ba-id-title">School ID on file</div>
            <div className="ba-id-sub">Your counselor will use this to verify your enrollment.</div>
          </div>
        </div>
      ) : (
        <div className="ba-id-status warning">
          <div className="ba-id-icon-wrap warning">
            <AlertTriangle size={18} color="#b45309" />
          </div>
          <div className="ba-id-text">
            <div className="ba-id-title">No School ID uploaded yet</div>
            <div className="ba-id-sub">School ID is required before your booking can be reviewed.</div>
          </div>
          <button className="ba-btn-upload-profile" onClick={() => navigate('/studentprofile')}>
            Upload in Profile
          </button>
        </div>
      )}

      <div className="ba-field" style={{ marginTop: '20px' }}>
        <label className="ba-label">Note / Message (optional)</label>
        <textarea
          className="ba-textarea"
          placeholder="Briefly describe what you'd like to talk about…"
          value={bookingNote}
          onChange={(e) => setBookingNote(e.target.value)}
          rows={4}
        />
      </div>

      <div className="ba-actions">
        <button className="ba-btn-secondary" onClick={onBack}>Back</button>
        <button
          className="ba-btn-primary"
          onClick={onNext}
          disabled={!studentProfile?.schoolIdPhotoUrl}
        >
          Review
        </button>
      </div>
    </div>
  );
}

function getOrdinal(n) {
  const s = ['th', 'st', 'nd', 'rd'];
  const v = n % 100;
  return s[(v - 20) % 10] || s[v] || s[0];
}

export default BookAppointment;