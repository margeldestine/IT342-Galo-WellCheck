import { useNavigate } from 'react-router-dom';
import '../styles/LandingPage.css';

function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="landing-wrapper">

      {/* Navbar */}
      <nav className="navbar">
        <div className="navbar-brand" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
          <div className="navbar-logo">♥</div>
          <span className="navbar-title">WellCheck</span>
        </div>
        <button className="btn-login" onClick={() => navigate('/login')}>
          Login
        </button>
      </nav>

      {/* Hero */}
      <div className="hero">
        <div className="hero-left">
          <div className="hero-badge">
            <span className="hero-badge-dot"></span>
            Guidance & Wellness Platform
          </div>
          <h1 className="hero-title">
            Book counseling<br />
            appointments <span>securely.</span>
          </h1>
          <p className="hero-subtitle">
            Connect with professional counselors who understand student life.
            Schedule appointments that fit your schedule and take control of
            your mental wellness journey.
          </p>
          <div className="hero-buttons">
            <button className="btn-primary" onClick={() => navigate('/register/student')}>
              Sign Up as Student 
            </button>
            <button className="btn-secondary" onClick={() => navigate('/register/counselor')}>
              Sign Up as Counselor
            </button>
          </div>
          <div className="hero-stats">
            <div className="hero-stat">
              <span className="hero-stat-value">100%</span>
              <span className="hero-stat-label">Confidential</span>
            </div>
            <div className="hero-stat">
              <span className="hero-stat-value">24/7</span>
              <span className="hero-stat-label">Booking access</span>
            </div>
            <div className="hero-stat">
              <span className="hero-stat-value">Fast</span>
              <span className="hero-stat-label">Approval process</span>
            </div>
          </div>
        </div>

        <div className="hero-right">
          <div className="feature-card feature-card-accent">
            <div className="feature-card-icon">🔒</div>
            <h3>Secure & Confidential</h3>
            <p>All appointments and conversations are protected with enterprise-grade encryption.</p>
          </div>
          <div className="feature-card">
            <div className="feature-card-icon">📅</div>
            <h3>Easy Scheduling</h3>
            <p>Browse available counselors and book your slot in just a few clicks.</p>
          </div>
          <div className="feature-card">
            <div className="feature-card-icon">🔔</div>
            <h3>Instant Notifications</h3>
            <p>Get notified via email when your appointment is approved or rejected.</p>
          </div>
        </div>
      </div>

      {/* How it works */}
      <div className="how-it-works">
        <p className="section-label">How it works</p>
        <h2 className="section-title">Get support in 3 simple steps</h2>
        <div className="steps-grid">
          <div className="step-card">
            <div className="step-number">1</div>
            <h3>Create an account</h3>
            <p>Sign up as a student and complete your profile to get started.</p>
          </div>
          <div className="step-card">
            <div className="step-number">2</div>
            <h3>Browse counselors</h3>
            <p>Find a counselor that fits your needs and view their available slots.</p>
          </div>
          <div className="step-card">
            <div className="step-number">3</div>
            <h3>Book & get support</h3>
            <p>Submit your booking and wait for confirmation. It's that simple.</p>
          </div>
        </div>
      </div>

      {/* Footer CTA */}
      <div className="footer-cta">
        <h2>Ready to get started?</h2>
        <p>Join students who are already managing their wellness with WellCheck.</p>
        <button className="btn-primary" onClick={() => navigate('/register/student')}>
          Create your free account →
        </button>
      </div>

      <div className="footer-bottom">
        <p>© 2026 WellCheck. All rights reserved.</p>
      </div>

    </div>
  );
}

export default LandingPage;