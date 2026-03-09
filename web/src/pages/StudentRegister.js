import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/StudentRegister.css';

function StudentRegister() {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState({
    studentIdNumber: '',
    firstName: '',
    lastName: '',
    program: '',
    yearLevel: '',
    gender: '',
    birthdate: '',
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
      await axios.post('http://localhost:8080/auth/register/student', form);
      setSuccess('Account created successfully! Redirecting to login...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(err.response?.data || 'Registration failed. Please try again.');
    }
    setLoading(false);
  };

  return (
    <div className="register-wrapper">
      <nav className="navbar">
        <div className="navbar-brand" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
          <div className="navbar-logo">♥</div>
          <span className="navbar-title">WellCheck</span>
        </div>
        <button className="btn-login" onClick={() => navigate('/login')}>Login</button>
      </nav>

      <div className="register-card">
        <h2>Create Student Account</h2>
        <p className="subtitle">Register to book counseling appointments securely.</p>

        {error && <div className="error-msg">{error}</div>}
        {success && <div className="success-msg">{success}</div>}

        <div className="section-title">Personal Information</div>

        <div className="form-group">
          <label>Student ID Number</label>
          <input name="studentIdNumber" placeholder="2023-12345" value={form.studentIdNumber} onChange={handleChange} />
        </div>

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

        <div className="form-group">
          <label>Enrolled Program</label>
          <div className="select-wrapper">
            <select name="program" value={form.program} onChange={handleChange}>
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

        <div className="form-row">
          <div className="form-group">
            <label>Year Level</label>
            <div className="select-wrapper">
              <select name="yearLevel" value={form.yearLevel} onChange={handleChange}>
                <option value="">Select</option>
                <option value="1">1st Year</option>
                <option value="2">2nd Year</option>
                <option value="3">3rd Year</option>
                <option value="4">4th Year</option>
              </select>
            </div>
          </div>
          <div className="form-group">
            <label>Gender</label>
            <div className="select-wrapper">
              <select name="gender" value={form.gender} onChange={handleChange}>
                <option value="">Select</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
              </select>
            </div>
          </div>
        </div>

        <div className="form-group">
          <label>Birthdate</label>
          <input type="date" name="birthdate" value={form.birthdate} onChange={handleChange} />
        </div>

        <div className="section-title">Account Credentials</div>

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
              {showPassword ? '🙈' : '👁'}
            </button>
          </div>
        </div>

        <button className="btn-register" onClick={handleSubmit} disabled={loading}>
          {loading ? 'Creating Account...' : 'Create Account'}
        </button>

        <p className="register-footer">
          Already have an account? <a href="/login">Sign In</a>
        </p>
      </div>
    </div>
  );
}

export default StudentRegister;