import { useNavigate } from 'react-router-dom';
import '../styles/LandingPage.css';
import logo from '../assets/wellcheck-logo.png';

function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="lp-wrapper">

      {/* Navbar */}
      <nav className="lp-navbar">
        <div className="lp-navbar-brand" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
          <img src={logo} alt="WellCheck" style={{ width: '36px', height: '36px' }} />
          <span className="lp-navbar-title">WellCheck</span>
        </div>
        <button className="lp-btn-login" onClick={() => navigate('/login')}>
          Login
        </button>
      </nav>

      {/* Hero */}
      <div className="lp-hero">
        <div className="lp-hero-left">
          <div className="lp-hero-badge">
            <span className="lp-hero-badge-dot"></span>
            Guidance & Wellness Platform
          </div>
          <h1 className="lp-hero-title">
            Book counseling<br />
            appointments <span>securely.</span>
          </h1>
          <p className="lp-hero-subtitle">
            Connect with professional counselors who understand student life.
            Schedule appointments that fit your schedule and take control of
            your mental wellness journey.
          </p>
          <div className="lp-hero-buttons">
            <button className="lp-btn-primary" onClick={() => navigate('/register/student')}>
              Sign Up as Student
            </button>
            <button className="lp-btn-secondary" onClick={() => navigate('/register/counselor')}>
              Sign Up as Counselor
            </button>
          </div>
          <div className="lp-hero-stats">
            <div className="lp-hero-stat">
              <span className="lp-hero-stat-value">100%</span>
              <span className="lp-hero-stat-label">Confidential</span>
            </div>
            <div className="lp-hero-stat">
              <span className="lp-hero-stat-value">24/7</span>
              <span className="lp-hero-stat-label">Booking access</span>
            </div>
            <div className="lp-hero-stat">
              <span className="lp-hero-stat-value">Fast</span>
              <span className="lp-hero-stat-label">Approval process</span>
            </div>
          </div>
        </div>

        <div className="lp-hero-right">
          <div className="lp-feature-card lp-feature-card-accent">
            <div className="lp-feature-card-icon">🔒</div>
            <h3>Secure & Confidential</h3>
            <p>All appointments and conversations are protected with enterprise-grade encryption.</p>
          </div>
          <div className="lp-feature-card">
            <div className="lp-feature-card-icon">📅</div>
            <h3>Easy Scheduling</h3>
            <p>Browse available counselors and book your slot in just a few clicks.</p>
          </div>
          <div className="lp-feature-card">
            <div className="lp-feature-card-icon">🔔</div>
            <h3>Instant Notifications</h3>
            <p>Get notified via email when your appointment is approved or rejected.</p>
          </div>
        </div>
      </div>

      {/* How it works */}
      <div className="lp-how-it-works">
        <p className="lp-section-label">How it works</p>
        <h2 className="lp-section-title">Get support in 3 simple steps</h2>
        <div className="lp-steps-grid">
          <div className="lp-step-card">
            <div className="lp-step-number">1</div>
            <h3>Create an account</h3>
            <p>Sign up as a student and complete your profile to get started.</p>
          </div>
          <div className="lp-step-card">
            <div className="lp-step-number">2</div>
            <h3>Browse counselors</h3>
            <p>Find a counselor that fits your needs and view their available slots.</p>
          </div>
          <div className="lp-step-card">
            <div className="lp-step-number">3</div>
            <h3>Book & get support</h3>
            <p>Submit your booking and wait for confirmation. It's that simple.</p>
          </div>
        </div>
      </div>

      {/* Footer CTA */}
      <div className="lp-footer-cta">
        <h2>Ready to get started?</h2>
        <p>Join students who are already managing their wellness with WellCheck.</p>
        <button className="lp-btn-primary" onClick={() => navigate('/register/student')}>
          Create your free account →
        </button>
      </div>

      <div className="lp-footer-bottom">
        <p>© 2026 WellCheck. All rights reserved.</p>
      </div>

    </div>
  );
}

export default LandingPage;