import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import StudentTopbar from '../components/StudentTopbar';
import StudentSidebar from '../components/StudentSidebar';
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
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

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

  return (
    <div className="sp-wrapper">
      <StudentTopbar />

      <div className="sp-container">
        <StudentSidebar activeItem="profile" />

        <main className="sp-main">
          {loading ? (
            <div className="sp-loading">Loading your profile...</div>
          ) : (
            <>
              {!profile?.schoolIdPhotoUrl && (
                <div className="sp-banner">
                  ℹ️ Complete your profile to get started and access all features!
                </div>
              )}

              <div className="sp-card">
                <div className="sp-avatar-section">
                  <div className="sp-avatar-circle">
                    {firstName.charAt(0)}{lastName.charAt(0)}
                  </div>
                </div>

                <div className="sp-field">
                  <label className="sp-label">Student ID Number</label>
                  <input className="sp-input" value={profile?.studentIdNumber || ''} readOnly />
                </div>

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

                <div className="sp-field">
                  <label className="sp-label">Enrolled Program</label>
                  <select className="sp-select" value={profile?.program || ''} disabled>
                    <option value="BSIT">BSIT</option>
                    <option value="BSCS">BSCS</option>
                  </select>
                </div>

                <div className="sp-row">
                  <div className="sp-field">
                    <label className="sp-label">Year Level</label>
                    <select className="sp-select" value={profile?.yearLevel || ''} disabled>
                      <option value="3">3rd Year</option>
                    </select>
                  </div>
                  <div className="sp-field">
                    <label className="sp-label">Gender</label>
                    <select className="sp-select" value={profile?.gender || ''} disabled>
                      <option value="Female">Female</option>
                    </select>
                  </div>
                </div>

                <div className="sp-field">
                  <label className="sp-label">Birthdate</label>
                  <input className="sp-input" value={profile?.birthdate || ''} readOnly />
                </div>

                <div className="sp-school-id-section">
                  <label className="sp-label">School ID Photo</label>
                  <input type="file" id="sp-school-id" style={{ display: 'none' }} onChange={handleSchoolIdChange} />
                  
                  {profile?.schoolIdPhotoUrl || schoolIdPreview ? (
                    <>
                      <img src={schoolIdPreview || profile.schoolIdPhotoUrl} alt="School ID" className="sp-id-img" />
                      <div className="sp-upload-row">
                        {schoolIdFile && <button className="sp-btn-upload" onClick={handleUploadSchoolId}>Upload</button>}
                        <label htmlFor="sp-school-id" className="sp-btn-choose">Change Photo</label>
                      </div>
                    </>
                  ) : (
                    <label htmlFor="sp-school-id" className="sp-btn-choose">Upload Photo</label>
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