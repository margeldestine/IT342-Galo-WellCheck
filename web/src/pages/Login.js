import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/Login.css';

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
      const res = await axios.post('http://localhost:8080/auth/login', { email, password });
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
      else setError('Dashboard not available for your role yet.');
    } catch (err) {
      setError(err.response?.data || 'Invalid email or password.');
    }
    setLoading(false);
  };

  return (
    <div className="login-wrapper">
      <nav className="navbar">
        <div className="navbar-brand" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
          <div className="navbar-logo">♥</div>
          <span className="navbar-title">WellCheck</span>
        </div>
        <button className="btn-login">Login</button>
      </nav>

      <div className="login-card">
        <h2>Welcome Back!</h2>
        <p className="subtitle">Sign in to your WellCheck account</p>

        {error && <div className="error-msg">{error}</div>}

        <div className="form-group">
          <label>Email</label>
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
              {showPassword ? '🙈' : '👁'}
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