import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/Login.css';

const API = process.env.REACT_APP_API_URL;

function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    setError('');
    setLoading(true);
    try {
      const res = await axios.post(`${API}/auth/login`, { email, password });
      const { accessToken, role, status } = res.data;

      if (status === 'PENDING') {
        setError('Your account is pending approval by the admin.');
        setLoading(false);
        return;
      }

      localStorage.setItem('token', accessToken);
      localStorage.setItem('role', role);
      localStorage.setItem('user', JSON.stringify(res.data));

      if (role === 'STUDENT') navigate('/dashboard');
      else if (role === 'COUNSELOR') navigate('/counselor/dashboard');
      else if (role === 'ADMIN') navigate('/admin/dashboard');

    } catch (err) {
      setError(err.response?.data || 'Invalid email or password.');
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
    <div className="login-wrapper">
      <nav className="navbar">
        <div className="navbar-brand" onClick={() => navigate('/')}>
          <div className="navbar-logo">♥</div>
          <span className="navbar-title">WellCheck</span>
        </div>
        <button className="btn-nav-login" onClick={() => navigate('/login')}>Login</button>
      </nav>

      <div className="login-card">
        <h2>Welcome Back!</h2>
        <p className="subtitle">Sign in to your WellCheck account</p>

        {error && <div className="error-msg">{error}</div>}

        <div className="form-group">
          <label>Email Address</label>
          <input
            type="email"
            placeholder="johndoe@gmail.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label>Password</label>
          <div className="password-wrapper">
            <input
              type={showPassword ? 'text' : 'password'}
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <button className="password-toggle" onClick={() => setShowPassword(!showPassword)}>
              {showPassword ? <EyeOffIcon /> : <EyeIcon />}
            </button>
          </div>
        </div>

        <button className="btn-signin" onClick={handleLogin} disabled={loading}>
          {loading ? 'Signing in...' : 'Sign In'}
        </button>

        <p className="login-footer">
          Don't have an account? <a href="/register/student">Sign Up</a>
        </p>
      </div>
    </div>
  );
}

export default Login;