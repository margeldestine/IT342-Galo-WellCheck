import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import StudentTopbar from '../components/StudentTopbar';
import StudentSidebar from '../components/StudentSidebar';
import '../styles/BookAppointment.css';

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

  const formatDate = (dt) => new Date(dt).toLocaleString('en-US', {
    month: 'long', day: 'numeric', year: 'numeric'
  });

  const formatTime = (dt) => new Date(dt).toLocaleString('en-US', {
    hour: '2-digit', minute: '2-digit'
  });

  const formatFullDate = (dt) => new Date(dt).toLocaleString('en-US', {
    weekday: 'long', month: 'long', day: 'numeric', year: 'numeric'
  });

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || '';
  const lastName = user.lastName || '';

  return (
    <div className="book-wrapper">
      <StudentTopbar />

      <div className="book-container">
        <StudentSidebar activeItem="browse-counselors" />

        {/* Main Content */}
        <main className="book-main">
          <div className="book-content-wrapper">

          {step !== 4 && (
            <>
              {/* Back button */}
              <button className="btn-back-counselors" onClick={() => navigate('/dashboard')}>
                ← Back to Counselors
              </button>

              {/* Header */}
              <div className="book-header-group">
                <h1 className="book-heading">Book Appointment</h1>
                <p className="book-subheading">
                  With {selectedCounselor.firstName} {selectedCounselor.lastName} on {formatDate(selectedSlot.startTime)} at {formatTime(selectedSlot.startTime)}
                </p>
              </div>

              {/* Step Indicators */}
              <div className="step-indicators">
                <div className={`step-circle ${step >= 1 ? 'active' : ''} ${step > 1 ? 'done' : ''}`}>1</div>
                <div className={`step-line ${step > 1 ? 'done' : ''}`} />
                <div className={`step-circle ${step >= 2 ? 'active' : ''} ${step > 2 ? 'done' : ''}`}>2</div>
                <div className={`step-line ${step > 2 ? 'done' : ''}`} />
                <div className={`step-circle ${step >= 3 ? 'active' : ''}`}>3</div>
              </div>
            </>
          )}

          {/* STEP 1 — Personal Information */}
          {step === 1 && (
            <div className="book-card">
              <h2 className="book-card-title">Personal Information</h2>

              {loading ? (
                <div className="book-loading">Loading your profile...</div>
              ) : (
                <>
                  <div className="book-field">
                    <label className="book-label">Student ID Number</label>
                    <input className="book-input" value={studentProfile?.studentIdNumber || ''} readOnly />
                  </div>

                  <div className="book-row">
                    <div className="book-field">
                      <label className="book-label">First Name</label>
                      <input className="book-input" value={firstName} readOnly />
                    </div>
                    <div className="book-field">
                      <label className="book-label">Last Name</label>
                      <input className="book-input" value={lastName} readOnly />
                    </div>
                  </div>

                  <div className="book-field">
                    <label className="book-label">Enrolled Program</label>
                    <input className="book-input" value={studentProfile?.program || ''} readOnly />
                  </div>

                  <div className="book-row">
                    <div className="book-field">
                      <label className="book-label">Year Level</label>
                      <input className="book-input" value={studentProfile?.yearLevel ? `${studentProfile.yearLevel}${getOrdinal(studentProfile.yearLevel)} Year` : ''} readOnly />
                    </div>
                    <div className="book-field">
                      <label className="book-label">Gender</label>
                      <input className="book-input" value={studentProfile?.gender || ''} readOnly />
                    </div>
                  </div>

                  <div className="book-field">
                    <label className="book-label">Birthdate</label>
                    <input className="book-input" value={studentProfile?.birthdate || ''} readOnly />
                  </div>

                  <div className="book-actions">
                    <button className="btn-change-details" onClick={() => navigate('/studentprofile')}>
                        Change Details
                    </button>   
                    <button className="btn-confirm-details" onClick={() => setStep(2)}>
                      Confirm Details
                    </button>
                  </div>
                </>
              )}
            </div>
          )}

          {/* STEP 2 — School ID */}
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

          {/* STEP 3 — Review & Submit */}
          {step === 3 && (
            <div className="book-card">
              <h2 className="book-card-title">Review & Submit</h2>
              <p className="book-card-sub">Please review your booking details before submitting.</p>

              <div className="review-box">
                <div className="review-row">
                  <span className="review-label">Counselor</span>
                  <span className="review-value">{selectedCounselor.firstName} {selectedCounselor.lastName}</span>
                </div>
                <div className="review-row">
                  <span className="review-label">Specialization</span>
                  <span className="review-value">{selectedCounselor.specialization}</span>
                </div>
                <div className="review-row">
                  <span className="review-label">Date</span>
                  <span className="review-value">{formatFullDate(selectedSlot.startTime)}</span>
                </div>
                <div className="review-row">
                  <span className="review-label">Time</span>
                  <span className="review-value">{formatTime(selectedSlot.startTime)} → {formatTime(selectedSlot.endTime)}</span>
                </div>
                <div className="review-row">
                  <span className="review-label">Student</span>
                  <span className="review-value">{firstName} {lastName}</span>
                </div>
                <div className="review-row">
                  <span className="review-label">Student ID</span>
                  <span className="review-value">{studentProfile?.studentIdNumber}</span>
                </div>
              </div>

              {bookingError && <div className="book-error">{bookingError}</div>}

              <div className="book-actions">
                <button className="btn-change-details" onClick={() => setStep(2)}>Back</button>
                <button className="btn-confirm-details" onClick={handleBookAppointment} disabled={bookingLoading}>
                  {bookingLoading ? 'Submitting...' : 'Confirm Booking'}
                </button>
              </div>
            </div>
          )}

          {/* STEP 4 — Booking Confirmed */}
          {step === 4 && (
            <div className="booking-confirmed-wrapper">
              <div className="booking-confirmed">
                <div className="confirmed-icon">🎉</div>
                <h2 className="confirmed-title">Booking Submitted!</h2>
                <p className="confirmed-sub">
                  Your appointment request has been sent to {selectedCounselor.firstName} {selectedCounselor.lastName}.
                </p>

                <div className="confirmed-details">
                  <div className="review-row">
                    <span className="review-label">Counselor</span>
                    <span className="review-value">{selectedCounselor.firstName} {selectedCounselor.lastName}</span>
                  </div>
                  <div className="review-row">
                    <span className="review-label">Date</span>
                    <span className="review-value">{formatFullDate(selectedSlot.startTime)}</span>
                  </div>
                  <div className="review-row">
                    <span className="review-label">Time</span>
                    <span className="review-value">{formatTime(selectedSlot.startTime)} → {formatTime(selectedSlot.endTime)}</span>
                  </div>
                  <div className="review-row">
                    <span className="review-label">Status</span>
                    <span className="review-value" style={{ color: '#f59e0b' }}>⏳ Pending Approval</span>
                  </div>
                </div>

                <div className="confirmed-actions">
                  <button className="btn-view-apts" onClick={() => navigate('/dashboard')}>
                    📅 View My Appointments
                  </button>
                  <button className="btn-back-dash" onClick={() => navigate('/dashboard')}>
                    Back to Dashboard
                  </button>
                </div>
              </div>
            </div>
          )}
          </div>
        </main>
      </div>
    </div>
  );
}

