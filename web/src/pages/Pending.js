import { useNavigate } from 'react-router-dom';
import '../styles/Pending.css';

function Pending() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Counselor';
  const email = user.email || '';

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  return (
    <div className="pending-wrapper">
      <nav className="pending-navbar">
        <div className="pending-brand" onClick={() => navigate('/')}>
          <div className="pending-logo">♥</div>
          <span className="pending-title">WellCheck</span>
        </div>
      </nav>

      <div className="pending-card">
        <div className="pending-icon">⏳</div>
        <h2 className="pending-heading">Account Pending Approval</h2>
        <p className="pending-subtitle">
          Hi {firstName}! Your counselor account has been submitted successfully.
        </p>
        <p className="pending-description">
          Our admin team will review your application and approve your account shortly.
          You will be able to log in once your account has been approved.
        </p>

        <div className="pending-info">
          {email && (
            <div className="pending-info-item">
              <span className="pending-info-label">Email</span>
              <span className="pending-info-value">{email}</span>
            </div>
          )}
          <div className="pending-info-item">
            <span className="pending-info-label">Status</span>
            <span className="pending-status-badge">Pending Review</span>
          </div>
        </div>

        <button className="pending-btn" onClick={handleLogout}>
          Back to Login
        </button>
      </div>
    </div>
  );
}

export default Pending;