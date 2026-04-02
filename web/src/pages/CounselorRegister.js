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

  const validateEmployeeNumber = (empNum) => {
    const pattern = /^EMP-\d{4}-\d{5}$/;
    return pattern.test(empNum);
  };

  const handleSubmit = async () => {
    setError('');
    setSuccess('');

    if (!form.firstName || !form.lastName || !form.employeeNumber ||
        !form.specialization || !form.bio || !form.email || !form.password) {
      setError('Please fill in all fields.');
      return;
    }

    if (!validateEmployeeNumber(form.employeeNumber)) {
      setError('Employee number must follow the format EMP-0000-00000.');
      return;
    }

    if (form.password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }

    setLoading(true);
    try {
      await axios.post(`${API}/auth/register/counselor`, form);
      setSuccess('Registration submitted! Your account is pending admin approval.');

      localStorage.setItem('user', JSON.stringify({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        role: 'COUNSELOR'
      }));

      setTimeout(() => {
        window.location.href = '/pending';
      }, 2000);
    } catch (err) {
      setError(err.response?.data || 'Registration failed. Please try again.');
    }
    setLoading(false);
  };

  const handleGoogleSignUp = () => {
    window.location.href = `${API}/oauth2/authorization/google?role=COUNSELOR`;
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
          <input
            name="employeeNumber"
            placeholder="EMP-2024-12345"
            value={form.employeeNumber}
            onChange={handleChange}
          />
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
          <input
            type="email"
            name="email"
            placeholder="johndoe@gmail.com"
            value={form.email}
            onChange={handleChange}
          />
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

        <div className="divider"><span>or</span></div>

        <button className="btn-google" onClick={handleGoogleSignUp}>
          <svg width="18" height="18" viewBox="0 0 24 24">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
          </svg>
          Sign up with Google
        </button>

        <p className="register-footer">
          Already have an account? <a href="/login">Sign In</a>
        </p>
      </div>
    </div>
  );
}

export default CounselorRegister;