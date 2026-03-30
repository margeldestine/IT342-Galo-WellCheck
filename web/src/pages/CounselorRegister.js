import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/CounselorRegister.css';

const API = process.env.REACT_APP_API_URL;

function CounselorRegister() {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    employeeNumber: '',
    specialization: '',
    bio: '',
    email: '',
    password: '',
  });

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async () => {
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await axios.post(`${API}/auth/register/counselor`, form);
      setSuccess('Registration submitted! Your account is pending admin approval.');
    } catch (err) {
      setError(err.response?.data || 'Registration failed. Please try again.');
    }
    setLoading(false);
  };

  const EyeIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );

  const EyeOffIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
      <line x1="1" y1="1" x2="23" y2="23" />
    </svg>
  );

  return (
    <div className="register-wrapper">
      <nav className="navbar">
        <div className="navbar-brand" onClick={() => navigate('/')}>
          <div className="navbar-logo">♥</div>
          <span className="navbar-title">WellCheck</span>
        </div>
        <button className="btn-login" onClick={() => navigate('/login')}>Login</button>
      </nav>

      <div className="register-card">
        <h2>Create Counselor Account</h2>
        <p className="subtitle">Register to manage student appointments and availability.</p>

        {error && <div className="error-msg">{error}</div>}
        {success && <div className="success-msg">{success}</div>}

        <div className="section-divider"><span>PERSONAL INFORMATION</span></div>

        <div className="form-row">
          <div className="form-group">
            <label>First Name</label>
            <input name="firstName" placeholder="John" value={form.firstName} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Last Name</label>
            <input name="lastName" placeholder="Doe" value={form.lastName} onChange={handleChange} />
          </div>
        </div>

        <div className="section-divider"><span>PROFESSIONAL INFORMATION</span></div>

        <div className="form-group">
          <label>Employee Number</label>
          <input name="employeeNumber" placeholder="EMP-2024-12345" value={form.employeeNumber} onChange={handleChange} />
        </div>

        <div className="form-group">
          <label>Specialization</label>
          <div className="select-wrapper">
            <select name="specialization" value={form.specialization} onChange={handleChange}>
              <option value="">Select Specialization</option>
              <option value="Mental Health">Mental Health</option>
              <option value="Career Counseling">Career Counseling</option>
              <option value="Academic Counseling">Academic Counseling</option>
              <option value="Family Counseling">Family Counseling</option>
              <option value="Crisis Intervention">Crisis Intervention</option>
            </select>
          </div>
        </div>

        <div className="form-group">
          <label>Short Bio / Description</label>
          <textarea
            name="bio"
            placeholder="Tell students about your background and approach..."
            value={form.bio}
            onChange={handleChange}
            rows={4}
          />
        </div>

        <div className="section-divider"><span>ACCOUNT CREDENTIALS</span></div>

        <div className="form-group">
          <label>Email Address</label>
          <input type="email" name="email" placeholder="johndoe@gmail.com" value={form.email} onChange={handleChange} />
        </div>

        <div className="form-group">
          <label>Password</label>
          <div className="password-wrapper">
            <input
              type={showPassword ? 'text' : 'password'}
              name="password"
              placeholder="Minimum 8 characters"
              value={form.password}
              onChange={handleChange}
            />
            <button className="password-toggle" onClick={() => setShowPassword(!showPassword)}>
              {showPassword ? <EyeOffIcon /> : <EyeIcon />}
            </button>
          </div>
        </div>

        <button className="btn-register" onClick={handleSubmit} disabled={loading}>
          {loading ? 'Submitting...' : 'Submit Registration'}
        </button>

        <p className="register-footer">
          Already have an account? <a href="/login">Sign In</a>
        </p>
      </div>
    </div>
  );
}

export default CounselorRegister;