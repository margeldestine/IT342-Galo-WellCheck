import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/StudentProfile.css';

const API = process.env.REACT_APP_API_URL;

function StudentProfile() {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');
  const userFromStorage = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = userFromStorage.firstName || '';
  const lastName = userFromStorage.lastName || '';

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  // School ID upload
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
    setUploadError('');
    setUploadSuccess('');
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

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  return (
    <div className="sp-wrapper">

      {/* Navbar */}
      <nav className="sp-navbar">
        <div className="sp-brand" onClick={() => navigate('/dashboard')}>
          <div className="sp-logo">♥</div>
          <div>
            <div className="sp-title">WellCheck</div>
            <div className="sp-subtitle">Student Portal</div>
          </div>
        </div>
        <div className="sp-user">
          <span className="sp-username">{firstName} {lastName}</span>
          <div className="sp-avatar">{firstName.charAt(0)}</div>
        </div>
      </nav>

      <div className="sp-container">

        {/* Sidebar */}
        <aside className="sp-sidebar">
          <nav className="sp-nav">
            <div className="sp-nav-item" onClick={() => navigate('/dashboard')}>
              <span>🏠</span> Dashboard
            </div>
            <div className="sp-nav-item" onClick={() => navigate('/dashboard')}>
              <span>👥</span> Browse Counselors
            </div>
            <div className="sp-nav-item" onClick={() => navigate('/dashboard')}>
              <span>📅</span> My Appointments
            </div>
            <div className="sp-nav-item active">
              <span>👤</span> Profile
            </div>
          </nav>
          <div className="sp-nav-logout" onClick={handleLogout}>
            <span>↪</span> Log Out
          </div>
        </aside>

        {/* Main */}
        <main className="sp-main">

          {loading ? (
            <div className="sp-loading">Loading your profile...</div>
          ) : (
            <>
              {/* Banner */}
              {!profile?.schoolIdPhotoUrl && (
                <div className="sp-banner">
                  ℹ️ Complete your profile to get started and access all features!
                </div>
              )}

              <div className="sp-card">

                {/* Avatar */}
                <div className="sp-avatar-section">
                  <div className="sp-avatar-circle">
                    {firstName.charAt(0)}{lastName.charAt(0)}
                  </div>
                </div>

                {successMsg && <div className="sp-success">{successMsg}</div>}
                {errorMsg && <div className="sp-error">{errorMsg}</div>}

                {/* Student ID */}
                <div className="sp-field">
                  <label className="sp-label">Student ID Number</label>
                  <input
                    className="sp-input"
                    value={profile?.studentIdNumber || ''}
                    readOnly
                  />
                </div>

                {/* Name */}
                <div className="sp-row">
                  <div className="sp-field">
                    <label className="sp-label">First Name</label>
                    <input className="sp-input" value={firstName} readOnly />
                  </div>
                  <div className="sp-field">
                    <label className="sp-label">Last Name</label>
                    <input className="sp-input" value={lastName} readOnly />
                  </div>
                </div>

                {/* Program */}
                <div className="sp-field">
                  <label className="sp-label">Enrolled Program</label>
                  <div className="sp-select-wrapper">
                    <select className="sp-select" value={profile?.program || ''} disabled>
                      <option value="">Select Program</option>
                      <option value="BSIT">BSIT</option>
                      <option value="BSCS">BSCS</option>
                      <option value="BSIS">BSIS</option>
                      <option value="BSA">BSA</option>
                      <option value="BSBA">BSBA</option>
                      <option value="BSN">BSN</option>
                    </select>
                  </div>
                </div>

                {/* Year Level & Gender */}
                <div className="sp-row">
                  <div className="sp-field">
                    <label className="sp-label">Year Level</label>
                    <div className="sp-select-wrapper">
                      <select className="sp-select" value={profile?.yearLevel || ''} disabled>
                        <option value="">Select</option>
                        <option value="1">1st Year</option>
                        <option value="2">2nd Year</option>
                        <option value="3">3rd Year</option>
                        <option value="4">4th Year</option>
                      </select>
                    </div>
                  </div>
                  <div className="sp-field">
                    <label className="sp-label">Gender</label>
                    <div className="sp-select-wrapper">
                      <select className="sp-select" value={profile?.gender || ''} disabled>
                        <option value="">Select</option>
                        <option value="Male">Male</option>
                        <option value="Female">Female</option>
                        <option value="Other">Other</option>
                      </select>
                    </div>
                  </div>
                </div>

                {/* Birthdate */}
                <div className="sp-field">
                  <label className="sp-label">Birthdate</label>
                  <input className="sp-input" value={profile?.birthdate || ''} readOnly />
                </div>

                {/* School ID Photo */}
                <div className="sp-school-id-section">
                  <label className="sp-label">School ID Photo</label>

                  {profile?.schoolIdPhotoUrl ? (
                    <div className="sp-id-uploaded">
                      <img
                        src={profile.schoolIdPhotoUrl}
                        alt="School ID"
                        className="sp-id-img"
                      />
                      {uploadSuccess && <div className="sp-upload-success">{uploadSuccess}</div>}
                      <input
                        type="file"
                        accept="image/*"
                        id="sp-school-id"
                        style={{ display: 'none' }}
                        onChange={handleSchoolIdChange}
                      />
                      {schoolIdFile ? (
                        <div className="sp-upload-row">
                          <span className="sp-filename">{schoolIdFile.name}</span>
                          <button className="sp-btn-upload" onClick={handleUploadSchoolId} disabled={uploadingId}>
                            {uploadingId ? 'Uploading...' : 'Upload'}
                          </button>
                          <label htmlFor="sp-school-id" className="sp-btn-change">Change</label>
                        </div>
                      ) : (
                        <label htmlFor="sp-school-id" className="sp-btn-change-photo">
                          🔄 Change Photo
                        </label>
                      )}
                      {uploadError && <div className="sp-upload-error">{uploadError}</div>}
                    </div>
                  ) : (
                    <div className="sp-id-empty">
                      <div className="sp-id-placeholder">
                        <div className="sp-id-icon">🪪</div>
                        <input
                          type="file"
                          accept="image/*"
                          id="sp-school-id"
                          style={{ display: 'none' }}
                          onChange={handleSchoolIdChange}
                        />
                        {schoolIdPreview && (
                          <img src={schoolIdPreview} alt="Preview" className="sp-id-img" />
                        )}
                        {schoolIdFile ? (
                          <div className="sp-upload-row">
                            <span className="sp-filename">{schoolIdFile.name}</span>
                            <button className="sp-btn-upload" onClick={handleUploadSchoolId} disabled={uploadingId}>
                              {uploadingId ? 'Uploading...' : 'Upload'}
                            </button>
                            <label htmlFor="sp-school-id" className="sp-btn-change">Change</label>
                          </div>
                        ) : (
                          <label htmlFor="sp-school-id" className="sp-btn-choose">
                            📷 Upload Photo
                          </label>
                        )}
                        {uploadError && <div className="sp-upload-error">{uploadError}</div>}
                        {uploadSuccess && <div className="sp-upload-success">{uploadSuccess}</div>}
                      </div>
                    </div>
                  )}
                </div>

              </div>
            </>
          )}
        </main>
      </div>
    </div>
  );
}

export default StudentProfile;