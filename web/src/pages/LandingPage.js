import { useNavigate } from 'react-router-dom';
import '../styles/LandingPage.css';

function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="landing-wrapper">
      <nav className="navbar">
        <div className="navbar-brand" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
          <div className="navbar-logo">♥</div>
          <span className="navbar-title">WellCheck</span>
        </div>
        <button className="btn-login" onClick={() => navigate('/login')}>
          Login
        </button>
      </nav>

      <div className="hero">
        <div className="hero-left">
          <h1 className="hero-title">
            Book counseling appointments securely. Get support faster.
          </h1>
          <p className="hero-subtitle">
            Connect with professional counselors who understand student life.
            Schedule appointments that fit your schedule, receive support when
            you need it most, and take control of your mental wellness journey.
          </p>
          <div className="hero-buttons">
            <button className="btn-primary" onClick={() => navigate('/register/student')}>
              Sign Up as Student
            </button>

          </div>
        </div>

        <div className="hero-card">
          <div className="hero-card-icon">♥</div>
          <h3>Secure & Confidential</h3>
          <p>
            Your privacy is our priority. All appointments and conversations
            are protected with enterprise-grade encryption.
          </p>
        </div>
      </div>
    </div>
  );
}

export default LandingPage;