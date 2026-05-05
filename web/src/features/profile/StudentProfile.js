import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import StudentSidebar from '../../components/StudentSidebar';
import './StudentProfile.css';

const API = process.env.REACT_APP_API_URL;

function StudentProfile() {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');
  const userFromStorage = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = userFromStorage.firstName || '';
  const lastName = userFromStorage.lastName || '';

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  const [schoolIdFile, setSchoolIdFile] = useState(null);
  const [schoolIdPreview, setSchoolIdPreview] = useState(null);
  const [uploadingId, setUploadingId] = useState(false);
  const [uploadSuccess, setUploadSuccess] = useState('');
  const [uploadError, setUploadError] = useState('');

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    setLoading(true);
    try {
      const res = await axios.get(`${API}/auth/profile/student`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setProfile(res.data);
    } catch (err) {
      console.error('Failed to fetch profile:', err);
    }
    setLoading(false);
  };

  const handleSchoolIdChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSchoolIdFile(file);
      setSchoolIdPreview(URL.createObjectURL(file));
      setUploadSuccess('');
      setUploadError('');
    }
  };

  const handleUploadSchoolId = async () => {
    if (!schoolIdFile) return;
    setUploadingId(true);
    try {
      const formData = new FormData();
      formData.append('file', schoolIdFile);
      const res = await axios.post(`${API}/upload/school-id`, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        }
      });
      setProfile(prev => ({ ...prev, schoolIdPhotoUrl: res.data }));
      setSchoolIdFile(null);
      setSchoolIdPreview(null);
      setUploadSuccess('School ID uploaded successfully!');
      setTimeout(() => setUploadSuccess(''), 3000);
    } catch (err) {
      setUploadError(err.response?.data || 'Failed to upload school ID.');
    }
    setUploadingId(false);
  };

  const getOrdinal = (n) => {
    const s = ['th', 'st', 'nd', 'rd'];
    const v = n % 100;
    return s[(v - 20) % 10] || s[v] || s[0];
  };

  const memberSince = profile?.createdAt
    ? new Date(profile.createdAt).toLocaleDateString('en-US', { month: 'short', year: 'numeric' })
    : '—';

  const initials = `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();

  return (
    <div className="sp-layout">
      <StudentSidebar activeItem="profile" />

      <main className="sp-main">
        {loading ? (
          <div className="sp-loading">Loading your profile...</div>
        ) : (
          <div className="sp-content">

            {/* Page Header */}
            <div className="sp-page-header">
              <div>
                <h1 className="sp-page-title">My Profile</h1>
                <p className="sp-page-sub">View and update your student information.</p>
              </div>
            </div>

            {/* No school ID banner */}
            {!profile?.schoolIdPhotoUrl && (
              <div className="sp-banner">
                ℹ️ Upload your School ID to complete your profile.
              </div>
            )}

            {/* Two-column layout */}
            <div className="sp-profile-layout">

              {/* LEFT — Summary card */}
              <div className="sp-sidebar-card">
                <div className="sp-avatar-lg">{initials}</div>
                <div className="sp-full-name">{firstName} {lastName}</div>
                <div className="sp-student-id">ID: {profile?.studentIdNumber || '—'}</div>

                <div className="sp-pills">
                  {profile?.program && <span className="sp-pill">{profile.program}</span>}
                  {profile?.yearLevel && (
                    <span className="sp-pill">{profile.yearLevel}{getOrdinal(profile.yearLevel)} Year</span>
                  )}
                  {profile?.gender && <span className="sp-pill">{profile.gender}</span>}
                </div>

                <div className="sp-stats">
                
                  {profile?.birthdate && (
                    <div className="sp-stat-row">
                      <span className="sp-stat-label">Birthdate</span>
                      <span className="sp-stat-val">
                        {new Date(profile.birthdate).toLocaleDateString('en-US', {
                          month: 'long', day: 'numeric', year: 'numeric'
                        })}
                      </span>
                    </div>
                  )}
                </div>
              </div>

              {/* RIGHT — Form card */}
              <div className="sp-form-card">
                <div className="sp-form-section-title">Personal Information</div>

                <div className="sp-form-row sp-single">
                  <div className="sp-form-group">
                    <label className="sp-form-label">Student ID Number</label>
                    <input className="sp-form-input" value={profile?.studentIdNumber || ''} readOnly />
                  </div>
                </div>

                <div className="sp-form-row">
                  <div className="sp-form-group">
                    <label className="sp-form-label">First Name</label>
                    <input className="sp-form-input" value={firstName} readOnly />
                  </div>
                  <div className="sp-form-group">
                    <label className="sp-form-label">Last Name</label>
                    <input className="sp-form-input" value={lastName} readOnly />
                  </div>
                </div>

                <div className="sp-form-row sp-single">
                  <div className="sp-form-group">
                    <label className="sp-form-label">Enrolled Program</label>
                    <select className="sp-form-select" value={profile?.program || ''} disabled>
                      <option value="BSIT">BSIT</option>
                      <option value="BSCS">BSCS</option>
                      <option value="BSIS">BSIS</option>
                      <option value="BSA">BSA</option>
                      <option value="BSBA">BSBA</option>
                      <option value="BSN">BSN</option>
                    </select>
                  </div>
                </div>

                <div className="sp-form-row">
                  <div className="sp-form-group">
                    <label className="sp-form-label">Year Level</label>
                    <select className="sp-form-select" value={profile?.yearLevel || ''} disabled>
                      <option value="1">1st Year</option>
                      <option value="2">2nd Year</option>
                      <option value="3">3rd Year</option>
                      <option value="4">4th Year</option>
                    </select>
                  </div>
                  <div className="sp-form-group">
                    <label className="sp-form-label">Gender</label>
                    <select className="sp-form-select" value={profile?.gender || ''} disabled>
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                      <option value="Other">Other</option>
                      <option value="Prefer not to say">Prefer not to say</option>
                    </select>
                  </div>
                </div>

                <div className="sp-form-row sp-single">
                  <div className="sp-form-group">
                    <label className="sp-form-label">Birthdate</label>
                    <input className="sp-form-input" value={profile?.birthdate || ''} readOnly />
                  </div>
                </div>

                {/* School ID Section */}
                <div className="sp-school-id-section">
                  <div className="sp-form-section-title" style={{ marginTop: 0 }}>School ID Photo</div>

                  {uploadSuccess && <div className="sp-upload-success">{uploadSuccess}</div>}
                  {uploadError  && <div className="sp-upload-error">{uploadError}</div>}

                  {profile?.schoolIdPhotoUrl || schoolIdPreview ? (
                    <>
                      <img
                        src={schoolIdPreview || profile.schoolIdPhotoUrl}
                        alt="School ID"
                        className="sp-id-img"
                      />
                      <div className="sp-upload-row">
                        {schoolIdFile && (
                          <button
                            className="sp-btn-upload"
                            onClick={handleUploadSchoolId}
                            disabled={uploadingId}
                          >
                            {uploadingId ? 'Uploading...' : 'Upload'}
                          </button>
                        )}
                        <label htmlFor="sp-school-id" className="sp-btn-choose">
                          Change Photo
                        </label>
                      </div>
                    </>
                  ) : (
                    <label htmlFor="sp-school-id" className="sp-btn-choose sp-btn-upload-empty">
                      + Upload School ID
                    </label>
                  )}

                  <input
                    type="file"
                    id="sp-school-id"
                    accept="image/*"
                    style={{ display: 'none' }}
                    onChange={handleSchoolIdChange}
                  />
                </div>

              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default StudentProfile;