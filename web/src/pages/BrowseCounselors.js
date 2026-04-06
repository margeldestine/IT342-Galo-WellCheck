import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/BrowseCounselors.css';

const API = process.env.REACT_APP_API_URL;

function BrowseCounselors() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Student';
  const lastName = user.lastName || '';
  const token = localStorage.getItem('token');

  const [counselors, setCounselors] = useState([]);
  const [loadingCounselors, setLoadingCounselors] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedSpecialization, setSelectedSpecialization] = useState('');

  const [showSlotsModal, setShowSlotsModal] = useState(false);
  const [selectedCounselor, setSelectedCounselor] = useState(null);
  const [slots, setSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);

  useEffect(() => {
    fetchCounselors();
  }, []);

  const fetchCounselors = async () => {
    setLoadingCounselors(true);
    try {
      const res = await axios.get(`${API}/counselors`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setCounselors(res.data);
    } catch (err) {
      console.error('Failed to fetch counselors:', err);
    }
    setLoadingCounselors(false);
  };

  const fetchSlots = async (counselorId) => {
    setLoadingSlots(true);
    try {
      const res = await axios.get(`${API}/slots/counselor/${counselorId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      const sorted = res.data.sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
      setSlots(sorted);
    } catch (err) {
      console.error('Failed to fetch slots:', err);
    }
    setLoadingSlots(false);
  };

  const handleViewSlots = (counselor) => {
    setSelectedCounselor(counselor);
    setShowSlotsModal(true);
    fetchSlots(counselor.id);
  };

  const handleSelectSlot = (slot) => {
    localStorage.setItem('selectedSlot', JSON.stringify(slot));
    localStorage.setItem('selectedCounselor', JSON.stringify(selectedCounselor));
    setShowSlotsModal(false);
    navigate('/book-appointment');
  };

  const formatTime = (dt) => new Date(dt).toLocaleString('en-US', {
    hour: '2-digit', minute: '2-digit'
  });

  const formatDate = (dt) => new Date(dt).toLocaleString('en-US', {
    weekday: 'long', month: 'long', day: 'numeric', year: 'numeric'
  });

  const filteredCounselors = counselors.filter(c => {
    const fullName = `${c.firstName} ${c.lastName}`.toLowerCase();
    const matchesSearch = fullName.includes(searchQuery.toLowerCase());
    const matchesSpec = selectedSpecialization === '' || c.specialization === selectedSpecialization;
    return matchesSearch && matchesSpec;
  });

  const specializations = [...new Set(counselors.map(c => c.specialization))];

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  return (
    <div className="bc-wrapper">

      {/* Navbar */}
      <nav className="bc-navbar">
        <div className="bc-brand" onClick={() => navigate('/dashboard')}>
          <div className="bc-logo">♥</div>
          <div>
            <div className="bc-title">WellCheck</div>
            <div className="bc-subtitle">Student Portal</div>
          </div>
        </div>
        <div className="bc-user">
          <span className="bc-username">{firstName} {lastName}</span>
          <div className="bc-avatar">{firstName.charAt(0)}</div>
        </div>
      </nav>

      <div className="bc-container">

        {/* Sidebar */}
        <aside className="bc-sidebar">
          <nav className="bc-nav">
            <div className="bc-nav-item" onClick={() => navigate('/dashboard')}>
              <span>🏠</span> Dashboard
            </div>
            <div className="bc-nav-item active">
              <span>👥</span> Browse Counselors
            </div>
            <div className="bc-nav-item" onClick={() => navigate('/dashboard')}>
              <span>📅</span> My Appointments
            </div>
            <div className="bc-nav-item" onClick={() => navigate('/studentprofile')}>
              <span>👤</span> Profile
            </div>
          </nav>
          <div className="bc-nav-logout" onClick={handleLogout}>
            <span>↪</span> Log Out
          </div>
        </aside>

        {/* Main */}
        <main className="bc-main">
          <h1 className="bc-heading">Browse Counselors</h1>
          <p className="bc-subheading">Find and book a session with a guidance counselor.</p>

          {/* Search and Filter */}
          <div className="bc-toolbar">
            <input
              className="bc-search"
              type="text"
              placeholder="🔍 Search by name..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <select
              className="bc-filter"
              value={selectedSpecialization}
              onChange={(e) => setSelectedSpecialization(e.target.value)}
            >
              <option value="">All Specializations</option>
              {specializations.map(spec => (
                <option key={spec} value={spec}>{spec}</option>
              ))}
            </select>
          </div>

          {/* Counselors Grid */}
          {loadingCounselors ? (
            <div className="bc-empty">Loading counselors...</div>
          ) : filteredCounselors.length === 0 ? (
            <div className="bc-empty">No counselors found.</div>
          ) : (
            <div className="bc-grid">
              {filteredCounselors.map(counselor => (
                <div key={counselor.id} className="bc-card">
                  <div className="bc-card-header">
                    <div className="bc-counselor-avatar">
                      {counselor.firstName.charAt(0)}{counselor.lastName.charAt(0)}
                    </div>
                    <div className="bc-counselor-info">
                      <div className="bc-counselor-name">
                        {counselor.firstName} {counselor.lastName}
                      </div>
                      <div className="bc-counselor-spec">{counselor.specialization}</div>
                    </div>
                  </div>
                  <p className="bc-counselor-bio">{counselor.bio || 'No bio available.'}</p>
                  <div className="bc-slots-count">
                    📅 {counselor.availableSlots} available slot{counselor.availableSlots !== 1 ? 's' : ''}
                  </div>
                  <button
                    className="bc-btn-view"
                    onClick={() => handleViewSlots(counselor)}
                    disabled={counselor.availableSlots === 0}
                  >
                    {counselor.availableSlots === 0 ? 'No slots available' : 'View available slots'}
                  </button>
                </div>
              ))}
            </div>
          )}
        </main>
      </div>

      {/* Slots Modal */}
      {showSlotsModal && (
        <div className="modal-overlay">
          <div className="modal-box modal-large">
            <div className="modal-header">
              <div>
                <h3 className="modal-title">Available Slots</h3>
                <p className="modal-subtitle">
                  {selectedCounselor?.firstName} {selectedCounselor?.lastName} · {selectedCounselor?.specialization}
                </p>
              </div>
              <button className="modal-close" onClick={() => setShowSlotsModal(false)}>✕</button>
            </div>
            {loadingSlots ? (
              <div className="bc-empty">Loading slots...</div>
            ) : slots.length === 0 ? (
              <div className="bc-empty">No available slots.</div>
            ) : (
              <div className="slots-modal-list">
                {slots.map(slot => (
                  <div key={slot.id} className="slot-modal-item" onClick={() => handleSelectSlot(slot)}>
                    <div className="slot-modal-date-badge">
                      <span className="slot-modal-month">
                        {new Date(slot.startTime).toLocaleString('en-US', { month: 'short' })}
                      </span>
                      <span className="slot-modal-day">{new Date(slot.startTime).getDate()}</span>
                    </div>
                    <div className="slot-modal-details">
                      <div className="slot-modal-time">{formatTime(slot.startTime)} → {formatTime(slot.endTime)}</div>
                      <div className="slot-modal-date">{formatDate(slot.startTime)}</div>
                    </div>
                    <button className="btn-select-slot">Select</button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

    </div>
  );
}

export default BrowseCounselors;