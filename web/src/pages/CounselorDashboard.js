import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import CounselorSidebar from '../components/CounselorSidebar';
import '../styles/CounselorDashboard.css';

const API = process.env.REACT_APP_API_URL;

function CounselorDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const firstName = user.firstName || 'Counselor';
  const lastName = user.lastName || '';
  const token = localStorage.getItem('token');

  const [searchParams, setSearchParams] = useSearchParams();
  const initialTab = searchParams.get('tab') || 'dashboard';
  const [activeTab, setActiveTab] = useState(initialTab);

  const [slots, setSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [showSlotForm, setShowSlotForm] = useState(false);
  const [editingSlot, setEditingSlot] = useState(null);
  const [slotForm, setSlotForm] = useState({ date: '', startTime: '', duration: '30', endTime: '', repeat: 'none' });
  const [slotFormError, setSlotFormError] = useState('');
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletingSlotId, setDeletingSlotId] = useState(null);
  const [expandedSlotId, setExpandedSlotId] = useState(null);

  const [appointments, setAppointments] = useState([]);
  const [loadingAppointments, setLoadingAppointments] = useState(false);
  const [filterStatus, setFilterStatus] = useState('PENDING');
  const [searchQuery, setSearchQuery] = useState('');

  const [actionLoading, setActionLoading] = useState(null);
  const [showApproveModal, setShowApproveModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [actionId, setActionId] = useState(null);
  const [rejectionReason, setRejectionReason] = useState('');

  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [showPhotoModal, setShowPhotoModal] = useState(false);
  const [viewingPhotoUrl, setViewingPhotoUrl] = useState('');

  // Calendar state
  const [calendarDate, setCalendarDate] = useState(new Date());
  const [selectedCalendarDay, setSelectedCalendarDay] = useState(null);

  useEffect(() => {
    const tab = searchParams.get('tab');
    if (tab && tab !== activeTab) setActiveTab(tab);
  }, [searchParams]);

  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setSearchParams({ tab });
  };

  useEffect(() => {
    if (activeTab === 'slots') fetchSlots();
    if (activeTab === 'requests') fetchAppointments();
    if (activeTab === 'dashboard') { fetchSlots(); fetchAppointments(); }
  }, [activeTab]);

  const fetchSlots = async () => {
    setLoadingSlots(true);
    try {
      const res = await axios.get(`${API}/slots/my`, { headers: { Authorization: `Bearer ${token}` } });
      const apptsRes = await axios.get(`${API}/appointments/counselor`, { headers: { Authorization: `Bearer ${token}` } });
      setAppointments(apptsRes.data);
      const sorted = res.data.sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
      setSlots(sorted);
    } catch (err) { console.error('Failed to fetch slots:', err); }
    setLoadingSlots(false);
  };

  const fetchAppointments = async () => {
    setLoadingAppointments(true);
    try {
      const res = await axios.get(`${API}/appointments/counselor`, { headers: { Authorization: `Bearer ${token}` } });
      setAppointments(res.data);
    } catch (err) { console.error('Failed to fetch appointments:', err); }
    setLoadingAppointments(false);
  };

  const handleApprove = async () => {
    setActionLoading(actionId);
    try {
      await axios.put(`${API}/appointments/${actionId}/approve`, {}, { headers: { Authorization: `Bearer ${token}` } });
      setShowApproveModal(false);
      setActionId(null);
      fetchAppointments();
    } catch (err) { alert(err.response?.data || 'Failed to approve appointment.'); }
    setActionLoading(null);
  };

  const handleReject = async () => {
    if (!rejectionReason.trim()) return alert('Please provide a reason for rejection.');
    setActionLoading(actionId);
    try {
      await axios.put(`${API}/appointments/${actionId}/reject`, { reason: rejectionReason }, { headers: { Authorization: `Bearer ${token}` } });
      setShowRejectModal(false);
      setActionId(null);
      setRejectionReason('');
      fetchAppointments();
      fetchSlots();
    } catch (err) { alert(err.response?.data || 'Failed to reject appointment.'); }
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

  // ─── SLOT FORM VALIDATION ────────────────────────────────────────────
  const isWeekday = (dateStr) => {
    if (!dateStr) return false;
    const day = new Date(dateStr + 'T00:00:00').getDay(); // 0=Sun, 6=Sat
    return day >= 1 && day <= 5;
  };

  const isAllowedTime = (startTime, duration) => {
    if (!startTime || !duration) return true; // let required check catch it
    const [h, m] = startTime.split(':').map(Number);
    const startMinutes = h * 60 + m;
    const endMinutes = startMinutes + parseInt(duration);

    // Morning block: 08:00–12:00 (480–720)
    const morningOk = startMinutes >= 480 && endMinutes <= 720;
    // Afternoon block: 13:00–17:00 (780–1020)
    const afternoonOk = startMinutes >= 780 && endMinutes <= 1020;

    return morningOk || afternoonOk;
  };

  const validateSlotForm = () => {
    if (!slotForm.date || !slotForm.startTime) {
      setSlotFormError('Please fill in date and start time.');
      return false;
    }
    if (!isWeekday(slotForm.date)) {
      setSlotFormError('Slots can only be created on Monday to Friday.');
      return false;
    }
    if (!isAllowedTime(slotForm.startTime, slotForm.duration)) {
      setSlotFormError('Time must be within 8:00 AM–12:00 PM or 1:00 PM–5:00 PM, and the slot must not overflow the block.');
      return false;
    }

    if (slotForm.repeat !== 'none' && !slotForm.repeatUntil) {
      setSlotFormError('Please choose an end date for the repeat.');
      return false;
    }
    if (slotForm.repeat !== 'none' && slotForm.repeatUntil <= slotForm.date) {
      setSlotFormError('Repeat end date must be after the start date.');
      return false;
    }
    setSlotFormError('');
    return true;
  };

  const buildRepeatDates = (startDate, repeat, repeatUntil) => {
    const dates = [startDate];
    if (repeat === 'none' || !repeatUntil) return dates;

    const incrementDays = repeat === 'daily' ? 1 : 7;
    const endDate = new Date(repeatUntil + 'T00:00:00');
    let current = new Date(startDate + 'T00:00:00');

    while (true) {
      current = new Date(current);
      current.setDate(current.getDate() + incrementDays);
      if (current > endDate) break;

      const dayOfWeek = current.getDay();
      // For daily repeat, skip weekends
      if (repeat === 'daily' && (dayOfWeek === 0 || dayOfWeek === 6)) continue;

      dates.push(current.toISOString().split('T')[0]);
    }
    return dates;
  };

  const handleSlotSubmit = async () => {
    if (!validateSlotForm()) return;

    const datesToCreate = buildRepeatDates(slotForm.date, slotForm.repeat, slotForm.repeatUntil);

    try {
      await Promise.all(
        datesToCreate.map(dateStr => {
          const payload = {
            startTime: `${dateStr}T${slotForm.startTime}`,
            endTime: `${dateStr}T${slotForm.endTime}`,
          };
          if (editingSlot) {
            return axios.put(`${API}/slots/${editingSlot.id}`, payload, { headers: { Authorization: `Bearer ${token}` } });
          } else {
            return axios.post(`${API}/slots`, payload, { headers: { Authorization: `Bearer ${token}` } });
          }
        })
      );
      setShowSlotForm(false);
      setEditingSlot(null);
      setSlotForm({ date: '', startTime: '', duration: '30', endTime: '', repeat: 'none', repeatUntil: '' })

      setSlotFormError('');
      fetchSlots();
    } catch (err) { alert(err.response?.data || 'Something went wrong.'); }
  };

  const handleEditSlot = (slot) => {
    setEditingSlot(slot);
    const start = new Date(slot.startTime);
    const end = new Date(slot.endTime);
    const diffMinutes = (end - start) / 60000;
    setSlotForm({
      date: start.toISOString().split('T')[0],
      startTime: start.toTimeString().slice(0, 5),
      duration: String(diffMinutes),
      endTime: end.toTimeString().slice(0, 5),
      repeat: 'none',
       repeatUntil: '',
    });
    setSlotFormError('');
    setShowSlotForm(true);
  };

  const handleDeleteSlot = async () => {
    try {
      const response = await axios.delete(`${API}/slots/${deletingSlotId}`, { headers: { Authorization: `Bearer ${token}` } });
      if (response.data.action === 'marked_unavailable') {
        alert(`⚠️ ${response.data.message}\n\nThe slot has been marked as unavailable instead of deleted to preserve appointment history.`);
      }
      setShowDeleteModal(false);
      setDeletingSlotId(null);
      fetchSlots();
    } catch (err) { alert(err.response?.data?.error || err.response?.data || 'Failed to delete slot.'); }
  };

  const formatTime = (dt) => new Date(dt).toLocaleString('en-US', { hour: '2-digit', minute: '2-digit' });
  const formatDateFull = (dt) => new Date(dt).toLocaleString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });

  const pendingCount = appointments.filter(a => a.status === 'PENDING').length;
  const confirmedCount = appointments.filter(a => a.status === 'CONFIRMED').length;

  const filteredAppointments = appointments.filter(a => {
    const matchesStatus = filterStatus === 'ALL' || a.status === filterStatus;
    const fullName = `${a.studentFirstName} ${a.studentLastName}`.toLowerCase();
    const matchesSearch =
      fullName.includes(searchQuery.toLowerCase()) ||
      (a.studentIdNumber && a.studentIdNumber.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchesStatus && matchesSearch;
  });

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  };

  // ─── UPCOMING SLOTS ONLY (today or future) ────────────────────────────
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const upcomingSlots = slots.filter(slot => {
    const slotDate = new Date(slot.startTime);
    slotDate.setHours(0, 0, 0, 0);
    return slotDate >= today;
  });

  const groupedSlots = upcomingSlots.reduce((acc, slot) => {
    const dateStr = new Date(slot.startTime).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
    if (!acc[dateStr]) acc[dateStr] = [];
    acc[dateStr].push(slot);
    return acc;
  }, {});

  // ─── CALENDAR LOGIC ────────────────────────────────────────────────────
  const bookedDates = new Set(
    slots
      .filter(s => s.status === 'BOOKED')
      .map(s => {
        const d = new Date(s.startTime);
        return `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`;
      })
  );

  const slotDates = new Set(
    upcomingSlots.map(s => {
      const d = new Date(s.startTime);
      return `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`;
    })
  );

  const getDaysInMonth = (year, month) => new Date(year, month + 1, 0).getDate();
  const getFirstDayOfMonth = (year, month) => new Date(year, month, 1).getDay();

  const calYear = calendarDate.getFullYear();
  const calMonth = calendarDate.getMonth();
  const daysInMonth = getDaysInMonth(calYear, calMonth);
  const firstDay = getFirstDayOfMonth(calYear, calMonth);

  const prevCalMonth = () => {
    setCalendarDate(new Date(calYear, calMonth - 1, 1));
    setSelectedCalendarDay(null);
  };
  const nextCalMonth = () => {
    setCalendarDate(new Date(calYear, calMonth + 1, 1));
    setSelectedCalendarDay(null);
  };

  const handleCalDayClick = (day) => {
    const key = `${calYear}-${calMonth}-${day}`;
    if (!slotDates.has(key)) return;
    setSelectedCalendarDay(selectedCalendarDay === day ? null : day);
  };

  const selectedDaySlots = selectedCalendarDay
    ? upcomingSlots.filter(s => {
        const d = new Date(s.startTime);
        return d.getFullYear() === calYear && d.getMonth() === calMonth && d.getDate() === selectedCalendarDay;
      })
    : [];

  const monthNames = ['January','February','March','April','May','June','July','August','September','October','November','December'];
  const dayLabels = ['Su','Mo','Tu','We','Th','Fr','Sa'];

  // Minimum date = tomorrow (no same-day slots)
  const minDate = (() => {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return d.toISOString().split('T')[0];
  })();

  // ─── RENDER ────────────────────────────────────────────────────────────
  return (
    <div className="cp-app">
      <CounselorSidebar activeItem={activeTab} onTabChange={handleTabChange} pendingCount={pendingCount} />

      <main className="cp-main">

        {/* ── DASHBOARD ──────────────────────────────────────────────── */}
        {activeTab === 'dashboard' && (
          <div className="cp-page cp-active">
            <div className="cp-welcome-bar">
              <div className="cp-welcome-text">
                <div className="cp-welcome-greeting">Counselor Portal</div>
                <div className="cp-welcome-name">{getGreeting()}, {firstName}.</div>
                <div className="cp-welcome-sub">Here is your counseling overview for today.</div>
              </div>
              <div className="cp-welcome-date">
                <div className="cp-date-day">{new Date().getDate()}</div>
                <div className="cp-date-month">{new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}</div>
              </div>
            </div>

            <div className="cp-stats-grid">
              <div className="cp-stat-card">
                <div className="cp-stat-icon cp-gold">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/></svg>
                </div>
                <div className="cp-stat-num">{pendingCount}</div>
                <div className="cp-stat-label">Pending requests</div>
              </div>
              <div className="cp-stat-card">
                <div className="cp-stat-icon cp-green">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><polyline points="20 6 9 17 4 12"/></svg>
                </div>
                <div className="cp-stat-num">{confirmedCount}</div>
                <div className="cp-stat-label">Confirmed sessions</div>
              </div>
              <div className="cp-stat-card">
                <div className="cp-stat-icon cp-blue">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.58-7 8-7s8 3 8 7"/></svg>
                </div>
                <div className="cp-stat-num">{appointments.length}</div>
                <div className="cp-stat-label">Total students</div>
              </div>
            </div>

            <div className="cp-quick-actions">
              <div className="cp-quick-card" onClick={() => handleTabChange('slots')}>
                <div className="cp-quick-icon cp-blue">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                </div>
                <div>
                  <div className="cp-quick-title">Create Slot</div>
                  <div className="cp-quick-sub">Add a new available time slot</div>
                </div>
                <div className="cp-quick-arrow"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="9 18 15 12 9 6"/></svg></div>
              </div>
              <div className="cp-quick-card" onClick={() => handleTabChange('requests')}>
                <div className="cp-quick-icon cp-gold">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/><line x1="9" y1="12" x2="15" y2="12"/></svg>
                </div>
                <div>
                  <div className="cp-quick-title">Review Requests</div>
                  <div className="cp-quick-sub">{pendingCount} pending appointment{pendingCount !== 1 ? 's' : ''}</div>
                </div>
                <div className="cp-quick-arrow"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="9 18 15 12 9 6"/></svg></div>
              </div>
            </div>

            <div className="cp-card">
              <div className="cp-card-label">Recent Pending Requests</div>
              {appointments.filter(a => a.status === 'PENDING').length === 0 ? (
                <div className="cp-pending-empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><polyline points="20 6 9 17 4 12"/></svg>
                  <p>No pending requests.</p>
                </div>
              ) : (
                <div className="cp-req-list">
                  {appointments.filter(a => a.status === 'PENDING').slice(0, 3).map(apt => (
                    <div key={apt.id} className="cp-req-card cp-status-pending" onClick={() => { setSelectedAppointment(apt); setShowDetailsModal(true); }}>
                      <div className="cp-req-student-av">{apt.studentFirstName.charAt(0)}{apt.studentLastName.charAt(0)}</div>
                      <div className="cp-req-info">
                        <div className="cp-req-student-name">{apt.studentFirstName} {apt.studentLastName}</div>
                        <div className="cp-req-meta">
                          <span className="cp-req-meta-item">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                            {new Date(apt.startTime).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}
                          </span>
                          <span className="cp-req-meta-item">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="9"/><polyline points="12 7 12 12 15 15"/></svg>
                            {formatTime(apt.startTime)}
                          </span>
                        </div>
                      </div>
                      <div className="cp-req-actions">
                        <button className="cp-accept-btn" onClick={(e) => { e.stopPropagation(); setActionId(apt.id); setShowApproveModal(true); }}>Accept</button>
                        <button className="cp-reject-btn" onClick={(e) => { e.stopPropagation(); setActionId(apt.id); setShowRejectModal(true); }}>Reject</button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {/* ── MANAGE SLOTS ───────────────────────────────────────────── */}
        {activeTab === 'slots' && (
          <div className="cp-page cp-active">
            <div className="cp-page-header">
              <div>
                <div className="cp-page-title">Manage Slots</div>
                <div className="cp-page-sub">Create and manage your available time slots.</div>
              </div>
              <button className="cp-btn-primary" onClick={() => { setEditingSlot(null); setSlotForm({ date: '', startTime: '', duration: '30', endTime: '', repeat: 'none', repeatUntil: '' }); setSlotFormError(''); setShowSlotForm(true); }}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                Create Slot
              </button>
            </div>

            {/* Two-column layout: slots list + calendar sidebar */}
            <div className="cp-slots-layout">

              {/* ── LEFT: Slots List ──────────────────────────────────── */}
              <div className="cp-slots-main">
                <div className="cp-slots-toolbar">
                  <div className="cp-slots-count">
                    Showing <strong>{upcomingSlots.length}</strong> upcoming slot{upcomingSlots.length !== 1 ? 's' : ''}
                  </div>
                </div>

                {loadingSlots ? (
                  <div className="cp-pending-empty"><p>Loading slots...</p></div>
                ) : upcomingSlots.length === 0 ? (
                  <div className="cp-pending-empty"><p>No upcoming slots. Create your first slot!</p></div>
                ) : (
                  <div className="cp-slots-list">
                    {Object.keys(groupedSlots).map(dateStr => (
                      <div key={dateStr} className="cp-slots-date-group">
                        <div className="cp-date-divider">{dateStr}</div>
                        <div className="cp-slots-date-items">
                          {groupedSlots[dateStr].map(slot => {
                            const apt = slot.status === 'BOOKED' ? appointments.find(a => a.slotId === slot.id) : null;
                            const isExpanded = expandedSlotId === slot.id;

                            return (
                              <div key={slot.id} className={`cp-slot-wrapper ${isExpanded ? 'expanded' : ''}`}>
                                <div
                                  className="cp-slot-item"
                                  onClick={() => {
                                    if (slot.status === 'BOOKED') {
                                      setExpandedSlotId(isExpanded ? null : slot.id);
                                    }
                                  }}
                                >
                                  <div className={`cp-slot-date-block ${slot.status === 'BOOKED' ? 'cp-booked-block' : ''}`}>
                                    <span className="cp-slot-month">{new Date(slot.startTime).toLocaleString('en-US', { month: 'short' })}</span>
                                    <span className="cp-slot-day">{new Date(slot.startTime).getDate()}</span>
                                  </div>

                                  <div className="cp-slot-info">
                                    <div className="cp-slot-time">{formatTime(slot.startTime)} → {formatTime(slot.endTime)}</div>
                                    <div className="cp-slot-date-full">{formatDateFull(slot.startTime)}</div>
                                  </div>

                                  <span className={`cp-slot-status cp-${slot.status.toLowerCase()}`}>
                                    {slot.status === 'AVAILABLE' ? 'Available' : slot.status === 'BOOKED' ? 'Booked' : 'Unavailable'}
                                  </span>

                                  <div className="cp-slot-actions">
                                    {slot.status === 'AVAILABLE' && (
                                      <>
                                        <button className="cp-btn-ghost" onClick={(e) => { e.stopPropagation(); handleEditSlot(slot); }}>
                                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg> Edit
                                        </button>
                                        <button className="cp-btn-danger" onClick={(e) => { e.stopPropagation(); setDeletingSlotId(slot.id); setShowDeleteModal(true); }}>
                                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/></svg> Delete
                                        </button>
                                      </>
                                    )}
                                    {slot.status === 'BOOKED' && (
                                      <div className="cp-slot-chevron">
                                        <svg
                                          viewBox="0 0 24 24"
                                          fill="none"
                                          stroke="currentColor"
                                          strokeWidth="2"
                                          style={{ width: '18px', height: '18px', transform: isExpanded ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s' }}
                                        >
                                          <polyline points="6 9 12 15 18 9"/>
                                        </svg>
                                      </div>
                                    )}
                                  </div>
                                </div>

                                {isExpanded && apt && (
                                  <div className="cp-slot-expanded-details">
                                    <div className="cp-expanded-identity">
                                      <div className="cp-inline-avatar">
                                        {apt.studentFirstName.charAt(0)}{apt.studentLastName.charAt(0)}
                                      </div>
                                      <div className="cp-expanded-identity-text">
                                        <div className="cp-expanded-student-label">Student</div>
                                        <div className="cp-expanded-student-name">
                                          {apt.studentFirstName} {apt.studentLastName}
                                        </div>
                                        <div className="cp-expanded-student-id">ID: {apt.studentIdNumber || '—'}</div>
                                      </div>
                                    </div>

                                    <div className="cp-expanded-meta-row">
                                      {[
                                        { label: 'Program', value: apt.studentProgram },
                                        { label: 'Year',    value: apt.studentYearLevel },
                                        { label: 'Gender',  value: apt.studentGender },
                                        {
                                          label: 'DOB',
                                          value: apt.studentBirthdate
                                            ? new Date(apt.studentBirthdate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
                                            : null,
                                        },
                                      ].map(({ label, value }) => (
                                        <div className="cp-meta-chip" key={label}>
                                          <span className="cp-meta-chip-label">{label}</span>
                                          <span className="cp-meta-chip-value">{value || '—'}</span>
                                        </div>
                                      ))}
                                    </div>

                                    <div className="cp-expanded-footer">
                                      {apt.note ? (
                                        <div className="cp-expanded-note-inline">
                                          <strong>Note from student</strong>
                                          {apt.note}
                                        </div>
                                      ) : (
                                        <div style={{ flex: 1 }} />
                                      )}
                                      <button
                                        className="cp-btn-secondary"
                                        onClick={(e) => {
                                          e.stopPropagation();
                                          if (apt.studentSchoolIdPhotoUrl) {
                                            setViewingPhotoUrl(apt.studentSchoolIdPhotoUrl);
                                            setShowPhotoModal(true);
                                          } else {
                                            alert('No school photo ID uploaded.');
                                          }
                                        }}
                                      >
                                        View School ID
                                      </button>
                                    </div>
                                  </div>
                                )}
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* ── RIGHT: Calendar Sidebar ────────────────────────────── */}
              <div className="cp-cal-sidebar">

                {/* Mini Calendar */}
                <div className="cp-cal-widget">
                  <div className="cp-cal-header">
                    <button className="cp-cal-nav" onClick={prevCalMonth}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="15 18 9 12 15 6"/></svg>
                    </button>
                    <span className="cp-cal-month-label">{monthNames[calMonth]} {calYear}</span>
                    <button className="cp-cal-nav" onClick={nextCalMonth}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="9 18 15 12 9 6"/></svg>
                    </button>
                  </div>

                  <div className="cp-cal-grid">
                    {dayLabels.map(d => (
                      <div key={d} className="cp-cal-day-label">{d}</div>
                    ))}

                    {Array.from({ length: firstDay }).map((_, i) => (
                      <div key={`empty-${i}`} className="cp-cal-cell cp-cal-empty" />
                    ))}

                    {Array.from({ length: daysInMonth }).map((_, i) => {
                      const day = i + 1;
                      const key = `${calYear}-${calMonth}-${day}`;
                      const hasSlot = slotDates.has(key);
                      const hasBooking = bookedDates.has(key);
                      const isToday =
                        new Date().getFullYear() === calYear &&
                        new Date().getMonth() === calMonth &&
                        new Date().getDate() === day;
                      const isSelected = selectedCalendarDay === day;

                      return (
                        <div
                          key={day}
                          className={[
                            'cp-cal-cell',
                            hasSlot ? 'cp-cal-has-slot' : '',
                            hasBooking ? 'cp-cal-has-booking' : '',
                            isToday ? 'cp-cal-today' : '',
                            isSelected ? 'cp-cal-selected' : '',
                            !hasSlot ? 'cp-cal-no-slot' : '',
                          ].join(' ')}
                          onClick={() => handleCalDayClick(day)}
                        >
                          {day}
                          {hasBooking && <span className="cp-cal-dot" />}
                        </div>
                      );
                    })}
                  </div>

                  <div className="cp-cal-legend">
                    <div className="cp-cal-legend-item">
                      <span className="cp-cal-legend-dot cp-legend-slot" />
                      Has slot
                    </div>
                    <div className="cp-cal-legend-item">
                      <span className="cp-cal-legend-dot cp-legend-booked" />
                      Booked
                    </div>
                  </div>
                </div>

                {/* Day Schedule Panel */}
                {selectedCalendarDay && (
                  <div className="cp-cal-schedule">
                    <div className="cp-cal-schedule-title">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="9"/><polyline points="12 7 12 12 15 15"/></svg>
                      {monthNames[calMonth]} {selectedCalendarDay}
                    </div>
                    {selectedDaySlots.length === 0 ? (
                      <div className="cp-cal-schedule-empty">No upcoming slots on this day.</div>
                    ) : (
                      <div className="cp-cal-schedule-list">
                        {selectedDaySlots.map(slot => (
                          <div key={slot.id} className={`cp-cal-schedule-item cp-cal-sched-${slot.status.toLowerCase()}`}>
                            <div className="cp-cal-sched-time">
                              {formatTime(slot.startTime)} – {formatTime(slot.endTime)}
                            </div>
                            <span className={`cp-cal-sched-badge cp-${slot.status.toLowerCase()}`}>
                              {slot.status === 'AVAILABLE' ? 'Open' : slot.status === 'BOOKED' ? 'Booked' : 'Unavailable'}
                            </span>
                            {slot.status === 'BOOKED' && (() => {
                              const apt = appointments.find(a => a.slotId === slot.id);
                              return apt ? (
                                <div className="cp-cal-sched-student">
                                  {apt.studentFirstName} {apt.studentLastName}
                                </div>
                              ) : null;
                            })()}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {/* History Button */}
                <button
                  className="cp-history-btn"
                  onClick={() => navigate('/counselor/appointments/history')}
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                    <path d="M12 8v4l3 3"/>
                    <path d="M3.05 11a9 9 0 1 0 .5-4"/>
                    <path d="M3 3v4h4"/>
                  </svg>
                  View Appointment History
                </button>

              </div>
            </div>
          </div>
        )}

        {/* ── REQUESTS ───────────────────────────────────────────────── */}
        {activeTab === 'requests' && (
          <div className="cp-page cp-active">
            <div className="cp-page-header">
              <div>
                <div className="cp-page-title">Appointment Requests</div>
                <div className="cp-page-sub">Review and manage student appointment requests.</div>
              </div>
            </div>

            <div className="cp-req-toolbar">
              <div className="cp-search-wrap">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input
                  type="text"
                  className="cp-search-input"
                  placeholder="Search by student name..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
              <div className="cp-req-tabs">
                {['PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED'].map(status => (
                  <button
                    key={status}
                    className={`cp-req-tab ${filterStatus === status ? 'cp-active' : ''}`}
                    onClick={() => setFilterStatus(status)}
                  >
                    {status.charAt(0) + status.slice(1).toLowerCase()}
                  </button>
                ))}
              </div>
            </div>

            <div>
              <div className="cp-req-section-title">
                {filterStatus.charAt(0) + filterStatus.slice(1).toLowerCase()} Requests
                <span className="cp-section-count">{filteredAppointments.length}</span>
              </div>

              {loadingAppointments ? (
                <div className="cp-req-empty"><p>Loading appointments...</p></div>
              ) : filteredAppointments.length === 0 ? (
                <div className="cp-req-empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><polyline points="20 6 9 17 4 12"/></svg>
                  <p>No {filterStatus.toLowerCase()} requests found.</p>
                </div>
              ) : (
                <div className="cp-req-list">
                  {filteredAppointments.map(apt => (
                    <div
                      key={apt.id}
                      className={`cp-req-card cp-status-${apt.status.toLowerCase()}`}
                      onClick={() => { setSelectedAppointment(apt); setShowDetailsModal(true); }}
                    >
                      <div className="cp-req-student-av">{apt.studentFirstName.charAt(0)}{apt.studentLastName.charAt(0)}</div>
                      <div className="cp-req-info">
                        <div className="cp-req-student-name">{apt.studentFirstName} {apt.studentLastName}</div>
                        <div className="cp-req-meta">
                          <span className="cp-req-meta-item">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                            {new Date(apt.startTime).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}
                          </span>
                          <span className="cp-req-meta-item">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="9"/><polyline points="12 7 12 12 15 15"/></svg>
                            {formatTime(apt.startTime)}
                          </span>
                        </div>
                      </div>

                      {apt.status === 'PENDING' ? (
                        <div className="cp-req-actions">
                          <button className="cp-accept-btn" onClick={(e) => { e.stopPropagation(); setActionId(apt.id); setShowApproveModal(true); }}>Accept</button>
                          <button className="cp-reject-btn" onClick={(e) => { e.stopPropagation(); setActionId(apt.id); setShowRejectModal(true); }}>Reject</button>
                        </div>
                      ) : (
                        <span className={`cp-status-pill cp-pill-${apt.status.toLowerCase()}`}>{apt.status.charAt(0) + apt.status.slice(1).toLowerCase()}</span>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {/* ── PROFILE ────────────────────────────────────────────────── */}
        {activeTab === 'profile' && (
          <div className="cp-page cp-active">
            <div className="cp-page-header">
              <div>
                <div className="cp-page-title">My Profile</div>
                <div className="cp-page-sub">Manage your counselor information and bio.</div>
              </div>
            </div>

            <div className="cp-profile-layout">
              <div className="cp-profile-sidebar">
                <div className="cp-profile-banner"></div>
                <div className="cp-profile-av-wrap">
                  <div className="cp-profile-av">{firstName.charAt(0)}{lastName.charAt(0)}</div>
                </div>
                <div className="cp-profile-sidebar-body">
                  <div className="cp-profile-full-name">{firstName} {lastName}</div>
                  <div className="cp-profile-emp-id">Counselor ID: {user.id || 'N/A'}</div>
                  <div className="cp-profile-spec-badge">Career Counseling</div>
                  <div className="cp-profile-divider"></div>
                  <div className="cp-profile-stat-grid">
                    <div className="cp-profile-stat">
                      <div className="cp-profile-stat-num">{slots.length}</div>
                      <div className="cp-profile-stat-label">Active slots</div>
                    </div>
                    <div className="cp-profile-stat">
                      <div className="cp-profile-stat-num">{appointments.length}</div>
                      <div className="cp-profile-stat-label">Total students</div>
                    </div>
                    <div className="cp-profile-stat">
                      <div className="cp-profile-stat-num">{confirmedCount}</div>
                      <div className="cp-profile-stat-label">Confirmed</div>
                    </div>
                    <div className="cp-profile-stat">
                      <div className="cp-profile-stat-num">{pendingCount}</div>
                      <div className="cp-profile-stat-label">Pending</div>
                    </div>
                  </div>
                </div>
              </div>

              <div className="cp-profile-form">
                <div className="cp-form-section-title">Counselor Information</div>
                <div className="cp-form-row cp-single">
                  <div className="cp-form-group">
                    <label className="cp-form-label">Email Address</label>
                    <input className="cp-form-input" type="text" value={user.email || ''} readOnly />
                  </div>
                </div>
                <div className="cp-form-row">
                  <div className="cp-form-group">
                    <label className="cp-form-label">First Name</label>
                    <input className="cp-form-input" type="text" value={firstName} readOnly />
                  </div>
                  <div className="cp-form-group">
                    <label className="cp-form-label">Last Name</label>
                    <input className="cp-form-input" type="text" value={lastName} readOnly />
                  </div>
                </div>
                <div className="cp-form-row cp-single">
                  <div className="cp-form-group">
                    <label className="cp-form-label">Bio / Description</label>
                    <textarea className="cp-form-textarea" placeholder="Write a short bio..." defaultValue="A certified guidance counselor specializing in career development and planning for college students."></textarea>
                  </div>
                </div>
                <div className="cp-form-actions">
                  <button className="cp-btn-primary">Save changes</button>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* ── MODALS ────────────────────────────────────────────────────── */}

      {showSlotForm && (
        <div className="cp-modal-overlay" onClick={() => { setShowSlotForm(false); setEditingSlot(null); setSlotFormError(''); }}>
          <div className="cp-modal" onClick={e => e.stopPropagation()}>
            <button className="cp-modal-close" onClick={() => { setShowSlotForm(false); setEditingSlot(null); setSlotFormError(''); }}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
            <div className="cp-modal-title">{editingSlot ? 'Edit Slot' : 'Create a New Slot'}</div>
            <div className="cp-modal-sub">Weekdays only · 8–12 AM or 1–5 PM</div>

            {/* Date */}
            <div className="cp-form-group" style={{ marginBottom: '14px' }}>
              <label className="cp-form-label">Date <span className="cp-form-hint">(Mon – Fri)</span></label>
              <input
                className="cp-form-input"
                type="date"
                value={slotForm.date}
                min={minDate}
                onChange={e => {
                  const val = e.target.value;
                  const updated = { ...slotForm, date: val };
                  updated.endTime = calculateEndTime(updated.startTime, updated.duration);
                  setSlotForm(updated);
                  setSlotFormError('');
                }}
              />
              {slotForm.date && !isWeekday(slotForm.date) && (
                <div className="cp-form-error">Please choose a weekday (Monday – Friday).</div>
              )}
            </div>

            {/* Start time + Duration */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '14px' }}>
              <div className="cp-form-group">
                <label className="cp-form-label">Start time</label>
                <select
                  className="cp-form-select"
                  value={slotForm.startTime}
                  onChange={e => {
                    const updated = { ...slotForm, startTime: e.target.value };
                    updated.endTime = calculateEndTime(updated.startTime, updated.duration);
                    setSlotForm(updated);
                    setSlotFormError('');
                  }}
                >
                  <option value="">Select time</option>
                  {/* Morning slots: 8:00–11:30 */}
                  {['08:00','08:30','09:00','09:30','10:00','10:30','11:00','11:30'].map(t => (
                    <option key={t} value={t}>{new Date(`1970-01-01T${t}`).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}</option>
                  ))}
                  {/* Afternoon slots: 13:00–16:30 */}
                  {['13:00','13:30','14:00','14:30','15:00','15:30','16:00','16:30'].map(t => (
                    <option key={t} value={t}>{new Date(`1970-01-01T${t}`).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}</option>
                  ))}
                </select>
              </div>
              <div className="cp-form-group">
                <label className="cp-form-label">Duration</label>
                <select
                  className="cp-form-select"
                  value={slotForm.duration}
                  onChange={e => {
                    const updated = { ...slotForm, duration: e.target.value };
                    updated.endTime = calculateEndTime(updated.startTime, updated.duration);
                    setSlotForm(updated);
                    setSlotFormError('');
                  }}
                >
                  <option value="15">15 min</option>
                  <option value="30">30 min</option>
                  <option value="45">45 min</option>
                  <option value="60">1 hr</option>
                </select>
              </div>
            </div>

            {/* End time preview */}
            {slotForm.startTime && slotForm.endTime && (
              <div className="cp-slot-time-preview">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="9"/><polyline points="12 7 12 12 15 15"/></svg>
                Slot: {new Date(`1970-01-01T${slotForm.startTime}`).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}
                {' → '}
                {new Date(`1970-01-01T${slotForm.endTime}`).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}
                {!isAllowedTime(slotForm.startTime, slotForm.duration) && (
                  <span className="cp-time-overflow-warn"> ⚠ Overflows allowed block</span>
                )}
              </div>
            )}

            {/* Repeat option — hidden when editing */}
            {!editingSlot && (
              <div className="cp-form-group" style={{ marginTop: '14px' }}>
                <label className="cp-form-label">Repeat</label>
                <div className="cp-repeat-options">
                  {[
                    { value: 'none',   label: 'No repeat', icon: '✕' },
                    { value: 'daily',  label: 'Daily (Mon – Fri)', icon: '↻' },
                    { value: 'weekly', label: 'Weekly', icon: '↺' },
                  ].map(opt => (
                    <button
                      key={opt.value}
                      type="button"
                      className={`cp-repeat-btn ${slotForm.repeat === opt.value ? 'cp-repeat-active' : ''}`}
                      onClick={() => setSlotForm(prev => ({ ...prev, repeat: opt.value, repeatUntil: '' }))}
                    >
                      <span className="cp-repeat-icon">{opt.icon}</span>
                      {opt.label}
                    </button>
                  ))}
                </div>
            

    {slotForm.repeat !== 'none' && (
      <div className="cp-repeat-until-wrap">
        <label className="cp-form-label" style={{ marginTop: '10px' }}>
          Repeat until <span className="cp-form-hint">(inclusive)</span>
        </label>
        <input
          type="date"
          className="cp-form-input"
          value={slotForm.repeatUntil}
          min={slotForm.date || minDate}
          onChange={e => setSlotForm(prev => ({ ...prev, repeatUntil: e.target.value }))}
        />
        {slotForm.date && slotForm.repeatUntil && (
          <div className="cp-repeat-info">
            {slotForm.repeat === 'daily'
              ? `Creates a slot every weekday from ${new Date(slotForm.date + 'T00:00:00').toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} to ${new Date(slotForm.repeatUntil + 'T00:00:00').toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} — weekends skipped.`
              : `Creates a slot every week on the same day from ${new Date(slotForm.date + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' })} until ${new Date(slotForm.repeatUntil + 'T00:00:00').toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}.`
            }
          </div>
        )}
      </div>
    )}
  </div>
)}

            {/* Error message */}
            {slotFormError && (
              <div className="cp-form-error cp-form-error-main">{slotFormError}</div>
            )}

            <div className="cp-modal-footer">
              <button className="cp-btn-secondary" onClick={() => { setShowSlotForm(false); setEditingSlot(null); setSlotFormError(''); }}>Cancel</button>
              <button className="cp-btn-primary" onClick={handleSlotSubmit}>
                {editingSlot ? 'Update Slot' : slotForm.repeat !== 'none' ? `Create Slots` : 'Create Slot'}
              </button>
            </div>
          </div>
        </div>
      )}

      {showDeleteModal && (
        <div className="cp-modal-overlay" onClick={() => { setShowDeleteModal(false); setDeletingSlotId(null); }}>
          <div className="cp-modal" onClick={e => e.stopPropagation()}>
            <div className="cp-modal-title">Delete Slot</div>
            <div className="cp-modal-sub">Are you sure you want to delete this slot? This action cannot be undone.</div>
            <div className="cp-modal-footer">
              <button className="cp-btn-secondary" onClick={() => { setShowDeleteModal(false); setDeletingSlotId(null); }}>Cancel</button>
              <button className="cp-btn-danger" onClick={handleDeleteSlot}>Delete</button>
            </div>
          </div>
        </div>
      )}

      {showApproveModal && (
        <div className="cp-modal-overlay" onClick={() => { setShowApproveModal(false); setActionId(null); }}>
          <div className="cp-modal" onClick={e => e.stopPropagation()}>
            <div className="cp-modal-title">Approve Request</div>
            <div className="cp-modal-sub">Are you sure you want to approve this appointment request?</div>
            <div className="cp-modal-footer">
              <button className="cp-btn-secondary" onClick={() => { setShowApproveModal(false); setActionId(null); }}>Cancel</button>
              <button className="cp-btn-primary" onClick={handleApprove}>Approve</button>
            </div>
          </div>
        </div>
      )}

      {showRejectModal && (
        <div className="cp-modal-overlay" onClick={() => { setShowRejectModal(false); setActionId(null); setRejectionReason(''); }}>
          <div className="cp-modal" onClick={e => e.stopPropagation()}>
            <div className="cp-modal-title">Reject Request</div>
            <div className="cp-modal-sub">Please provide a reason for rejecting this appointment.</div>
            <div className="cp-form-group">
              <label className="cp-form-label">Reason</label>
              <textarea className="cp-form-textarea" placeholder="Explain why..." value={rejectionReason} onChange={e => setRejectionReason(e.target.value)}></textarea>
            </div>
            <div className="cp-modal-footer">
              <button className="cp-btn-secondary" onClick={() => { setShowRejectModal(false); setActionId(null); setRejectionReason(''); }}>Cancel</button>
              <button className="cp-btn-danger" onClick={handleReject} disabled={actionLoading === actionId}>
                {actionLoading === actionId ? 'Rejecting...' : 'Reject'}
              </button>
            </div>
          </div>
        </div>
      )}

      {showDetailsModal && selectedAppointment && (
        <div className="cp-modal-overlay" onClick={() => setShowDetailsModal(false)}>
          <div className="cp-modal cp-modal-details-large" onClick={e => e.stopPropagation()}>
            <button className="cp-modal-close" onClick={() => setShowDetailsModal(false)}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
            <div className="cp-modal-title">Appointment Details</div>

            <div className="cp-details-user-info" style={{ marginTop: '20px' }}>
              <div className="cp-details-avatar">{selectedAppointment.studentFirstName.charAt(0)}{selectedAppointment.studentLastName.charAt(0)}</div>
              <div className="cp-details-user-text">
                <div className="cp-details-label-small">Student</div>
                <div className="cp-details-user-name">{selectedAppointment.studentFirstName} {selectedAppointment.studentLastName}</div>
                <div className="cp-details-user-id">ID: {selectedAppointment.studentIdNumber || '—'}</div>
              </div>
            </div>

            <div className="cp-details-grid-card">
              <div className="cp-details-grid-item"><span className="cp-details-grid-label">Program</span><span className="cp-details-grid-value">{selectedAppointment.studentProgram || '—'}</span></div>
              <div className="cp-details-grid-item"><span className="cp-details-grid-label">Year Level</span><span className="cp-details-grid-value">{selectedAppointment.studentYearLevel || '—'}</span></div>
              <div className="cp-details-grid-item"><span className="cp-details-grid-label">Gender</span><span className="cp-details-grid-value">{selectedAppointment.studentGender || '—'}</span></div>
              <div className="cp-details-grid-item">
                <span className="cp-details-grid-label">Status</span>
                <span className={`cp-status-pill cp-pill-${selectedAppointment.status.toLowerCase()}`}>{selectedAppointment.status}</span>
              </div>
            </div>

            <div style={{ marginBottom: '20px' }}>
              <div className="cp-details-label-small" style={{ marginBottom: '8px' }}>Schedule</div>
              <div className="cp-req-meta">
                <span className="cp-req-meta-item">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  {formatDateFull(selectedAppointment.startTime)}
                </span>
                <span className="cp-req-meta-item">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="9"/><polyline points="12 7 12 12 15 15"/></svg>
                  {formatTime(selectedAppointment.startTime)} → {formatTime(selectedAppointment.endTime)}
                </span>
              </div>
            </div>

            {selectedAppointment.note && (
              <div style={{ marginBottom: '20px' }}>
                <div className="cp-details-label-small" style={{ marginBottom: '8px' }}>Note from Student</div>
                <div style={{ padding: '14px', background: 'var(--cp-bg)', borderRadius: 'var(--cp-r-md)', border: '1px solid var(--cp-border)', fontStyle: 'italic', fontSize: '13px' }}>
                  {selectedAppointment.note}
                </div>
              </div>
            )}

            <div className="cp-modal-footer" style={{ justifyContent: 'space-between' }}>
              <button className="cp-btn-secondary" onClick={() => {
                if (selectedAppointment.studentSchoolIdPhotoUrl) {
                  setViewingPhotoUrl(selectedAppointment.studentSchoolIdPhotoUrl);
                  setShowPhotoModal(true);
                } else {
                  alert('No school photo ID uploaded.');
                }
              }}>View School ID</button>

              {selectedAppointment.status === 'PENDING' && (
                <div style={{ display: 'flex', gap: '10px' }}>
                  <button className="cp-btn-danger" onClick={() => { setShowDetailsModal(false); setActionId(selectedAppointment.id); setShowRejectModal(true); }}>Reject</button>
                  <button className="cp-btn-primary" onClick={() => { setShowDetailsModal(false); setActionId(selectedAppointment.id); setShowApproveModal(true); }}>Approve</button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {showPhotoModal && (
        <div className="cp-modal-overlay" onClick={() => setShowPhotoModal(false)}>
          <div className="cp-modal cp-photo-modal-box" onClick={e => e.stopPropagation()}>
            <button className="cp-modal-close" onClick={() => setShowPhotoModal(false)}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
            <div className="cp-modal-title">Student School ID</div>
            <div className="cp-photo-container">
              <img
                src={viewingPhotoUrl.startsWith('http') ? viewingPhotoUrl : `${API}/uploads/${viewingPhotoUrl}`}
                alt="Student ID"
                className="cp-student-id-image"
              />
            </div>
          </div>
        </div>
      )}

    </div>
  );
}

export default CounselorDashboard;