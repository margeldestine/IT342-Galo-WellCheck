import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/CompleteProfile.css';

const API = process.env.REACT_APP_API_URL;

function CompleteProfile() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const token = localStorage.getItem('token');

  const [form, setForm] = useState({
    studentIdNumber: '',
    program: '',
    yearLevel: '',
    gender: '',
    birthdate: '',
  });

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const validateStudentId = (id) => {
    const pattern1 = /^\d{4}-\d{5}$/;
    const pattern2 = /^\d{2}-\d{4}-\d{3}$/;
    return pattern1.test(id) || pattern2.test(id);
  };

  const handleSubmit = async () => {
    setError('');

    if (!validateStudentId(form.studentIdNumber)) {
      setError('Student ID must follow the format 0000-00000 or 00-0000-000.');
      return;
    }

    if (!form.program || !form.yearLevel || !form.gender || !form.birthdate) {
      setError('Please fill in all fields.');
      return;
    }

    setLoading(true);
    try {
      await axios.post(`${API}/auth/complete-profile`, form, {
        headers: { Authorization: `Bearer ${token}` }
      });
      window.location.href = '/dashboard';
    } catch (err) {
      setError(err.response?.data || 'Failed to complete profile. Please try again.');
    }
    setLoading(false);
  };

  return (
    <div className="complete-wrapper">
      <nav className="complete-navbar">
        <div className="complete-brand" onClick={() => navigate('/')}>
          <div className="complete-logo">♥</div>
          <span className="complete-title">WellCheck</span>
        </div>
      </nav>

      <div className="complete-card">
        <h2 className="complete-heading">Complete Your Profile</h2>
        <p className="complete-subtitle">
          Welcome, {user.firstName}! Please fill in your student details to continue.
        </p>

        {error && <div className="complete-error">{error}</div>}

        <div className="complete-section-divider">
          <span>STUDENT INFORMATION</span>
        </div>

        <div className="complete-form-group">
          <label className="complete-label">Student ID Number</label>
          <input
            className="complete-input"
            name="studentIdNumber"
            placeholder="2023-12345 or 23-2425-001"
            value={form.studentIdNumber}
            onChange={handleChange}
          />
        </div>

        <div className="complete-form-group">
          <label className="complete-label">Enrolled Program</label>
          <div className="complete-select-wrapper">
            <select className="complete-input" name="program" value={form.program} onChange={handleChange}>
              <option value="">Select Your Program</option>
              <option value="BSIT">BSIT</option>
              <option value="BSCS">BSCS</option>
              <option value="BSIS">BSIS</option>
              <option value="BSA">BSA</option>
              <option value="BSBA">BSBA</option>
              <option value="BSN">BSN</option>
            </select>
          </div>
        </div>

        <div className="complete-row">
          <div className="complete-form-group">
            <label className="complete-label">Year Level</label>
            <div className="complete-select-wrapper">
              <select className="complete-input" name="yearLevel" value={form.yearLevel} onChange={handleChange}>
                <option value="">Select</option>
                <option value="1">1st Year</option>
                <option value="2">2nd Year</option>
                <option value="3">3rd Year</option>
                <option value="4">4th Year</option>
              </select>
            </div>
          </div>
          <div className="complete-form-group">
            <label className="complete-label">Gender</label>
            <div className="complete-select-wrapper">
              <select className="complete-input" name="gender" value={form.gender} onChange={handleChange}>
                <option value="">Select</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
              </select>
            </div>
          </div>
        </div>

        <div className="complete-form-group">
          <label className="complete-label">Birthdate</label>
            <input 
            type="date" 
            name="birthdate" 
            value={form.birthdate} 
            onChange={handleChange}
            max={new Date().toISOString().split('T')[0]}
            />
        </div>

        <button className="complete-btn" onClick={handleSubmit} disabled={loading}>
          {loading ? 'Saving...' : 'Complete Profile'}
        </button>
      </div>
    </div>
  );
}

export default CompleteProfile;