// School ID Step Component
function SchoolIdStep({ token, studentProfile, onBack, onNext, API, bookingNote, setBookingNote }) {
  const navigate = useNavigate();

  return (
    <div className="book-card">
      <h2 className="book-card-title">Additional Info</h2>

      {/* School ID Status */}
      {studentProfile?.schoolIdPhotoUrl ? (
        <div className="school-id-status success">
          <div className="school-id-status-icon">✓</div>
          <div className="school-id-status-text">
            <div className="school-id-status-title">School ID on file</div>
            <div className="school-id-status-sub">Your counselor will use this to verify your enrollment.</div>
          </div>
        </div>
      ) : (
        <div className="school-id-status warning">
          <div className="school-id-status-icon">⚠</div>
          <div className="school-id-status-text">
            <div className="school-id-status-title">No School ID uploaded yet</div>
            <div className="school-id-status-sub">School ID is required before your booking can be reviewed.</div>
          </div>
          <button className="btn-upload-in-profile" onClick={() => navigate('/studentprofile')}>
            Upload in Profile
          </button>
        </div>
      )}

      {/* Note */}
      <div className="book-field" style={{ marginTop: '20px' }}>
        <label className="book-label">Note / Message (optional)</label>
        <textarea
          className="book-textarea"
          placeholder="Enter any additional notes or messages..."
          value={bookingNote}
          onChange={(e) => setBookingNote(e.target.value)}
          rows={4}
        />
      </div>

      <div className="book-actions" style={{ marginTop: '28px', flexDirection: 'row', justifyContent: 'space-between' }}>
        <button className="btn-change-details" style={{ width: 'auto', padding: '12px 24px' }} onClick={onBack}>
          Back
        </button>
        <button
          className="btn-confirm-details"
          style={{ width: 'auto', padding: '12px 24px' }}
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