function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
}

async function loadProfileData() {
  try {
    // Load Profile
    const pr = await fetch('api/profile');
    if (!pr.ok) { location.href = 'login.html'; return; }
    const p = await pr.json();
    const nameEl = document.getElementById('profName');
    const mobileEl = document.getElementById('profMobile');
    const emailEl = document.getElementById('profEmail');
    const cityEl = document.getElementById('profCity');
    if (nameEl) nameEl.textContent = p.name || '—';
    if (mobileEl) mobileEl.textContent = p.mobile || '—';
    if (emailEl) emailEl.textContent = p.email || '—';
    if (cityEl) cityEl.textContent = (p.city ? p.city + ', ' + p.country : '—');

    const loadingEl = document.getElementById('profileLoading');
    const detailsEl = document.getElementById('profileDetails');
    if (loadingEl) loadingEl.classList.add('d-none');
    if (detailsEl) detailsEl.classList.remove('d-none');

    // Load Cities for default selection
    const cr = await fetch('api/cities');
    const cities = await cr.json();
    const citySelect = document.getElementById('defaultCity');
    if (citySelect) {
      citySelect.innerHTML = cities.map(c => `<option value="${c.id}">${esc(c.name)}, ${esc(c.country)}</option>`).join('');
    }

    // Load Preferences
    const prefRes = await fetch('api/preferences');
    if (prefRes.ok) {
      const pref = await prefRes.json();
      const tempUnit = document.getElementById('tempUnit');
      const themeMode = document.getElementById('themeMode');
      const rainThreshold = document.getElementById('rainThreshold');
      const notifEnabled = document.getElementById('notifEnabled');

      if (tempUnit) tempUnit.value = pref.temperatureUnit || 'C';
      if (themeMode) themeMode.value = pref.theme || 'light';
      if (pref.defaultCityId && citySelect) citySelect.value = pref.defaultCityId;
      if (rainThreshold) rainThreshold.value = pref.rainAlertThreshold ?? 70;
      if (notifEnabled) notifEnabled.checked = pref.notificationsEnabled !== false;
    }
  } catch (e) {
    const msgEl = document.getElementById('msg');
    if (msgEl) msgEl.innerHTML = `<div class="alert alert-danger">${esc(e.message)}</div>`;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  loadProfileData();

  const prefForm = document.getElementById('prefForm');
  if (prefForm) {
    prefForm.addEventListener('submit', async e => {
      e.preventDefault();
      const body = {
        temperatureUnit: document.getElementById('tempUnit').value,
        theme: document.getElementById('themeMode').value,
        defaultCityId: document.getElementById('defaultCity').value,
        rainAlertThreshold: document.getElementById('rainThreshold').value,
        notificationsEnabled: document.getElementById('notifEnabled').checked
      };

      try {
        const r = await fetch('api/preferences', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        });
        const d = await r.json();
        if (!r.ok) throw new Error(d.message || 'Failed to save preferences');

        // Apply theme immediately
        document.documentElement.setAttribute('data-bs-theme', body.theme);
        document.body.classList.toggle('dark-mode', body.theme === 'dark');
        localStorage.setItem('weatherAppTheme', body.theme);
        localStorage.setItem('weatherAppUnit', body.temperatureUnit);

        const msgEl = document.getElementById('msg');
        if (msgEl) msgEl.innerHTML = `<div class="alert alert-success alert-dismissible fade show">Preferences saved successfully!<button class="btn-close" data-bs-dismiss="alert"></button></div>`;
      } catch (err) {
        const msgEl = document.getElementById('msg');
        if (msgEl) msgEl.innerHTML = `<div class="alert alert-danger alert-dismissible fade show">${esc(err.message)}<button class="btn-close" data-bs-dismiss="alert"></button></div>`;
      }
    });
  }
});
