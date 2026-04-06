import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/CounselorProfile.css';

const API = process.env.REACT_APP_API_URL;

function CounselorProfile() {
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

  const [form, setForm] = useState({
    specialization: '',
    bio: ''
  });

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    setLoading(true);
    try {
      const res = await axios.get(`${API}/auth/profile/counselor`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setProfile(res.data);
      setForm({
        specialization: res.data.specialization || '',
        bio: res.data.bio || ''
      });
    } catch (err) {
      console.error('Failed to fetch profile:', err);
    }
    setLoading(false);
  };

  const handleSave = async () => {
    setSaving(true);
    setSuccessMsg('');
    setErrorMsg('');
    try {
      await axios.put(`${API}/auth/profile/counselor`, form, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setSuccessMsg('Profile updated successfully!');
      setTimeout(() => setSuccessMsg(''), 3000);
      fetchProfile();
    } catch (err) {
      setErrorMsg(err.response?.data || 'Failed to update profile.');
    }
    setSaving(false);
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  return (
    <div className="cp-wrapper">

      {/* Navbar */}
      <nav className="cp-navbar">
        <div className="cp-brand" onClick={() => navigate('/counselor/dashboard')}>
          <div className="cp-logo">♥</div>
          <div>
            <div className="cp-title">WellCheck</div>
            <div className="cp-subtitle">Counselor Portal</div>
          </div>
        </div>
        <div className="cp-user">
          <span className="cp-username">{firstName} {lastName}</span>
          <div className="cp-avatar">{firstName.charAt(0)}</div>
        </div>
      </nav>

      <div className="cp-container">

        {/* Sidebar */}
        <aside className="cp-sidebar">
          <nav className="cp-nav">
            <div className="cp-nav-item" onClick={() => navigate('/counselor/dashboard')}>
              <span>🏠</span> Dashboard
            </div>
            <div className="cp-nav-item" onClick={() => navigate('/counselor/dashboard')}>
              <span>📅</span> Manage Slots
            </div>
            <div className="cp-nav-item" onClick={() => navigate('/counselor/dashboard')}>
              <span>📋</span> Requests
            </div>
            <div className="cp-nav-item active">
              <span>👤</span> Profile
            </div>
          </nav>
          <div className="cp-nav-logout" onClick={handleLogout}>
            <span>↪</span> Log Out
          </div>
        </aside>

        {/* Main */}
        <main className="cp-main">
          {loading ? (
            <div className="cp-loading">Loading your profile...</div>
          ) : (
            <div className="cp-card">

              {/* Avatar */}
              <div className="cp-avatar-section">
                <div className="cp-avatar-circle">
                  {firstName.charAt(0)}{lastName.charAt(0)}
                </div>
              </div>

              {successMsg && <div className="cp-success">{successMsg}</div>}
              {errorMsg && <div className="cp-error">{errorMsg}</div>}

              {/* Employee Number */}
              <div className="cp-field">
                <label className="cp-label">Employee ID Number</label>
                <input
                  className="cp-input"
                  value={profile?.employeeNumber || ''}
                  readOnly
                />
              </div>

              {/* Name */}
              <div className="cp-row">
                <div className="cp-field">
                  <label className="cp-label">First Name</label>
                  <input className="cp-input" value={firstName} readOnly />
                </div>
                <div className="cp-field">
                  <label className="cp-label">Last Name</label>
                  <input className="cp-input" value={lastName} readOnly />
                </div>
              </div>

              {/* Specialization */}
              <div className="cp-field">
                <label className="cp-label">Specialization</label>
                <div className="cp-select-wrapper">
                  <select
                    className="cp-select"
                    value={form.specialization}
                    onChange={(e) => setForm({ ...form, specialization: e.target.value })}
                  >
                    <option value="">Select Specialization</option>
                    <option value="Mental Health">Mental Health</option>
                    <option value="Career Counseling">Career Counseling</option>
                    <option value="Academic Counseling">Academic Counseling</option>
                    <option value="Family Counseling">Family Counseling</option>
                    <option value="Crisis Intervention">Crisis Intervention</option>
                  </select>
                </div>
              </div>

              {/* Bio */}
              <div className="cp-field">
                <label className="cp-label">Description</label>
                <textarea
                  className="cp-textarea"
                  rows={4}
                  placeholder="Tell students about your background and approach..."
                  value={form.bio}
                  onChange={(e) => setForm({ ...form, bio: e.target.value })}
                />
              </div>

              {/* Save Button */}
              <button
                className="cp-btn-save"
                onClick={handleSave}
                disabled={saving}
              >
                {saving ? 'Saving...' : 'Save Profile Changes'}
              </button>

            </div>
          )}
        </main>
      </div>
    </div>
  );
}

export default CounselorProfile;