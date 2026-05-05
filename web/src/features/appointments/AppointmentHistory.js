import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import CounselorSidebar from '../../components/CounselorSidebar';
import './AppointmentHistory.css';

const API = process.env.REACT_APP_API_URL;

function AppointmentHistory() {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');

  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchName, setSearchName] = useState('');
  const [searchDate, setSearchDate] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    setLoading(true);
    try {
      const res = await axios.get(`${API}/appointments/counselor`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      // Past appointments only: completed, rejected, cancelled, confirmed that are past
      const now = new Date();
      const past = res.data.filter(a => {
        const isPastStatus = ['REJECTED', 'CANCELLED', 'CONFIRMED', 'COMPLETED'].includes(a.status);
        const isPastDate = new Date(a.endTime || a.startTime) < now;
        return isPastStatus || isPastDate;
      });
      // Sort newest first
      past.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
      setAppointments(past);
    } catch (err) {
      console.error('Failed to fetch history:', err);
    }
    setLoading(false);
  };

  const formatTime = (dt) =>
    new Date(dt).toLocaleString('en-US', { hour: '2-digit', minute: '2-digit' });

  const formatDateFull = (dt) =>
    new Date(dt).toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'long',
      day: 'numeric',
      year: 'numeric',
    });

  const formatDateInput = (dt) =>
    new Date(dt).toISOString().split('T')[0];

  const statusOptions = ['ALL', 'CONFIRMED', 'COMPLETED', 'REJECTED', 'CANCELLED'];

  const filtered = appointments.filter(a => {
    const fullName = `${a.studentFirstName} ${a.studentLastName}`.toLowerCase();
    const matchesName = fullName.includes(searchName.toLowerCase()) ||
      (a.studentIdNumber && a.studentIdNumber.toLowerCase().includes(searchName.toLowerCase()));
    const matchesDate = !searchDate || formatDateInput(a.startTime) === searchDate;
    const matchesStatus = filterStatus === 'ALL' || a.status === filterStatus;
    return matchesName && matchesDate && matchesStatus;
  });

  const pendingCount = appointments.filter(a => a.status === 'PENDING').length;

  const statusMeta = {
    CONFIRMED:  { label: 'Confirmed',  cls: 'ah-pill-confirmed' },
    COMPLETED:  { label: 'Completed',  cls: 'ah-pill-completed' },
    REJECTED:   { label: 'Rejected',   cls: 'ah-pill-rejected' },
    CANCELLED:  { label: 'Cancelled',  cls: 'ah-pill-cancelled' },
    PENDING:    { label: 'Pending',    cls: 'ah-pill-pending' },
  };

  const clearFilters = () => {
    setSearchName('');
    setSearchDate('');
    setFilterStatus('ALL');
  };

  const hasFilters = searchName || searchDate || filterStatus !== 'ALL';

  return (
    <div className="cp-app">
      <CounselorSidebar
        activeItem="slots"
        onTabChange={(tab) => navigate(`/counselor/dashboard?tab=${tab}`)}
        pendingCount={pendingCount}
      />

      <main className="cp-main">
        <div className="ah-page">

          {/* ── Header ───────────────────────────────────────────────── */}
          <div className="ah-page-header">
            <div className="ah-header-left">
              <button className="ah-back-btn" onClick={() => navigate(-1)}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polyline points="15 18 9 12 15 6"/>
                </svg>
                Back
              </button>
              <div>
                <div className="ah-page-title">Appointment History</div>
                <div className="ah-page-sub">All past and closed appointment records.</div>
              </div>
            </div>
            <div className="ah-total-badge">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                <path d="M12 8v4l3 3"/>
                <path d="M3.05 11a9 9 0 1 0 .5-4"/>
                <path d="M3 3v4h4"/>
              </svg>
              {appointments.length} total records
            </div>
          </div>

          {/* ── Filters ──────────────────────────────────────────────── */}
          <div className="ah-filters">
            {/* Name search */}
            <div className="ah-search-wrap">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                <circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              <input
                type="text"
                className="ah-search-input"
                placeholder="Search by student name or ID..."
                value={searchName}
                onChange={e => setSearchName(e.target.value)}
              />
              {searchName && (
                <button className="ah-input-clear" onClick={() => setSearchName('')}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                </button>
              )}
            </div>

            {/* Date filter */}
            <div className="ah-date-wrap">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="3" y1="10" x2="21" y2="10"/>
                <line x1="8" y1="2" x2="8" y2="6"/><line x1="16" y1="2" x2="16" y2="6"/>
              </svg>
              <input
                type="date"
                className="ah-date-input"
                value={searchDate}
                onChange={e => setSearchDate(e.target.value)}
              />
              {searchDate && (
                <button className="ah-input-clear" onClick={() => setSearchDate('')}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                </button>
              )}
            </div>

            {/* Status tabs */}
            <div className="ah-status-tabs">
              {statusOptions.map(s => (
                <button
                  key={s}
                  className={`ah-status-tab ${filterStatus === s ? 'ah-active' : ''}`}
                  onClick={() => setFilterStatus(s)}
                >
                  {s === 'ALL' ? 'All' : s.charAt(0) + s.slice(1).toLowerCase()}
                </button>
              ))}
            </div>

            {hasFilters && (
              <button className="ah-clear-btn" onClick={clearFilters}>
                Clear filters
              </button>
            )}
          </div>

          {/* ── Result count ─────────────────────────────────────────── */}
          <div className="ah-results-bar">
            <span className="ah-results-count">
              Showing <strong>{filtered.length}</strong> of {appointments.length} records
            </span>
          </div>

          {/* ── Table / List ─────────────────────────────────────────── */}
          {loading ? (
            <div className="ah-empty">
              <div className="ah-empty-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.4">
                  <circle cx="12" cy="12" r="9"/><polyline points="12 7 12 12 15 15"/>
                </svg>
              </div>
              <p>Loading records...</p>
            </div>
          ) : filtered.length === 0 ? (
            <div className="ah-empty">
              <div className="ah-empty-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.4">
                  <circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
              </div>
              <p>{hasFilters ? 'No records match your filters.' : 'No appointment history yet.'}</p>
              {hasFilters && (
                <button className="ah-clear-btn ah-clear-center" onClick={clearFilters}>
                  Clear filters
                </button>
              )}
            </div>
          ) : (
            <div className="ah-list">
              {filtered.map((apt, idx) => {
                const meta = statusMeta[apt.status] || { label: apt.status, cls: 'ah-pill-cancelled' };
                return (
                  <div
                    key={apt.id}
                    className="ah-row"
                    style={{ animationDelay: `${Math.min(idx * 0.03, 0.3)}s` }}
                  >
                    {/* Avatar */}
                    <div className="ah-avatar">
                      {apt.studentFirstName.charAt(0)}{apt.studentLastName.charAt(0)}
                    </div>

                    {/* Student info */}
                    <div className="ah-student">
                      <div className="ah-student-name">
                        {apt.studentFirstName} {apt.studentLastName}
                      </div>
                      <div className="ah-student-meta">
                        {apt.studentIdNumber && (
                          <span className="ah-meta-chip">ID: {apt.studentIdNumber}</span>
                        )}
                        {apt.studentProgram && (
                          <span className="ah-meta-chip">{apt.studentProgram}</span>
                        )}
                        {apt.studentYearLevel && (
                          <span className="ah-meta-chip">Year {apt.studentYearLevel}</span>
                        )}
                      </div>
                    </div>

                    {/* Date & time */}
                    <div className="ah-schedule">
                      <div className="ah-sched-date">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <rect x="3" y="4" width="18" height="18" rx="2"/>
                          <line x1="3" y1="10" x2="21" y2="10"/>
                        </svg>
                        {formatDateFull(apt.startTime)}
                      </div>
                      <div className="ah-sched-time">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <circle cx="12" cy="12" r="9"/><polyline points="12 7 12 12 15 15"/>
                        </svg>
                        {formatTime(apt.startTime)} → {formatTime(apt.endTime)}
                      </div>
                    </div>

                    {/* Note indicator */}
                    {apt.note ? (
                      <div className="ah-note-indicator" title={apt.note}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                          <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
                        </svg>
                      </div>
                    ) : (
                      <div className="ah-note-placeholder" />
                    )}

                    {/* Status pill */}
                    <span className={`ah-pill ${meta.cls}`}>{meta.label}</span>
                  </div>
                );
              })}
            </div>
          )}

        </div>
      </main>
    </div>
  );
}

export default AppointmentHistory;