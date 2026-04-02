import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/CompleteCounselorProfile.css';

const API = process.env.REACT_APP_API_URL;

function CompleteCounselorProfile() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const token = localStorage.getItem('token');

  const [form, setForm] = useState({
    employeeNumber: '',
    specialization: '',
    bio: '',
  });

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const validateEmployeeNumber = (empNum) => {
    const pattern = /^EMP-\d{4}-\d{5}$/;
    return pattern.test(empNum);
  };

  const handleSubmit = async () => {
    setError('');

    if (!form.employeeNumber || !form.specialization || !form.bio) {
      setError('Please fill in all fields.');
      return;
    }

    if (!validateEmployeeNumber(form.employeeNumber)) {
      setError('Employee number must follow the format EMP-0000-00000.');
      return;
    }

    setLoading(true);
    try {
      await axios.post(`${API}/auth/complete-counselor-profile`, form, {
        headers: { Authorization: `Bearer ${token}` }
      });
      window.location.href = '/pending';
    } catch (err) {
      setError(err.response?.data || 'Failed to complete profile. Please try again.');
    }
    setLoading(false);
  };

  return (
    <div className="cc-wrapper">
      <nav className="cc-navbar">
        <div className="cc-brand" onClick={() => navigate('/')}>
          <div className="cc-logo">♥</div>
          <span className="cc-title">WellCheck</span>
        </div>
      </nav>

      <div className="cc-card">
        <h2 className="cc-heading">Complete Your Profile</h2>
        <p className="cc-subtitle">
          Welcome, {user.firstName}! Please fill in your professional details to continue.
        </p>

        {error && <div className="cc-error">{error}</div>}

        <div className="cc-section-divider">
          <span>PROFESSIONAL INFORMATION</span>
        </div>

        <div className="cc-form-group">
          <label className="cc-label">Employee Number</label>
          <input
            className="cc-input"
            name="employeeNumber"
            placeholder="EMP-2024-12345"
            value={form.employeeNumber}
            onChange={handleChange}
          />
        </div>

        <div className="cc-form-group">
          <label className="cc-label">Specialization</label>
          <div className="cc-select-wrapper">
            <select className="cc-input" name="specialization" value={form.specialization} onChange={handleChange}>
              <option value="">Select Specialization</option>
              <option value="Mental Health">Mental Health</option>
              <option value="Career Counseling">Career Counseling</option>
              <option value="Academic Counseling">Academic Counseling</option>
              <option value="Family Counseling">Family Counseling</option>
              <option value="Crisis Intervention">Crisis Intervention</option>
            </select>
          </div>
        </div>

        <div className="cc-form-group">
          <label className="cc-label">Short Bio / Description</label>
          <textarea
            className="cc-input cc-textarea"
            name="bio"
            placeholder="Tell students about your background and approach..."
            value={form.bio}
            onChange={handleChange}
            rows={4}
          />
        </div>

        <button className="cc-btn" onClick={handleSubmit} disabled={loading}>
          {loading ? 'Saving...' : 'Complete Profile'}
        </button>
      </div>
    </div>
  );
}

export default CompleteCounselorProfile;