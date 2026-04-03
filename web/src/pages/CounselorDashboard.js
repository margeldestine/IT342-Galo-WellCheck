import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/CounselorDashboard.css';

const API = process.env.REACT_APP_API_URL;

function CounselorDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Counselor';
  const lastName = user.lastName || '';
  const token = localStorage.getItem('token');
  const [activeTab, setActiveTab] = useState('dashboard');

  // Slots state
  const [slots, setSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [showSlotForm, setShowSlotForm] = useState(false);
  const [editingSlot, setEditingSlot] = useState(null);
  const [slotForm, setSlotForm] = useState({ startTime: '', endTime: '' });
  const [slotError, setSlotError] = useState('');
  const [slotSuccess, setSlotSuccess] = useState('');

  useEffect(() => {
    if (activeTab === 'slots') fetchSlots();
  }, [activeTab]);

  const fetchSlots = async () => {
    setLoadingSlots(true);
    try {
      const res = await axios.get(`${API}/slots/my`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setSlots(res.data);
    } catch (err) {
      console.error('Failed to fetch slots:', err);
    }
    setLoadingSlots(false);
  };

  const handleSlotSubmit = async () => {
    setSlotError('');
    setSlotSuccess('');

    if (!slotForm.startTime || !slotForm.endTime) {
      setSlotError('Please fill in both start and end time.');
      return;
    }

    try {
      const payload = {
        startTime: slotForm.startTime,
        endTime: slotForm.endTime
      };

      if (editingSlot) {
        await axios.put(`${API}/slots/${editingSlot.id}`, payload, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setSlotSuccess('Slot updated successfully!');
      } else {
        await axios.post(`${API}/slots`, payload, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setSlotSuccess('Slot created successfully!');
      }

      setShowSlotForm(false);
      setEditingSlot(null);
      setSlotForm({ startTime: '', endTime: '' });
      fetchSlots();
    } catch (err) {
      setSlotError(err.response?.data || 'Something went wrong.');
    }
  };

  const handleEditSlot = (slot) => {
    setEditingSlot(slot);
    setSlotForm({
      startTime: slot.startTime.slice(0, 16),
      endTime: slot.endTime.slice(0, 16)
    });
    setShowSlotForm(true);
    setSlotError('');
    setSlotSuccess('');
  };

  const handleDeleteSlot = async (slotId) => {
    if (!window.confirm('Are you sure you want to delete this slot?')) return;
    try {
      await axios.delete(`${API}/slots/${slotId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      fetchSlots();
    } catch (err) {
      alert(err.response?.data || 'Failed to delete slot.');
    }
  };

  const formatDateTime = (dateTime) => {
    return new Date(dateTime).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  return (
    <div className="counselor-wrapper">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="navbar-logo">♥</div>
          <div>
            <div className="navbar-title">WellCheck</div>
            <div className="sidebar-subtitle">Counselor Portal</div>
          </div>
        </div>
        <nav className="sidebar-nav">
          <div className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`} onClick={() => setActiveTab('dashboard')}>
            <span className="nav-icon">⊞</span> Dashboard
          </div>
          <div className={`nav-item ${activeTab === 'slots' ? 'active' : ''}`} onClick={() => setActiveTab('slots')}>
            <span className="nav-icon">📅</span> Manage Slots
          </div>
          <div className={`nav-item ${activeTab === 'requests' ? 'active' : ''}`} onClick={() => setActiveTab('requests')}>
            <span className="nav-icon">📋</span> Requests
          </div>
          <div className={`nav-item ${activeTab === 'profile' ? 'active' : ''}`} onClick={() => setActiveTab('profile')}>
            <span className="nav-icon">👤</span> Profile
          </div>
        </nav>
        <div className="sidebar-logout" onClick={handleLogout}>
          <span className="nav-icon">↪</span> Log Out
        </div>
      </aside>

      {/* Main */}
      <main className="dashboard-main">
        <div className="topbar">
          <div />
          <div className="topbar-user">
            <span className="topbar-name">{firstName} {lastName}</span>
            <div className="topbar-avatar">{firstName.charAt(0)}</div>
          </div>
        </div>

        <div className="dashboard-content">

          {/* Dashboard Tab */}
          {activeTab === 'dashboard' && (
            <>
              <h1 className="greeting">Welcome, {firstName}!</h1>
              <p className="greeting-sub">Here is your counseling overview.</p>
              <div className="stats-row">
                <div className="stat-card">
                  <div className="stat-icon">📋</div>
                  <div className="stat-value">0</div>
                  <div className="stat-label">Pending Requests</div>
                </div>
                <div className="stat-card">
                  <div className="stat-icon">✅</div>
                  <div className="stat-value">0</div>
                  <div className="stat-label">Confirmed Sessions</div>
                </div>
                <div className="stat-card">
                  <div className="stat-icon">👥</div>
                  <div className="stat-value">0</div>
                  <div className="stat-label">Total</div>
                </div>
              </div>
              <div className="quick-actions-row">
                <div className="quick-action-card" onClick={() => setActiveTab('slots')}>
                  <div className="qa-icon">📅</div>
                  <div>
                    <div className="qa-title">Create Slot</div>
                    <div className="qa-sub">Add a new available time slot</div>
                  </div>
                </div>
                <div className="quick-action-card" onClick={() => setActiveTab('requests')}>
                  <div className="qa-icon">📋</div>
                  <div>
                    <div className="qa-title">Review Requests</div>
                    <div className="qa-sub">0 Pending Appointments</div>
                  </div>
                </div>
              </div>
              <div className="section-card">
                <h3 className="section-title">Recent Pending Requests</h3>
                <div className="empty-msg">No pending requests yet.</div>
              </div>
            </>
          )}

          {/* Manage Slots Tab */}
          {activeTab === 'slots' && (
            <>
              <div className="slots-header">
                <div>
                  <h1 className="greeting">Manage Slots</h1>
                  <p className="greeting-sub">Create and manage your available time slots.</p>
                </div>
                <button className="btn-create-slot" onClick={() => {
                  setShowSlotForm(true);
                  setEditingSlot(null);
                  setSlotForm({ startTime: '', endTime: '' });
                  setSlotError('');
                  setSlotSuccess('');
                }}>
                  + Create Slot
                </button>
              </div>

              {slotSuccess && <div className="slot-success">{slotSuccess}</div>}

              {/* Slot Form */}
              {showSlotForm && (
                <div className="section-card">
                  <h3 className="section-title">{editingSlot ? 'Edit Slot' : 'Create New Slot'}</h3>
                  {slotError && <div className="slot-error">{slotError}</div>}
                  <div className="slot-form">
                    <div className="slot-form-group">
                      <label>Start Time</label>
                      <input
                        type="datetime-local"
                        value={slotForm.startTime}
                        onChange={(e) => setSlotForm({ ...slotForm, startTime: e.target.value })}
                        min={new Date().toISOString().slice(0, 16)}
                      />
                    </div>
                    <div className="slot-form-group">
                      <label>End Time</label>
                      <input
                        type="datetime-local"
                        value={slotForm.endTime}
                        onChange={(e) => setSlotForm({ ...slotForm, endTime: e.target.value })}
                        min={slotForm.startTime || new Date().toISOString().slice(0, 16)}
                      />
                    </div>
                    <div className="slot-form-actions">
                      <button className="btn-save-slot" onClick={handleSlotSubmit}>
                        {editingSlot ? 'Update Slot' : 'Save Slot'}
                      </button>
                      <button className="btn-cancel-slot" onClick={() => {
                        setShowSlotForm(false);
                        setEditingSlot(null);
                        setSlotError('');
                      }}>
                        Cancel
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {/* Slots List */}
              <div className="section-card">
                <h3 className="section-title">Your Slots ({slots.length})</h3>
                {loadingSlots ? (
                  <div className="empty-msg">Loading slots...</div>
                ) : slots.length === 0 ? (
                  <div className="empty-msg">No slots yet. Create your first slot!</div>
                ) : (
                  <div className="slots-list">
                    {slots.map(slot => (
                      <div key={slot.id} className="slot-item">
                        <div className="slot-info">
                          <div className="slot-time">
                            📅 {formatDateTime(slot.startTime)} — {formatDateTime(slot.endTime)}
                          </div>
                          <span className={`slot-status ${slot.status.toLowerCase()}`}>
                            {slot.status}
                          </span>
                        </div>
                        {slot.status === 'AVAILABLE' && (
                          <div className="slot-actions">
                            <button className="btn-edit-slot" onClick={() => handleEditSlot(slot)}>Edit</button>
                            <button className="btn-delete-slot" onClick={() => handleDeleteSlot(slot.id)}>Delete</button>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </>
          )}

          {/* Requests Tab */}
          {activeTab === 'requests' && (
            <>
              <h1 className="greeting">Appointment Requests</h1>
              <p className="greeting-sub">Review and manage student appointment requests.</p>
              <div className="section-card">
                <div className="empty-msg">Appointment requests coming soon.</div>
              </div>
            </>
          )}

          {/* Profile Tab */}
          {activeTab === 'profile' && (
            <>
              <h1 className="greeting">Profile</h1>
              <p className="greeting-sub">View and edit your counselor profile.</p>
              <div className="section-card">
                <div className="empty-msg">Profile management coming soon.</div>
              </div>
            </>
          )}

        </div>
      </main>
    </div>
  );
}

export default CounselorDashboard;