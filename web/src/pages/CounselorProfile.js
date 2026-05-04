import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import CounselorSidebar from '../components/CounselorSidebar';
import '../styles/CounselorProfile.css';

const API = process.env.REACT_APP_API_URL;

const DAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];

function StarDisplay({ rating }) {
  return (
    <div className="cpro-stars">
      {[1, 2, 3, 4, 5].map(i => (
        <svg key={i} className={`cpro-star ${i <= Math.round(rating) ? '' : 'cpro-star-empty'}`}
          viewBox="0 0 24 24" fill={i <= Math.round(rating) ? 'currentColor' : 'none'}
          stroke="currentColor" strokeWidth="1.5">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
        </svg>
      ))}
    </div>
  );
}

function CounselorProfile() {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');
  const userFromStorage = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = userFromStorage.firstName || '';
  const lastName = userFromStorage.lastName || '';

  const fileInputRef = useRef(null);

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const [ratingData, setRatingData] = useState({ average: 0, count: 0 });

  const [form, setForm] = useState({
    specialization: '',
    bio: '',
    yearsExperience: '',
    licenseNumber: '',
    availableDays: [],
  });

  // Credentials list: { title, year }
  const [credentials, setCredentials] = useState([]);
  const [newCred, setNewCred] = useState({ title: '', year: '' });

  useEffect(() => { fetchProfile(); }, []);

  const fetchProfile = async () => {
    setLoading(true);
    try {
      const res = await axios.get(`${API}/auth/profile/counselor`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setProfile(res.data);
      setForm({
        specialization: res.data.specialization || '',
        bio: res.data.bio || '',
        yearsExperience: res.data.yearsExperience || '',
        licenseNumber: res.data.licenseNumber || '',
        availableDays: res.data.availableDays || [],
      });

      setCredentials(
        (res.data.credentialEntries || []).map(c => {
          if (typeof c === 'string') {
            const [title, year] = c.split('|');
            return { title: title || '', year: year || '' };
          }
          return c;
        })
      );

      setRatingData({
        average: res.data.averageRating || 0,
        count: res.data.ratingCount || 0,
      });

      // Sync profile photo and specialization to localStorage so sidebar reflects it
      const updatedUser = {
        ...JSON.parse(localStorage.getItem('user') || '{}'),
        profilePhoto: res.data.profilePhoto || '',
        specialization: res.data.specialization || '',
      };
      localStorage.setItem('user', JSON.stringify(updatedUser));

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
      await axios.put(`${API}/auth/profile/counselor`, { ...form, credentials }, {
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

  const toggleDay = (day) => {
    setForm(prev => ({
      ...prev,
      availableDays: prev.availableDays.includes(day)
        ? prev.availableDays.filter(d => d !== day)
        : [...prev.availableDays, day]
    }));
  };

  const addCredential = () => {
    if (!newCred.title.trim()) return;
    setCredentials(prev => [...prev, { ...newCred }]);
    setNewCred({ title: '', year: '' });
  };

  const removeCredential = (index) => {
    setCredentials(prev => prev.filter((_, i) => i !== index));
  };

  if (loading) return (
    <div className="cpro-app">
      <CounselorSidebar activeItem="profile" />
      <main className="cpro-main"><p style={{ color: '#9ca3af', marginTop: 40 }}>Loading profile...</p></main>
    </div>
  );

  return (
    <div className="cpro-app">
      <CounselorSidebar activeItem="profile" />

      <main className="cpro-main">
        <div className="cpro-page-header">
          <div>
            <div className="cpro-page-title">My Profile</div>
            <div className="cpro-page-sub">Manage your counselor information, credentials, and availability.</div>
          </div>
        </div>

        {successMsg && <div className="cpro-success">{successMsg}</div>}
        {errorMsg && <div className="cpro-error">{errorMsg}</div>}

        <div className="cpro-layout">

          {/* ── LEFT PANEL ─────────────────────────────────────────── */}
          <div className="cpro-left-panel">

            {/* Identity Card */}
            <div className="cpro-identity-card">
              <div className="cpro-banner" />
              <div className="cpro-avatar-wrap">
                <div className="cpro-avatar" onClick={() => fileInputRef.current?.click()}>
                  {profile?.profilePhoto
                    ? <img src={profile.profilePhoto}
                        alt="avatar" style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }} />
                    : `${firstName.charAt(0)}${lastName.charAt(0)}`
                  }
                  <div className="cpro-avatar-overlay">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z"/>
                      <circle cx="12" cy="13" r="4"/>
                    </svg>
                  </div>
                </div>
                <input ref={fileInputRef} type="file" accept="image/*" style={{ display: 'none' }}
                  onChange={async (e) => {
                    const file = e.target.files[0];
                    if (!file) return;
                    const formData = new FormData();
                    formData.append('file', file);
                    try {
                      const res = await axios.post(`${API}/auth/profile/counselor/photo`, formData, {
                        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'multipart/form-data' }
                      });
                      const publicUrl = res.data;
                      const updatedUser = {
                        ...JSON.parse(localStorage.getItem('user') || '{}'),
                        profilePhoto: publicUrl,
                      };
                      localStorage.setItem('user', JSON.stringify(updatedUser));
                      fetchProfile();
                    } catch (err) {
                      setErrorMsg('Failed to upload photo.');
                    }
                  }} />
              </div>
              <div className="cpro-identity-body">
                <div className="cpro-full-name">{firstName} {lastName}</div>
                <div className="cpro-emp-id">ID: {profile?.employeeNumber || 'N/A'}</div>
                {form.specialization && (
                  <div className="cpro-spec-badge">{form.specialization}</div>
                )}
              </div>
            </div>

            {/* Rating Card */}
            <div className="cpro-rating-card">
              <div className="cpro-rating-label">Student Rating</div>
              <div className="cpro-rating-big">
                <div className="cpro-rating-number">{ratingData.average.toFixed(1)}</div>
                <div>
                  <StarDisplay rating={ratingData.average} />
                  <div className="cpro-rating-count">{ratingData.count} review{ratingData.count !== 1 ? 's' : ''}</div>
                </div>
              </div>
            </div>

          </div>

          {/* ── RIGHT PANEL ────────────────────────────────────────── */}
          <div className="cpro-right-panel">

            {/* Basic Info */}
            <div className="cpro-form-card">
              <div className="cpro-section-title">Basic Information</div>
              <div className="cpro-form-row single">
                <div className="cpro-form-group">
                  <label className="cpro-label">Email Address</label>
                  <input className="cpro-input" value={userFromStorage.email || ''} readOnly />
                </div>
              </div>
              <div className="cpro-form-row">
                <div className="cpro-form-group">
                  <label className="cpro-label">First Name</label>
                  <input className="cpro-input" value={firstName} readOnly />
                </div>
                <div className="cpro-form-group">
                  <label className="cpro-label">Last Name</label>
                  <input className="cpro-input" value={lastName} readOnly />
                </div>
              </div>
              <div className="cpro-form-row">
                <div className="cpro-form-group">
                  <label className="cpro-label">Specialization</label>
                  <div className="cpro-select-wrap">
                    <select className="cpro-select" value={form.specialization}
                      onChange={e => setForm({ ...form, specialization: e.target.value })}>
                      <option value="">Select Specialization</option>
                      <option value="Mental Health">Mental Health</option>
                      <option value="Career Counseling">Career Counseling</option>
                      <option value="Academic Counseling">Academic Counseling</option>
                      <option value="Family Counseling">Family Counseling</option>
                      <option value="Crisis Intervention">Crisis Intervention</option>
                    </select>
                  </div>
                </div>
                <div className="cpro-form-group">
                  <label className="cpro-label">Years of Experience</label>
                  <input className="cpro-input" type="number" min="0" placeholder="e.g. 5"
                    value={form.yearsExperience}
                    onChange={e => setForm({ ...form, yearsExperience: e.target.value })} />
                </div>
              </div>
              <div className="cpro-form-row single">
                <div className="cpro-form-group">
                  <label className="cpro-label">License / PRC Number</label>
                  <input className="cpro-input" placeholder="e.g. 0123456"
                    value={form.licenseNumber}
                    onChange={e => setForm({ ...form, licenseNumber: e.target.value })} />
                </div>
              </div>
              <div className="cpro-form-row single">
                <div className="cpro-form-group">
                  <label className="cpro-label">Bio / Description</label>
                  <textarea className="cpro-textarea"
                    placeholder="Tell students about your background and approach..."
                    value={form.bio}
                    onChange={e => setForm({ ...form, bio: e.target.value })} />
                </div>
              </div>
              <div className="cpro-form-actions">
                <button className="cpro-btn-primary" onClick={handleSave} disabled={saving}>
                  {saving ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </div>

            {/* Credentials */}
            <div className="cpro-form-card">
              <div className="cpro-section-title">Credentials & Education</div>
              <div className="cpro-creds-list">
                {credentials.length === 0 && (
                  <div style={{ fontSize: '13px', color: '#9ca3af', padding: '8px 0' }}>No credentials added yet.</div>
                )}
                {credentials.map((cred, i) => (
                  <div key={i} className="cpro-cred-item">
                    <span className="cpro-cred-title">{cred.title}</span>
                    <span className="cpro-cred-year">{cred.year}</span>
                    <button className="cpro-cred-remove" onClick={() => removeCredential(i)}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
              <div className="cpro-add-cred-row">
                <input className="cpro-input" placeholder="Degree / Certificate name"
                  value={newCred.title}
                  onChange={e => setNewCred({ ...newCred, title: e.target.value })} />
                <input className="cpro-input" placeholder="Year" type="number" min="1990" max="2099"
                  value={newCred.year}
                  onChange={e => setNewCred({ ...newCred, year: e.target.value })} />
                <button className="cpro-btn-add-cred" onClick={addCredential}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
                  </svg>
                  Add
                </button>
              </div>
              <div className="cpro-form-actions">
                <button className="cpro-btn-primary" onClick={handleSave} disabled={saving}>
                  Save Credentials
                </button>
              </div>
            </div>

            {/* Availability */}
            <div className="cpro-form-card">
              <div className="cpro-section-title">Preferred Availability</div>
              <div className="cpro-form-group">
                <label className="cpro-label">Available Days</label>
                <div className="cpro-avail-tags">
                  {DAYS.map(day => (
                    <button key={day}
                      className={`cpro-avail-tag ${form.availableDays.includes(day) ? 'active' : ''}`}
                      onClick={() => toggleDay(day)}>
                      {day}
                    </button>
                  ))}
                </div>
              </div>
              <div className="cpro-form-actions">
                <button className="cpro-btn-primary" onClick={handleSave} disabled={saving}>
                  Save Availability
                </button>
              </div>
            </div>

          </div>
        </div>
      </main>
    </div>
  );
}

export default CounselorProfile;