// Weather Alerts & Historical Advisories
(function() {
  const $ = id => document.getElementById(id);

  function esc(s) {
    return String(s ?? '').replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
  }

  function formatEpoch(sec) {
    if (!sec) return '—';
    return new Date(sec * 1000).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  async function init() {
    try {
      const r = await fetch('api/cities');
      if (r.status === 401) { location.href = 'login.html'; return; }
      const cities = await r.json();

      $('citySelect').innerHTML = `<option value="">All Saved Locations</option>` + cities.map(c => `
        <option value="${c.id}">${esc(c.name)}, ${esc(c.country)}</option>
      `).join('');

      const meRes = await fetch('api/me');
      if (meRes.ok) {
        const me = await meRes.json();
        if (me.cityId && cities.some(c => c.id === me.cityId)) {
          $('citySelect').value = me.cityId;
        }
      }

      loadAlerts();
    } catch (e) {
      $('msg').innerHTML = `<div class="alert alert-danger">${esc(e.message)}</div>`;
    }

    $('citySelect').addEventListener('change', loadAlerts);
    $('refreshBtn').addEventListener('click', loadAlerts);
  }

  async function loadAlerts() {
    const cityId = $('citySelect').value;
    $('activeLoading').classList.remove('d-none');
    $('activeAlertsContainer').innerHTML = '';

    try {
      // 1. Fetch current weather for the city to see live provider alerts
      let liveAlerts = [];
      if (cityId) {
        const wRes = await fetch(`api/weather?cityId=${cityId}`);
        if (wRes.ok) {
          const wData = await wRes.json();
          liveAlerts = wData.alerts || [];
        }
      } else {
        const aRes = await fetch(`api/alerts`);
        if (aRes.ok) {
          const aData = await aRes.json();
          liveAlerts = aData.alerts || [];
        }
      }

      $('activeBadge').textContent = `${liveAlerts.length} Active`;
      if (liveAlerts.length === 0) {
        $('activeAlertsContainer').innerHTML = `
          <div class="alert alert-secondary border-0 text-center py-4 mb-0">
            <div class="fs-1 mb-2">🌤</div>
            <h5 class="fw-bold">No active alerts reported by the configured provider</h5>
            <p class="text-secondary mb-0">There are currently no active severe weather advisories reported for this location.</p>
          </div>
        `;
      } else {
        $('activeAlertsContainer').innerHTML = liveAlerts.map(a => `
          <div class="alert-box shadow-sm mb-3">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <h5 class="fw-bold text-danger mb-0">${esc(a.event || a.eventType || 'Weather Advisory')}</h5>
              <span class="badge bg-danger">${esc(a.severity || 'Warning')}</span>
            </div>
            <div class="small text-secondary mb-2">
              <strong>Source:</strong> ${esc(a.sender_name || 'Government Meteorological Authority')} | 
              <strong>Window:</strong> ${formatEpoch(a.start)} to ${formatEpoch(a.end)}
            </div>
            <p class="mb-0 text-dark">${esc(a.description || 'Take necessary precautions in according with regional safety advisories.')}</p>
          </div>
        `).join('');
      }

      // 2. Fetch History Records
      const histUrl = cityId ? `api/alerts?history=true&cityId=${cityId}` : `api/alerts?history=true`;
      const histRes = await fetch(histUrl);
      const histData = await histRes.json();
      const historyList = histData.alerts || [];

      if (historyList.length === 0) {
        $('historyTableBody').innerHTML = `
          <tr>
            <td colspan="6" class="text-center py-4 text-secondary">
              No historical advisory records logged yet. Records are saved when warnings are retrieved.
            </td>
          </tr>
        `;
      } else {
        $('historyTableBody').innerHTML = historyList.map(h => `
          <tr>
            <td><strong>${esc(h.cityName)}</strong>, ${esc(h.country)}</td>
            <td>${esc(h.eventType)}</td>
            <td><span class="badge ${h.severity === 'Severe' ? 'bg-danger' : 'bg-warning text-dark'}">${esc(h.severity)}</span></td>
            <td class="small">${esc(h.startTime || '—')} &rarr; ${esc(h.endTime || '—')}</td>
            <td class="small text-secondary">${esc(h.retrievedAt)}</td>
            <td>
              <span class="badge ${h.isActive ? 'bg-success' : 'bg-secondary'}">
                ${h.isActive ? 'Active' : 'Archived'}
              </span>
            </td>
          </tr>
        `).join('');
      }
    } catch (e) {
      $('msg').innerHTML = `<div class="alert alert-danger">${esc(e.message)}</div>`;
    } finally {
      $('activeLoading').classList.add('d-none');
    }
  }

  init();
})();
