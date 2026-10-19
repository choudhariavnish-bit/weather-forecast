(async function(){
  // Auto-apply saved theme immediately
  const savedTheme = localStorage.getItem('weatherAppTheme') || 'light';
  document.documentElement.setAttribute('data-bs-theme', savedTheme);
  document.body.classList.toggle('dark-mode', savedTheme === 'dark');

  const host = document.getElementById('appNav');
  if (!host) return;

  function esc(s) {
    return String(s ?? '').replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
  }

  try {
    const r = await fetch('api/me');
    if (!r.ok) { location.href = 'login.html'; return; }
    const me = await r.json();

    host.innerHTML = `
    <nav class="navbar navbar-expand-lg navbar-dark weather-nav shadow-sm sticky-top">
      <div class="container-fluid px-lg-4">
        <a class="navbar-brand fw-bold d-flex align-items-center gap-2" href="dashboard.html">
          <span class="fs-4">☁</span> <span>Weather Portal</span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav" aria-controls="mainNav" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">
            <li class="nav-item"><a class="nav-link" href="dashboard.html">Dashboard</a></li>
            <li class="nav-item"><a class="nav-link" href="select-city.html">Select City</a></li>
            <li class="nav-item"><a class="nav-link" href="analytics.html">Analytics</a></li>
            <li class="nav-item"><a class="nav-link" href="alerts.html">Alerts</a></li>
            <li class="nav-item"><a class="nav-link" href="profile.html">Profile</a></li>
            <li class="nav-item"><a class="nav-link" href="help.html">Help</a></li>
            <li class="nav-item"><a class="nav-link" href="about.html">About</a></li>
          </ul>
          <div class="d-flex align-items-center gap-3">
            <button id="themeToggleBtn" class="btn btn-outline-light btn-sm rounded-pill px-2 py-1" title="Toggle Light/Dark Theme">
              <span id="themeIcon">${savedTheme === 'dark' ? '☀️' : '🌙'}</span>
            </button>
            <span class="navbar-text text-white small">
              👤 ${esc(me.name || 'User')}
            </span>
            <button id="navLogout" class="btn btn-light btn-sm rounded-pill px-3">Logout</button>
          </div>
        </div>
      </div>
    </nav>`;

    // Active Link Highlighting
    const current = location.pathname.split('/').pop() || 'dashboard.html';
    host.querySelectorAll('.nav-link').forEach(a => {
      const href = a.getAttribute('href');
      if (href === current || (current === 'user-profile.html' && href === 'profile.html')) {
        a.classList.add('active', 'fw-semibold');
      }
    });

    // Theme Toggle Handler
    const toggleBtn = document.getElementById('themeToggleBtn');
    if (toggleBtn) {
      toggleBtn.addEventListener('click', () => {
        const cur = document.documentElement.getAttribute('data-bs-theme') || 'light';
        const next = cur === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-bs-theme', next);
        document.body.classList.toggle('dark-mode', next === 'dark');
        localStorage.setItem('weatherAppTheme', next);
        const icon = document.getElementById('themeIcon');
        if (icon) icon.textContent = next === 'dark' ? '☀️' : '🌙';
      });
    }

    // Logout Handler
    const logoutBtn = document.getElementById('navLogout');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', async () => {
        await fetch('api/logout', { method: 'POST' });
        location.href = 'login.html';
      });
    }
  } catch (e) {
    location.href = 'login.html';
  }
})();
