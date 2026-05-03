import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Search } from 'lucide-react';
import StudentSidebar from '../components/StudentSidebar';
import '../styles/BrowseCounselors.css';

const API = process.env.REACT_APP_API_URL;

function BrowseCounselors() {
  const navigate = useNavigate();
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
      setSlots(res.data.sort((a, b) => new Date(a.startTime) - new Date(b.startTime)));
    } catch (err) {
      console.error('Failed to fetch slots:', err);
    }
    setLoadingSlots(false);
  };

  const handleViewSlots = (e, counselor) => {
    e.stopPropagation();
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

  const formatTime = (dt) => new Date(dt).toLocaleString('en-US', { hour: '2-digit', minute: '2-digit' });
  const formatDate = (dt) => new Date(dt).toLocaleString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });

  const filteredCounselors = counselors.filter(c => {
    const fullName = `${c.firstName} ${c.lastName}`.toLowerCase();
    const matchesSearch = fullName.includes(searchQuery.toLowerCase());
    const matchesSpec = selectedSpecialization === '' || c.specialization === selectedSpecialization;
    return matchesSearch && matchesSpec;
  });

  const specializations = [...new Set(counselors.map(c => c.specialization))];

  const truncateBio = (bio, limit = 250) => {
    if (!bio) return 'No bio available.';
    return bio.length > limit ? bio.slice(0, limit) + '…' : bio;
  };

  return (
    <div className="bc-layout">
      <StudentSidebar activeItem="browse-counselors" />
      <main className="bc-main">
        <div className="bc-content">

          {/* ── Header ── */}
          <div className="bc-page-header">
            <div>
              <h1 className="bc-heading">Browse Counselors</h1>
              <p className="bc-subheading">Find and book a session with a guidance counselor.</p>
            </div>
          </div>

          {/* ── Toolbar ── */}
          <div className="bc-toolbar">
            <div className="bc-search-wrapper">
              <Search size={15} className="bc-search-icon" />
              <input
                className="bc-search"
                type="text"
                placeholder="Search by name..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
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

          {/* ── Grid ── */}
          {loadingCounselors ? (
            <div className="bc-empty">Loading counselors...</div>
          ) : filteredCounselors.length === 0 ? (
            <div className="bc-empty">No counselors found.</div>
          ) : (
            <div className="bc-grid">
              {filteredCounselors.map(counselor => (
                <div
                  key={counselor.id}
                  className="bc-card"
                  onClick={() => navigate(`/counselor/${counselor.id}`)}
                >
                  <div className="bc-card-top">
                    <div className="bc-avatar">
                      {counselor.firstName.charAt(0)}{counselor.lastName.charAt(0)}
                    </div>
                    <div className="bc-info">
                      <div className="bc-name">{counselor.firstName} {counselor.lastName}</div>
                      <div className="bc-spec">{counselor.specialization}</div>
                    </div>
                    <span className="bc-available-dot-wrap">
                      <span className="bc-available-dot" /> Available
                    </span>
                  </div>

                  <p className="bc-bio">{truncateBio(counselor.bio)}</p>

                  <div className="bc-card-footer">
                    <span className={`bc-slots-count ${counselor.availableSlots === 0 ? 'none' : ''}`}>
                      {counselor.availableSlots > 0
                        ? `${counselor.availableSlots} slot${counselor.availableSlots !== 1 ? 's' : ''} available`
                        : 'No slots available'}
                    </span>
                    <button
                      className="bc-btn-slots"
                      disabled={counselor.availableSlots === 0}
                      onClick={(e) => handleViewSlots(e, counselor)}
                    >
                      {counselor.availableSlots === 0 ? 'No slots available' : 'View available slots'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}

        </div>
      </main>

      {/* ── Slots Modal ── */}
      {showSlotsModal && (
        <div className="bc-modal-overlay" onClick={() => setShowSlotsModal(false)}>
          <div className="bc-modal" onClick={(e) => e.stopPropagation()}>
            <div className="bc-modal-header">
              <div>
                <h3 className="bc-modal-title">Available Slots</h3>
                <p className="bc-modal-sub">
                  {selectedCounselor?.firstName} {selectedCounselor?.lastName} · {selectedCounselor?.specialization}
                </p>
              </div>
              <button className="bc-modal-close" onClick={() => setShowSlotsModal(false)}>×</button>
            </div>

            {loadingSlots ? (
              <div className="bc-empty">Loading slots...</div>
            ) : slots.length === 0 ? (
              <div className="bc-empty">No available slots.</div>
            ) : (
              <div className="bc-slots-list">
                {slots.map(slot => (
                  <div key={slot.id} className="bc-slot-item" onClick={() => handleSelectSlot(slot)}>
                    <div className="bc-slot-badge">
                      <span className="bc-slot-month">
                        {new Date(slot.startTime).toLocaleString('en-US', { month: 'short' })}
                      </span>
                      <span className="bc-slot-day">{new Date(slot.startTime).getDate()}</span>
                    </div>
                    <div className="bc-slot-details">
                      <div className="bc-slot-time">{formatTime(slot.startTime)} → {formatTime(slot.endTime)}</div>
                      <div className="bc-slot-date">{formatDate(slot.startTime)}</div>
                    </div>
                    <button
                      className="bc-btn-select"
                      onClick={(e) => { e.stopPropagation(); handleSelectSlot(slot); }}
                    >
                      Select 
                    </button>
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