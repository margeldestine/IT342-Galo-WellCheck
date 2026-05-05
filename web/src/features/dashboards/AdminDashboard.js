import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './AdminDashboard.css'; 

const API = process.env.REACT_APP_API_URL;

const AVATAR_COLORS = [
  '#2d5c45','#3b5a8a','#b45309','#7c3aed','#0f766e',
  '#be123c','#0369a1','#15803d','#92400e','#4338ca',
];

function avatarColor(name = '') {
  let h = 0;
  for (let i = 0; i < name.length; i++) h = name.charCodeAt(i) + ((h << 5) - h);
  return AVATAR_COLORS[Math.abs(h) % AVATAR_COLORS.length];
}

function initials(first = '', last = '') {
  return `${first.charAt(0)}${last.charAt(0)}`.toUpperCase();
}

function greeting() {
  const h = new Date().getHours();
  return h < 12 ? 'Good morning' : h < 17 ? 'Good afternoon' : 'Good evening';
}

function StatusPill({ status }) {
  const map = {
    ACTIVE:     'pill-active',
    PENDING:    'pill-pending',
    INACTIVE:   'pill-inactive',
    CONFIRMED:  'pill-active',
    REJECTED:   'pill-rejected',
    CANCELLED:  'pill-inactive',
  };
  const labels = {
    ACTIVE: 'Active', PENDING: 'Pending', INACTIVE: 'Inactive',
    CONFIRMED: 'Confirmed', REJECTED: 'Rejected', CANCELLED: 'Cancelled',
  };
  return (
    <span className={`status-pill ${map[status] || 'pill-inactive'}`}>
      {labels[status] || status}
    </span>
  );
}

function SpecBadge({ spec }) {
  if (!spec) return null;
  const s = spec.toLowerCase();
  let cls = 'spec-mental';
  if (s.includes('career'))   cls = 'spec-career';
  if (s.includes('academic')) cls = 'spec-academic';
  if (s.includes('personal')) cls = 'spec-personal';
  return <span className={`spec-badge ${cls}`}>{spec}</span>;
}

function Avatar({ first, last }) {
  const name = `${first} ${last}`;
  return (
    <div className="table-avatar" style={{ background: avatarColor(name) }}>
      {initials(first, last)}
    </div>
  );
}

function DonutChart({ data, total }) {
  const colors = {
    CONFIRMED: '#16a34a', PENDING: '#d97706',
    REJECTED: '#dc2626', CANCELLED: '#a1a1aa',
  };
  const r = 34, cx = 45, cy = 45;
  const circ = 2 * Math.PI * r;
  let offset = 0;
  const slices = Object.entries(data).map(([key, count]) => {
    const pct  = total > 0 ? count / total : 0;
    const dash = pct * circ;
    const s = { key, color: colors[key], dasharray: `${dash} ${circ}`, dashoffset: -offset };
    offset += dash;
    return s;
  });
  return (
    <svg className="donut-svg" width="90" height="90" viewBox="0 0 90 90">
      <circle cx={cx} cy={cy} r={r} fill="none" stroke="#f4f4f5" strokeWidth="14"/>
      {slices.map(s => (
        <circle key={s.key} cx={cx} cy={cy} r={r} fill="none"
          stroke={s.color} strokeWidth="14"
          strokeDasharray={s.dasharray}
          strokeDashoffset={s.dashoffset}
          transform={`rotate(-90 ${cx} ${cy})`}
        />
      ))}
      <text x={cx} y={cy + 5} textAnchor="middle" fontSize="14" fontWeight="700" fill="#0f0f12">{total}</text>
    </svg>
  );
}

const Icons = {
  grid:    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/></svg>,
  users:   <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>,
  user:    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>,
  cal:     <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><rect x="3" y="4" width="18" height="18" rx="2.5"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>,
  bars:    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>,
  logout:  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>,
  bell:    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg>,
  warn:    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>,
  search:  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>,
  clock:   <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><circle cx="12" cy="12" r="9"/><polyline points="12 7 12 12 15 15"/></svg>,
  download:<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>,
};

