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

  const [slots, setSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [showSlotForm, setShowSlotForm] = useState(false);
  const [editingSlot, setEditingSlot] = useState(null);
  const [slotForm, setSlotForm] = useState({
    date: '', startTime: '', duration: '30', endTime: ''
  });
  const [slotError, setSlotError] = useState('');
  const [slotSuccess, setSlotSuccess] = useState('');
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletingSlotId, setDeletingSlotId] = useState(null);

  // Requests state
  const [appointments, setAppointments] = useState([]);
  const [loadingAppointments, setLoadingAppointments] = useState(false);
  const [filterStatus, setFilterStatus] = useState('PENDING');
  const [actionLoading, setActionLoading] = useState(null);
  const [showApproveModal, setShowApproveModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [actionId, setActionId] = useState(null);

  // Details modal state
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState(null);

  useEffect(() => {
    if (activeTab === 'slots') fetchSlots();
    if (activeTab === 'requests') fetchAppointments();
    if (activeTab === 'dashboard') {
      fetchSlots();
      fetchAppointments();
    }
  }, [activeTab]);

  const fetchSlots = async () => {
    setLoadingSlots(true);
    try {
      const res = await axios.get(`${API}/slots/my`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      const sorted = res.data.sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
      setSlots(sorted);
    } catch (err) {
      console.error('Failed to fetch slots:', err);
    }
    setLoadingSlots(false);
  };

  const fetchAppointments = async () => {
    setLoadingAppointments(true);
    try {
      const res = await axios.get(`${API}/appointments/counselor`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setAppointments(res.data);
    } catch (err) {
      console.error('Failed to fetch appointments:', err);
    }
    setLoadingAppointments(false);
  };

  const handleApprove = async () => {
    setActionLoading(actionId);
    try {
      await axios.put(`${API}/appointments/${actionId}/approve`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setShowApproveModal(false);
      setActionId(null);
      fetchAppointments();
    } catch (err) {
      alert(err.response?.data || 'Failed to approve appointment.');
    }
    setActionLoading(null);
  };

  const handleReject = async () => {
    setActionLoading(actionId);
    try {
      await axios.put(`${API}/appointments/${actionId}/reject`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setShowRejectModal(false);
      setActionId(null);
      fetchAppointments();
      fetchSlots();
    } catch (err) {
      alert(err.response?.data || 'Failed to reject appointment.');
    }
    setActionLoading(null);
  };

  const calculateEndTime = (startTime, duration) => {
    if (!startTime || !duration) return '';
    const [hours, minutes] = startTime.split(':').map(Number);
    const totalMinutes = hours * 60 + minutes + parseInt(duration);
    const endHours = Math.floor(totalMinutes / 60) % 24;
    const endMinutes = totalMinutes % 60;
    return `${String(endHours).padStart(2, '0')}:${String(endMinutes).padStart(2, '0')}`;
  };

  const handleSlotSubmit = async () => {
    setSlotError('');
    setSlotSuccess('');
    if (!slotForm.date || !slotForm.startTime) {
      setSlotError('Please fill in date and start time.');
      return;
    }
    try {
      const payload = {
        startTime: `${slotForm.date}T${slotForm.startTime}`,
        endTime: `${slotForm.date}T${slotForm.endTime}`
      };
      if (editingSlot) {
        await axios.put(`${API}/slots/${editingSlot.id}`, payload, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setSlotSuccess('Slot updated successfully!');
        setTimeout(() => setSlotSuccess(''), 2000);
      } else {
        await axios.post(`${API}/slots`, payload, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setSlotSuccess('Slot created successfully!');
        setTimeout(() => setSlotSuccess(''), 2000);
      }
      setShowSlotForm(false);
      setEditingSlot(null);
      setSlotForm({ date: '', startTime: '', duration: '30', endTime: '' });
      fetchSlots();
    } catch (err) {
      setSlotError(err.response?.data || 'Something went wrong.');
    }
  };

  const handleEditSlot = (slot) => {
    setEditingSlot(slot);
    const start = new Date(slot.startTime);
    const end = new Date(slot.endTime);
    const diffMinutes = (end - start) / 60000;
    const date = start.toISOString().split('T')[0];
    const startTime = start.toTimeString().slice(0, 5);
    const endTime = end.toTimeString().slice(0, 5);
    setSlotForm({ date, startTime, duration: String(diffMinutes), endTime });
    setShowSlotForm(true);
    setSlotError('');
    setSlotSuccess('');
  };

  const handleDeleteSlot = async () => {
    try {
      await axios.delete(`${API}/slots/${deletingSlotId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setShowDeleteModal(false);
      setDeletingSlotId(null);
      fetchSlots();
    } catch (err) {
      alert(err.response?.data || 'Failed to delete slot.');
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  const formatTime = (dt) => new Date(dt).toLocaleString('en-US', {
    hour: '2-digit', minute: '2-digit'
  });

  const formatDate = (dt) => new Date(dt).toLocaleString('en-US', {
    weekday: 'long', month: 'long', day: 'numeric', year: 'numeric'
  });

  const pendingCount = appointments.filter(a => a.status === 'PENDING').length;
  const confirmedCount = appointments.filter(a => a.status === 'CONFIRMED').length;

  const filteredAppointments = appointments.filter(a =>
    filterStatus === 'ALL' || a.status === filterStatus
  );

  const getStatusClass = (status) => {
    switch (status) {
      case 'PENDING': return 'apt-status-pending';
      case 'CONFIRMED': return 'apt-status-confirmed';
      case 'REJECTED': return 'apt-status-rejected';
      case 'CANCELLED': return 'apt-status-cancelled';
      default: return '';
    }
  };

  return (
    <div className="counselor-wrapper">
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
            <span className="nav-icon">🏠</span> Dashboard
          </div>
          <div className={`nav-item ${activeTab === 'slots' ? 'active' : ''}`} onClick={() => setActiveTab('slots')}>
            <span className="nav-icon">📅</span> Manage Slots
          </div>
          <div className={`nav-item ${activeTab === 'requests' ? 'active' : ''}`} onClick={() => setActiveTab('requests')}>
            <span className="nav-icon">📋</span> Requests
            {pendingCount > 0 && <span className="nav-badge">{pendingCount}</span>}
          </div>
          <div className={`nav-item ${activeTab === 'profile' ? 'active' : ''}`} onClick={() => setActiveTab('profile')}>
            <span className="nav-icon">👤</span> Profile
          </div>
        </nav>
        <div className="sidebar-logout" onClick={handleLogout}>
          <span className="nav-icon">↪</span> Log Out
        </div>
      </aside>

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
                  <div className="stat-icon-wrapper orange">📋</div>
                  <div className="stat-value">{pendingCount}</div>
                  <div className="stat-label">Pending Requests</div>
                </div>
                <div className="stat-card">
                  <div className="stat-icon-wrapper green">✅</div>
                  <div className="stat-value">{confirmedCount}</div>
                  <div className="stat-label">Confirmed Sessions</div>
                </div>
                <div className="stat-card">
                  <div className="stat-icon-wrapper purple">👥</div>
                  <div className="stat-value">{appointments.length}</div>
                  <div className="stat-label">Total</div>
                </div>
              </div>
              <div className="quick-actions-row">
                <div className="quick-action-card" onClick={() => setActiveTab('slots')}>
                  <div className="qa-icon-wrapper">📅</div>
                  <div>
                    <div className="qa-title">Create Slot</div>
                    <div className="qa-sub">Add a new available time slot</div>
                  </div>
                </div>
                <div className="quick-action-card" onClick={() => setActiveTab('requests')}>
                  <div className="qa-icon-wrapper">📋</div>
                  <div>
                    <div className="qa-title">Review Requests</div>
                    <div className="qa-sub">{pendingCount} Pending Appointment{pendingCount !== 1 ? 's' : ''}</div>
                  </div>
                </div>
              </div>
              <div className="section-card">
                <h3 className="section-title">Recent Pending Requests</h3>
                {appointments.filter(a => a.status === 'PENDING').length === 0 ? (
                  <div className="empty-msg">No pending requests yet.</div>
                ) : (
                  <div className="apt-list">
                    {appointments.filter(a => a.status === 'PENDING').slice(0, 3).map(apt => (
                      <div key={apt.id} className="apt-item" onClick={() => {
                        setSelectedAppointment(apt);
                        setShowDetailsModal(true);
                      }}>
                        <div className="apt-left">
                          <div className="apt-date-badge">
                            <span className="apt-month">
                              {new Date(apt.startTime).toLocaleString('en-US', { month: 'short' })}
                            </span>
                            <span className="apt-day">{new Date(apt.startTime).getDate()}</span>
                          </div>
                          <div className="apt-details">
                            <div className="apt-student">{apt.studentFirstName} {apt.studentLastName}</div>
                            <div className="apt-time">{formatTime(apt.startTime)} → {formatTime(apt.endTime)}</div>
                            {apt.note && <div className="apt-note">📝 {apt.note}</div>}
                          </div>
                        </div>
                        <div className="apt-right">
                          <button className="btn-approve-sm" onClick={(e) => {
                            e.stopPropagation();
                            setActionId(apt.id);
                            setShowApproveModal(true);
                          }}>Approve</button>
                          <button className="btn-reject-sm" onClick={(e) => {
                            e.stopPropagation();
                            setActionId(apt.id);
                            setShowRejectModal(true);
                          }}>Reject</button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </>
          )}

          {/* Slots Tab */}
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
                  setSlotForm({ date: '', startTime: '', duration: '30', endTime: '' });
                  setSlotError('');
                  setSlotSuccess('');
                }}>
                  + Create Slot
                </button>
              </div>

              {slotSuccess && <div className="slot-success">{slotSuccess}</div>}

              {showSlotForm && (
                <div className="section-card">
                  <h3 className="section-title">{editingSlot ? 'Edit Slot' : 'Create New Slot'}</h3>
                  {slotError && <div className="slot-error">{slotError}</div>}
                  <div className="slot-form">
                    <div className="slot-form-row">
                      <div className="slot-form-group">
                        <label>Date</label>
                        <input type="date" value={slotForm.date}
                          onChange={(e) => {
                            const updated = { ...slotForm, date: e.target.value };
                            updated.endTime = calculateEndTime(updated.startTime, updated.duration);
                            setSlotForm(updated);
                          }}
                          min={new Date().toISOString().split('T')[0]} />
                      </div>
                      <div className="slot-form-group">
                        <label>Start Time</label>
                        <input type="time" value={slotForm.startTime}
                          onChange={(e) => {
                            const updated = { ...slotForm, startTime: e.target.value };
                            updated.endTime = calculateEndTime(e.target.value, updated.duration);
                            setSlotForm(updated);
                          }} />
                      </div>
                      <div className="slot-form-group">
                        <label>Duration</label>
                        <select value={slotForm.duration}
                          onChange={(e) => {
                            const updated = { ...slotForm, duration: e.target.value };
                            updated.endTime = calculateEndTime(updated.startTime, e.target.value);
                            setSlotForm(updated);
                          }}>
                          <option value="15">15 minutes</option>
                          <option value="30">30 minutes</option>
                          <option value="45">45 minutes</option>
                          <option value="60">1 hour</option>
                          <option value="90">1.5 hours</option>
                          <option value="120">2 hours</option>
                        </select>
                      </div>
                      <div className="slot-form-group">
                        <label>End Time</label>
                        <input type="time" value={slotForm.endTime} readOnly className="input-readonly" />
                      </div>
                    </div>
                    <div className="slot-form-actions">
                      <button className="btn-save-slot" onClick={handleSlotSubmit}>
                        {editingSlot ? 'Update Slot' : 'Save Slot'}
                      </button>
                      <button className="btn-cancel-slot" onClick={() => {
                        setShowSlotForm(false);
                        setEditingSlot(null);
                        setSlotError('');
                      }}>Cancel</button>
                    </div>
                  </div>
                </div>
              )}

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
                        <div className="slot-left">
                          <div className="slot-date-badge">
                            <span className="slot-month">
                              {new Date(slot.startTime).toLocaleString('en-US', { month: 'short' })}
                            </span>
                            <span className="slot-day">{new Date(slot.startTime).getDate()}</span>
                          </div>
                          <div className="slot-details">
                            <div className="slot-time-text">
                              {new Date(slot.startTime).toLocaleString('en-US', { hour: '2-digit', minute: '2-digit' })}
                              {' '}&rarr;{' '}
                              {new Date(slot.endTime).toLocaleString('en-US', { hour: '2-digit', minute: '2-digit' })}
                            </div>
                            <div className="slot-date-text">
                              {new Date(slot.startTime).toLocaleString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                            </div>
                          </div>
                        </div>
                        <div className="slot-right">
                          <span className={`slot-status ${slot.status.toLowerCase()}`}>
                            {slot.status === 'AVAILABLE' ? 'Available' : '⏳ Booked'}
                          </span>
                          {slot.status === 'AVAILABLE' && (
                            <div className="slot-actions">
                              <button className="btn-edit-slot" onClick={() => handleEditSlot(slot)}>Edit</button>
                              <button className="btn-delete-slot" onClick={() => {
                                setDeletingSlotId(slot.id);
                                setShowDeleteModal(true);
                              }}>Delete</button>
                            </div>
                          )}
                        </div>
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

              <div className="apt-filter-tabs">
                {['ALL', 'PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED'].map(status => (
                  <button
                    key={status}
                    className={`apt-filter-tab ${filterStatus === status ? 'active' : ''}`}
                    onClick={() => setFilterStatus(status)}
                  >
                    {status}
                    {status === 'PENDING' && pendingCount > 0 && (
                      <span className="tab-badge">{pendingCount}</span>
                    )}
                  </button>
                ))}
              </div>

              {loadingAppointments ? (
                <div className="empty-msg">Loading appointments...</div>
              ) : filteredAppointments.length === 0 ? (
                <div className="section-card">
                  <div className="empty-msg">No {filterStatus !== 'ALL' ? filterStatus.toLowerCase() : ''} appointments found.</div>
                </div>
              ) : (
                <div className="apt-list">
                  {filteredAppointments.map(apt => (
                    <div key={apt.id} className="apt-item" style={{ cursor: 'pointer' }}
                      onClick={() => {
                        setSelectedAppointment(apt);
                        setShowDetailsModal(true);
                      }}>
                      <div className="apt-left">
                        <div className="apt-date-badge">
                          <span className="apt-month">
                            {new Date(apt.startTime).toLocaleString('en-US', { month: 'short' })}
                          </span>
                          <span className="apt-day">{new Date(apt.startTime).getDate()}</span>
                        </div>
                        <div className="apt-details">
                          <div className="apt-student">{apt.studentFirstName} {apt.studentLastName}</div>
                          <div className="apt-date-text">{formatDate(apt.startTime)}</div>
                          <div className="apt-time">{formatTime(apt.startTime)} → {formatTime(apt.endTime)}</div>
                          {apt.note && <div className="apt-note">📝 {apt.note}</div>}
                        </div>
                      </div>
                      <div className="apt-right">
                        <span className={`apt-status-badge ${getStatusClass(apt.status)}`}>
                          {apt.status}
                        </span>
                        {apt.status === 'PENDING' && (
                          <div className="apt-actions">
                            <button className="btn-approve" disabled={actionLoading === apt.id}
                              onClick={(e) => {
                                e.stopPropagation();
                                setActionId(apt.id);
                                setShowApproveModal(true);
                              }}>✓ Approve</button>
                            <button className="btn-reject" disabled={actionLoading === apt.id}
                              onClick={(e) => {
                                e.stopPropagation();
                                setActionId(apt.id);
                                setShowRejectModal(true);
                              }}>✕ Reject</button>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
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

      {/* Delete Slot Modal */}
      {showDeleteModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-icon">🗑️</div>
            <h3 className="modal-title">Delete Slot</h3>
            <p className="modal-message">Are you sure you want to delete this slot? This action cannot be undone.</p>
            <div className="modal-actions">
              <button className="btn-modal-cancel" onClick={() => {
                setShowDeleteModal(false);
                setDeletingSlotId(null);
              }}>Cancel</button>
              <button className="btn-modal-delete" onClick={handleDeleteSlot}>Delete</button>
            </div>
          </div>
        </div>
      )}

      {/* Approve Modal */}
      {showApproveModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-icon">✅</div>
            <h3 className="modal-title">Approve Appointment</h3>
            <p className="modal-message">Are you sure you want to approve this appointment request?</p>
            <div className="modal-actions">
              <button className="btn-modal-cancel" onClick={() => {
                setShowApproveModal(false);
                setActionId(null);
              }}>Cancel</button>
              <button className="btn-modal-approve" onClick={handleApprove}>Yes, Approve</button>
            </div>
          </div>
        </div>
      )}

      {/* Reject Modal */}
      {showRejectModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-icon">❌</div>
            <h3 className="modal-title">Reject Appointment</h3>
            <p className="modal-message">Are you sure you want to reject this appointment request?</p>
            <div className="modal-actions">
              <button className="btn-modal-cancel" onClick={() => {
                setShowRejectModal(false);
                setActionId(null);
              }}>Cancel</button>
              <button className="btn-modal-delete" onClick={handleReject}>Yes, Reject</button>
            </div>
          </div>
        </div>
      )}

      {/* Appointment Details Modal */}
      {showDetailsModal && selectedAppointment && (
        <div className="modal-overlay" onClick={() => setShowDetailsModal(false)}>
          <div className="modal-box modal-details" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Appointment Details</h3>
              <button className="modal-close" onClick={() => setShowDetailsModal(false)}>✕</button>
            </div>

            <div className="details-section">
              <div className="details-label">Student</div>
              <div className="details-value">
                {selectedAppointment.studentFirstName} {selectedAppointment.studentLastName}
              </div>
            </div>

            <div className="details-section">
              <div className="details-label">Date</div>
              <div className="details-value">{formatDate(selectedAppointment.startTime)}</div>
            </div>

            <div className="details-section">
              <div className="details-label">Time</div>
              <div className="details-value">
                {formatTime(selectedAppointment.startTime)} → {formatTime(selectedAppointment.endTime)}
              </div>
            </div>

            <div className="details-section">
              <div className="details-label">Status</div>
              <div className="details-value">
                <span className={`apt-status-badge ${getStatusClass(selectedAppointment.status)}`}>
                  {selectedAppointment.status}
                </span>
              </div>
            </div>

            {selectedAppointment.note && (
              <div className="details-section">
                <div className="details-label">Note from Student</div>
                <div className="details-value details-note">{selectedAppointment.note}</div>
              </div>
            )}

            {selectedAppointment.status === 'PENDING' && (
              <div className="details-actions">
                <button className="btn-approve" onClick={(e) => {
                  e.stopPropagation();
                  setShowDetailsModal(false);
                  setActionId(selectedAppointment.id);
                  setShowApproveModal(true);
                }}>✓ Approve</button>
                <button className="btn-reject" onClick={(e) => {
                  e.stopPropagation();
                  setShowDetailsModal(false);
                  setActionId(selectedAppointment.id);
                  setShowRejectModal(true);
                }}>✕ Reject</button>
              </div>
            )}
          </div>
        </div>
      )}

    </div>
  );
}

export default CounselorDashboard;