export default function AdminDashboard() {
  const navigate   = useNavigate();
  const user       = JSON.parse(localStorage.getItem('user') || '{}');
  const token      = localStorage.getItem('token');
  const authHeader = { headers: { Authorization: `Bearer ${token}` } };

  const [stats,        setStats]       = useState(null);
  const [counselors,   setCounselors]  = useState([]);
  const [students,     setStudents]    = useState([]);
  const [appointments, setAppts]       = useState([]);
  const [activeTab,    setActiveTab]   = useState('dashboard');
  const [apptFilter,   setApptFilter]  = useState('all');
  const [cSearch,      setCSearch]     = useState('');
  const [cStatusFilter,setCStatusFilter] = useState('all');
  const [apptSearch,   setApptSearch]  = useState('');
  const [loading,      setLoading]     = useState(false);
  const [toast,        setToast]       = useState(null);
  const [modal,        setModal]       = useState(null);

  useEffect(() => {
    fetchStats(); fetchCounselors(); fetchStudents(); fetchAppointments();
  }, []);

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3000);
  };

  const fetchStats        = async () => { try { const r = await axios.get(`${API}/admin/dashboard`,    authHeader); setStats(r.data);       } catch {} };
  const fetchCounselors   = async () => { try { const r = await axios.get(`${API}/admin/counselors`,   authHeader); setCounselors(r.data);  } catch {} };
  const fetchStudents     = async () => { try { const r = await axios.get(`${API}/admin/students`,     authHeader); setStudents(r.data);    } catch {} };
  const fetchAppointments = async () => { try { const r = await axios.get(`${API}/admin/appointments`, authHeader); setAppts(r.data);       } catch {} };

  const handleApprove = async (id) => {
    setLoading(true);
    try {
      await axios.put(`${API}/admin/counselors/${id}/approve`, {}, authHeader);
      showToast('Counselor approved successfully!');
      fetchCounselors(); fetchStats();
    } catch { showToast('Failed to approve counselor.', 'error'); }
    setLoading(false);
  };

  const handleReject = async (id) => {
    setLoading(true);
    try {
      await axios.put(`${API}/admin/counselors/${id}/reject`, {}, authHeader);
      showToast('Counselor rejected.');
      fetchCounselors(); fetchStats();
    } catch { showToast('Failed to reject counselor.', 'error'); }
    setLoading(false);
    setModal(null);
  };

  const handleDeactivate = async (id) => {
    setLoading(true);
    try {
      await axios.put(`${API}/admin/counselors/${id}/deactivate`, {}, authHeader);
      showToast('Counselor deactivated.');
      fetchCounselors(); fetchStats();
    } catch { showToast('Failed to deactivate counselor.', 'error'); }
    setLoading(false);
    setModal(null);
  };

  const confirmAction = () => {
    if (!modal) return;
    if (modal.type === 'reject')     handleReject(modal.id);
    if (modal.type === 'deactivate') handleDeactivate(modal.id);
  };

  const handleLogout = () => { localStorage.clear(); navigate('/'); };

  const pendingCounselors = counselors.filter(c => c.status === 'PENDING');

  const filteredCounselors = counselors.filter(c => {
    const matchSearch = `${c.firstName} ${c.lastName} ${c.email}`.toLowerCase().includes(cSearch.toLowerCase());
    const matchStatus = cStatusFilter === 'all' || c.status === cStatusFilter.toUpperCase();
    return matchSearch && matchStatus;
  });

  const filteredAppts = appointments.filter(a => {
    const matchTab    = apptFilter === 'all' || a.status === apptFilter.toUpperCase();
    const matchSearch = `${a.studentFirstName} ${a.studentLastName} ${a.counselorFirstName} ${a.counselorLastName}`
      .toLowerCase().includes(apptSearch.toLowerCase());
    return matchTab && matchSearch;
  });

  const apptByStatus = {
    CONFIRMED: appointments.filter(a => a.status === 'CONFIRMED').length,
    PENDING:   appointments.filter(a => a.status === 'PENDING').length,
    REJECTED:  appointments.filter(a => a.status === 'REJECTED').length,
    CANCELLED: appointments.filter(a => a.status === 'CANCELLED').length,
  };

  const programCounts = students.reduce((acc, s) => {
    acc[s.program] = (acc[s.program] || 0) + 1; return acc;
  }, {});
  const maxProgram = Math.max(...Object.values(programCounts), 1);

  const counselorApptCounts = counselors
    .filter(c => c.status === 'ACTIVE')
    .map(c => ({
      name:  `${c.firstName} ${c.lastName}`,
      count: appointments.filter(a =>
        a.counselorFirstName === c.firstName && a.counselorLastName === c.lastName
      ).length,
    }));
  const maxCounselorAppt = Math.max(...counselorApptCounts.map(x => x.count), 1);

  const today = new Date().toLocaleDateString('en-US', {
    weekday: 'short', month: 'short', day: 'numeric', year: 'numeric',
  });

  const navItems = [
    { id: 'dashboard',    label: 'Dashboard',    icon: Icons.grid  },
    { id: 'counselors',   label: 'Counselors',   icon: Icons.users,  badge: pendingCounselors.length },
    { id: 'students',     label: 'Students',     icon: Icons.user  },
    { id: 'appointments', label: 'Appointments', icon: Icons.cal,    badge: apptByStatus.PENDING },
    { id: 'reports',      label: 'Reports',      icon: Icons.bars  },
  ];

  const pageTitles = {
    dashboard: 'Dashboard', counselors: 'Counselors',
    students: 'Students', appointments: 'Appointments', reports: 'Reports',
  };

  return (
    <div className="app">

      <aside className="sidebar">

        <div className="admin-strip">
          <div className="admin-av" style={{ background: avatarColor(`${user.firstName}${user.lastName}`) }}>
            {initials(user.firstName || 'A', user.lastName || '')}
          </div>
          <div>
            <div className="admin-name">{user.firstName || 'Admin'} {user.lastName || ''}</div>
            <div className="admin-role">System Admin</div>
          </div>
        </div>

        <div className="nav-section-label">Overview</div>
        <nav className="nav">
          {navItems.slice(0, 1).map(item => (
            <button
              key={item.id}
              className={`nav-link ${activeTab === item.id ? 'active' : ''}`}
              onClick={() => setActiveTab(item.id)}
            >
              {item.icon}
              {item.label}
            </button>
          ))}

          <div className="nav-section-label">Management</div>
          {navItems.slice(1, 4).map(item => (
            <button
              key={item.id}
              className={`nav-link ${activeTab === item.id ? 'active' : ''}`}
              onClick={() => setActiveTab(item.id)}
            >
              {item.icon}
              {item.label}
              {item.badge > 0 && <span className="nav-badge">{item.badge}</span>}
            </button>
          ))}

          <div className="nav-section-label">Insights</div>
          {navItems.slice(4).map(item => (
            <button
              key={item.id}
              className={`nav-link ${activeTab === item.id ? 'active' : ''}`}
              onClick={() => setActiveTab(item.id)}
            >
              {item.icon}
              {item.label}
            </button>
          ))}
        </nav>

        <div className="sidebar-footer">
          <button className="logout-btn" onClick={handleLogout}>
            {Icons.logout} Log Out
          </button>
        </div>
      </aside>

      <main className="main">

        {toast && (
          <div className={`toast ${toast.type} show`} style={{ position:'fixed', top:20, right:20, zIndex:200 }}>
            {toast.msg}
          </div>
        )}

        {activeTab === 'dashboard' && (
          <section className="page active">

            <div className="hero-banner">
              <div className="hero-left">
                <div className="hero-eyebrow">Admin Control Center</div>
                <div className="hero-title">{greeting()}, {user.firstName || 'Admin'}.</div>
                <div className="hero-sub">
                  {apptByStatus.PENDING > 0
                    ? `${apptByStatus.PENDING} appointment${apptByStatus.PENDING > 1 ? 's' : ''} need attention.`
                    : 'System is running normally.'}
                </div>
              </div>
              <div className="hero-right">
                <div className="hero-num">{new Date().getDate()}</div>
                <div className="hero-num-label">
                  {new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
                </div>
              </div>
            </div>

            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-icon si-blue">{Icons.user}</div>
                <div className="stat-num">{stats?.totalStudents ?? '—'}</div>
                <div className="stat-label">Total Students</div>
                <div className="stat-sub">Registered accounts</div>
              </div>
              <div className="stat-card">
                <div className="stat-icon si-orange">{Icons.users}</div>
                <div className="stat-num">{stats?.totalCounselors ?? '—'}</div>
                <div className="stat-label">Total Counselors</div>
                <div className="stat-sub">Active counselors</div>
              </div>
              <div className="stat-card">
                <div className="stat-icon si-amber">{Icons.clock}</div>
                <div className="stat-num">{stats?.totalAppointments ?? '—'}</div>
                <div className="stat-label">Total Appointments</div>
                <div className="stat-sub">All time records</div>
              </div>
              <div className="stat-card highlight">
                <div className="stat-icon si-orange">{Icons.warn}</div>
                <div className="stat-num accent-num">{stats?.pendingCounselorApprovals ?? '—'}</div>
                <div className="stat-label">Pending Approvals</div>
                <div className="stat-sub">Awaiting review</div>
              </div>
            </div>

            <div className="dashboard-grid">

              <div className="card">
                <div className="card-label">Recent Activity</div>
                <div className="activity-list">
                  {appointments.slice(0, 5).map((a, i) => {
                    const dots = ['ad-gray','ad-blue','ad-green','ad-orange','ad-purple'];
                    return (
                      <div className="activity-item" key={a.id}>
                        <div className="activity-dot-wrap">
                          <div className={`activity-dot ${dots[i % dots.length]}`}/>
                        </div>
                        <div className="activity-text">
                          <div className="activity-msg">
                            <strong>{a.studentFirstName} {a.studentLastName}</strong>
                            {' '}booked with {a.counselorFirstName} {a.counselorLastName}.
                          </div>
                          <div className="activity-time">
                            {new Date(a.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                  {appointments.length === 0 && (
                    <div style={{ fontSize: 13, color: 'var(--text-3)', textAlign: 'center', padding: '24px 0' }}>
                      No recent activity.
                    </div>
                  )}
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                <div className="card">
                  <div className="card-label">System Health</div>
                  <div className="health-list">
                    {[
                      { label: 'Uptime',      val: '99%',  pct: 99, cls: 'hb-green',  color: 'var(--green)' },
                      { label: 'DB Load',     val: '34%',  pct: 34, cls: 'hb-blue'  },
                      { label: 'API Latency', val: '18ms', pct: 18, cls: 'hb-orange' },
                    ].map(h => (
                      <div className="health-item" key={h.label}>
                        <div className="health-label">{h.label}</div>
                        <div className="health-bar-wrap">
                          <div className={`health-bar ${h.cls}`} style={{ width: `${h.pct}%` }}/>
                        </div>
                        <div className="health-val" style={h.color ? { color: h.color } : {}}>
                          {h.val}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="card">
                  <div className="card-label">Quick Actions</div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {[
                      { label: 'View All Students',   tab: 'students',     icon: Icons.user  },
                      { label: 'Generate Report',     tab: 'reports',      icon: Icons.bars  },
                      { label: 'View Appointments',   tab: 'appointments', icon: Icons.cal   },
                    ].map(q => (
                      <button key={q.tab} className="btn-ghost-full" onClick={() => setActiveTab(q.tab)}>
                        {q.icon} {q.label}
                      </button>
                    ))}
                  </div>
                </div>
              </div>

            </div>
          </section>
        )}

        {activeTab === 'counselors' && (
          <section className="page active">
            <div className="page-header">
              <div>
                <div className="page-title">Counselors</div>
                <div className="page-sub">Manage all registered counselors in the system.</div>
              </div>
            </div>

            <div className="toolbar">
              <div className="search-wrap">
                {Icons.search}
                <input
                  className="search-input"
                  type="text"
                  placeholder="Search counselors…"
                  value={cSearch}
                  onChange={e => setCSearch(e.target.value)}
                />
              </div>
              <select
                className="filter-select"
                value={cStatusFilter}
                onChange={e => setCStatusFilter(e.target.value)}
              >
                <option value="all">All Status</option>
                <option value="active">Active</option>
                <option value="pending">Pending</option>
                <option value="inactive">Inactive</option>
              </select>
            </div>

            <div className="data-table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Counselor</th>
                    <th>Employee ID</th>
                    <th>Specialization</th>
                    <th>Sessions</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredCounselors.length === 0 ? (
                    <tr><td colSpan={6} style={{ textAlign:'center', color:'var(--text-3)', padding:32 }}>No counselors found.</td></tr>
                  ) : filteredCounselors.map(c => {
                    const name = `${c.firstName} ${c.lastName}`;
                    const sessions = appointments.filter(a =>
                      a.counselorFirstName === c.firstName && a.counselorLastName === c.lastName
                    ).length;
                    return (
                      <tr key={c.id}>
                        <td>
                          <div className="person-cell">
                            <Avatar first={c.firstName} last={c.lastName}/>
                            <div>
                              <div className="person-name">{name}</div>
                              <div className="person-id">{c.email}</div>
                            </div>
                          </div>
                        </td>
                        <td style={{ color:'var(--text-3)' }}>{c.employeeNumber}</td>
                        <td><SpecBadge spec={c.specialization}/></td>
                        <td>{sessions}</td>
                        <td><StatusPill status={c.status}/></td>
                        <td>
                          <div className="table-actions">
                            {c.status === 'PENDING' && <>
                              <button className="btn-ghost" onClick={() => handleApprove(c.id)} disabled={loading}>Approve</button>
                              <button className="btn-danger" onClick={() => setModal({ type:'reject', id:c.id, name })} disabled={loading}>Reject</button>
                            </>}
                            {c.status === 'ACTIVE' && (
                              <button className="btn-danger" onClick={() => setModal({ type:'deactivate', id:c.id, name })} disabled={loading}>Deactivate</button>
                            )}
                            {c.status === 'INACTIVE' && (
                              <button className="btn-ghost" onClick={() => handleApprove(c.id)} disabled={loading}>Reactivate</button>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </section>
        )}

        {activeTab === 'students' && (
          <section className="page active">
            <div className="page-header">
              <div>
                <div className="page-title">Students</div>
                <div className="page-sub">View all registered student accounts.</div>
              </div>
              <div style={{ fontSize:13, color:'var(--text-3)' }}>{students.length} registered accounts</div>
            </div>

            <div className="data-table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Student</th>
                    <th>Student ID</th>
                    <th>Program</th>
                    <th>Year Level</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {students.length === 0 ? (
                    <tr><td colSpan={5} style={{ textAlign:'center', color:'var(--text-3)', padding:32 }}>No students found.</td></tr>
                  ) : students.map(s => (
                      <tr key={s.id}>
                        <td>
                          <div className="person-cell">
                            <Avatar first={s.firstName} last={s.lastName}/>
                            <div>
                              <div className="person-name">{s.firstName} {s.lastName}</div>
                              <div className="person-id">{s.email}</div>
                            </div>
                          </div>
                        </td>
                        <td style={{ color:'var(--text-3)' }}>{s.studentIdNumber}</td>
                        <td><span className="spec-badge spec-career">{s.program}</span></td>
                        <td>{s.yearLevel}</td>
                        <td><StatusPill status={s.status}/></td>
                      </tr>
                    ))
                  }
                </tbody>
              </table>
            </div>
          </section>
        )}

        {activeTab === 'appointments' && (
          <section className="page active">
            <div className="page-header">
              <div>
                <div className="page-title">Appointments</div>
                <div className="page-sub">View all appointment records across the system.</div>
              </div>
            </div>

            <div style={{ display:'flex', alignItems:'center', gap:12, flexWrap:'wrap' }}>
              <div className="appt-tabs">
                {[
                  { key:'all',       label:`All (${appointments.length})` },
                  { key:'pending',   label:`Pending (${apptByStatus.PENDING})` },
                  { key:'confirmed', label:`Confirmed (${apptByStatus.CONFIRMED})` },
                  { key:'cancelled', label:`Cancelled (${apptByStatus.CANCELLED})` },
                  { key:'rejected',  label:`Rejected (${apptByStatus.REJECTED})` },
                ].map(t => (
                  <button
                    key={t.key}
                    className={`appt-tab ${apptFilter === t.key ? 'active' : ''}`}
                    onClick={() => setApptFilter(t.key)}
                  >
                    {t.label}
                  </button>
                ))}
              </div>

              <div className="search-wrap" style={{ maxWidth: 280 }}>
                {Icons.search}
                <input
                  className="search-input"
                  type="text"
                  placeholder="Search by student or counselor…"
                  value={apptSearch}
                  onChange={e => setApptSearch(e.target.value)}
                />
              </div>
            </div>

            <div className="data-table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Student</th>
                    <th>Counselor</th>
                    <th>Type</th>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredAppts.length === 0 ? (
                    <tr><td colSpan={6} style={{ textAlign:'center', color:'var(--text-3)', padding:32 }}>No appointments found.</td></tr>
                  ) : filteredAppts.map(a => {
                    const dt = new Date(a.startTime);
                    return (
                      <tr key={a.id}>
                        <td>
                          <div className="person-cell">
                            <Avatar first={a.studentFirstName} last={a.studentLastName}/>
                            <div>
                              <div className="person-name">{a.studentFirstName} {a.studentLastName}</div>
                              <div className="person-id">{a.studentProgram} · {a.studentYearLevel}</div>
                            </div>
                          </div>
                        </td>
                        <td>{a.counselorFirstName} {a.counselorLastName}</td>
                        <td><SpecBadge spec={a.counselorSpecialization}/></td>
                        <td>{dt.toLocaleDateString('en-US', { month:'long', day:'numeric', year:'numeric' })}</td>
                        <td>{dt.toLocaleTimeString('en-US', { hour:'numeric', minute:'2-digit' })}</td>
                        <td><StatusPill status={a.status}/></td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </section>
        )}

        {activeTab === 'reports' && (
          <section className="page active">
            <div className="page-header">
              <div>
                <div className="page-title">Reports &amp; Insights</div>
                <div className="page-sub">System usage statistics and summaries.</div>
              </div>
            </div>

            <div className="mini-stat-row">
              {[
                { num: stats?.totalStudents      ?? 0, label: 'Total Students'      },
                { num: stats?.totalCounselors     ?? 0, label: 'Total Counselors'    },
                { num: stats?.totalAppointments   ?? 0, label: 'Total Appointments'  },
              ].map(m => (
                <div className="mini-stat" key={m.label}>
                  <div className="mini-stat-num">{m.num}</div>
                  <div className="mini-stat-label">{m.label}</div>
                </div>
              ))}
            </div>

            <div className="reports-grid">

              <div className="report-card">
                <div className="report-head">
                  <div>
                    <div className="report-title">Appointments by Counselor</div>
                    <div className="report-sub">Breakdown of sessions per counselor</div>
                  </div>
                  <div className="report-icon si-blue">{Icons.users}</div>
                </div>
                <div className="bar-chart">
                  {counselorApptCounts.length === 0
                    ? <div style={{ fontSize:13, color:'var(--text-3)' }}>No data yet.</div>
                    : counselorApptCounts.map((c, i) => (
                      <div className="bar-row" key={c.name}>
                        <div className="bar-name">{c.name}</div>
                        <div className="bar-track">
                          <div className="bar-fill" style={{ width:`${(c.count / maxCounselorAppt) * 100}%`, background:'var(--blue)', opacity: 1 - i * 0.2 }}/>
                        </div>
                        <div className="bar-val">{c.count}</div>
                      </div>
                    ))
                  }
                </div>
              </div>

              <div className="report-card">
                <div className="report-head">
                  <div>
                    <div className="report-title">Appointment Status Split</div>
                    <div className="report-sub">Distribution of all appointment statuses</div>
                  </div>
                  <div className="report-icon si-amber">{Icons.clock}</div>
                </div>
                <div className="donut-wrap">
                  <DonutChart data={apptByStatus} total={appointments.length}/>
                  <div className="donut-legend">
                    {[
                      { label:'Confirmed', count:apptByStatus.CONFIRMED, color:'#16a34a' },
                      { label:'Pending',   count:apptByStatus.PENDING,   color:'#d97706' },
                      { label:'Rejected',  count:apptByStatus.REJECTED,  color:'#dc2626' },
                      { label:'Cancelled', count:apptByStatus.CANCELLED, color:'#a1a1aa' },
                    ].map(l => (
                      <div className="legend-item" key={l.label}>
                        <div className="legend-dot" style={{ background:l.color }}/>
                        {l.label} ({l.count})
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              <div className="report-card">
                <div className="report-head">
                  <div>
                    <div className="report-title">Students by Program</div>
                    <div className="report-sub">Enrollment distribution across programs</div>
                  </div>
                  <div className="report-icon si-green">{Icons.user}</div>
                </div>
                <div className="bar-chart">
                  {Object.keys(programCounts).length === 0
                    ? <div style={{ fontSize:13, color:'var(--text-3)' }}>No data yet.</div>
                    : Object.entries(programCounts).map(([prog, count]) => (
                      <div className="bar-row" key={prog}>
                        <div className="bar-name">{prog}</div>
                        <div className="bar-track">
                          <div className="bar-fill" style={{ width:`${(count / maxProgram) * 100}%`, background:'var(--green)' }}/>
                        </div>
                        <div className="bar-val">{count}</div>
                      </div>
                    ))
                  }
                </div>
              </div>

              <div className="report-card">
                <div className="report-head">
                  <div>
                    <div className="report-title">Counselor Slot Utilization</div>
                    <div className="report-sub">Available vs. booked slots per counselor</div>
                  </div>
                  <div className="report-icon si-orange">{Icons.cal}</div>
                </div>
                <div className="bar-chart">
                  {counselorApptCounts.length === 0
                    ? <div style={{ fontSize:13, color:'var(--text-3)' }}>No data yet.</div>
                    : counselorApptCounts.map(c => (
                      <div className="bar-row" key={c.name}>
                        <div className="bar-name">{c.name}</div>
                        <div className="bar-track">
                          <div className="bar-fill" style={{ width:`${(c.count / maxCounselorAppt) * 100}%`, background:'var(--accent)' }}/>
                        </div>
                        <div className="bar-val">{c.count}</div>
                      </div>
                    ))
                  }
                </div>
              </div>

            </div>
          </section>
        )}

      </main>

      {modal && (
        <div className="modal-overlay open" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className={`confirm-icon ${modal.type === 'reject' ? 'danger' : 'warning'}`}>
              {Icons.warn}
            </div>
            <div className="confirm-title">
              {modal.type === 'reject' ? `Reject ${modal.name}?` : `Deactivate ${modal.name}?`}
            </div>
            <div className="confirm-sub">
              {modal.type === 'reject'
                ? 'This will permanently remove this counselor registration. This action cannot be undone.'
                : 'This will prevent the counselor from accessing the portal. You can reactivate them later.'}
            </div>
            <div className="modal-footer">
              <button className="btn-ghost" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn-red" onClick={confirmAction} disabled={loading}>
                {modal.type === 'reject' ? 'Reject' : 'Deactivate'}
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}