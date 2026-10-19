const $ = id => document.getElementById(id);
let cities = [];
let currentWeatherData = null;
let currentUnit = localStorage.getItem('weatherAppUnit') || 'C';
let chartInstance = null;

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
}

function dt(x) {
  return new Date(x * 1000).toLocaleDateString(undefined, { weekday: 'short', day: '2-digit', month: 'short' });
}

function time(x) {
  return new Date(x * 1000).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
}

function icon(i) {
  return i ? `https://openweathermap.org/img/wn/${i}@2x.png` : 'https://openweathermap.org/img/wn/01d@2x.png';
}

function formatTemp(celsius) {
  if (celsius == null) return '—';
  const val = currentUnit === 'F' ? (celsius * 9 / 5) + 32 : celsius;
  return Math.round(val) + '°' + currentUnit;
}

function convertVal(celsius) {
  if (celsius == null) return 0;
  return currentUnit === 'F' ? Math.round(((celsius * 9 / 5) + 32) * 10) / 10 : Math.round(celsius * 10) / 10;
}

function updateUnitButtons() {
  if (currentUnit === 'F') {
    $('unitF').classList.add('btn-primary');
    $('unitF').classList.remove('btn-outline-primary');
    $('unitC').classList.remove('btn-primary');
    $('unitC').classList.add('btn-outline-primary');
  } else {
    $('unitC').classList.add('btn-primary');
    $('unitC').classList.remove('btn-outline-primary');
    $('unitF').classList.remove('btn-primary');
    $('unitF').classList.add('btn-outline-primary');
  }
}

function alertBox(a) {
  const sev = (a.severity || 'Warning').toLowerCase();
  const badgeClass = sev.includes('severe') || sev.includes('extreme') ? 'bg-danger' : sev.includes('mod') ? 'bg-warning text-dark' : 'bg-info text-dark';
  return `
    <div class="alert-box">
      <div class="d-flex justify-content-between align-items-center mb-1">
        <strong>${esc(a.event || 'Weather Warning')}</strong>
        <span class="badge ${badgeClass}">${esc(a.severity || 'Warning')}</span>
      </div>
      <div class="small text-secondary mb-2">${esc(a.sender_name || 'Meteorological Service')} · ${time(a.start)} - ${time(a.end)}</div>
      <div class="small">${esc(a.description || 'Hazard advisory issued for this region.')}</div>
    </div>
  `;
}

async function loadCities() {
  updateUnitButtons();

  // Load preferences for unit
  try {
    const prefRes = await fetch('api/preferences');
    if (prefRes.ok) {
      const pref = await prefRes.json();
      if (pref.temperatureUnit) {
        currentUnit = pref.temperatureUnit;
        localStorage.setItem('weatherAppUnit', currentUnit);
        updateUnitButtons();
      }
    }
  } catch (_) {}

  // Load City Controller cities
  try {
    const r = await fetch('api/city-controller');
    if (r.ok) {
      const data = await r.json();
      if (Array.isArray(data) && data.length > 0) {
        cities = data.map(c => ({
          id: c.cityId,
          name: c.cityName,
          country: c.countryCode,
          lat: c.latitude,
          lon: c.longitude,
          isFavorite: !!c.favorite
        }));
      }
    }
  } catch (_) {}

  // Fallback to /api/cities if empty
  if (cities.length === 0) {
    try {
      const r2 = await fetch('api/cities');
      if (r2.ok) {
        const data2 = await r2.json();
        cities = data2.map(c => ({
          id: c.id,
          name: c.name,
          country: c.country,
          lat: c.lat,
          lon: c.lon,
          isFavorite: false
        }));
      }
    } catch (_) {}
  }

  $('city').innerHTML = cities.map(x => `<option value="${x.id}">${x.isFavorite ? '⭐ ' : ''}${esc(x.name)}, ${esc(x.country)}</option>`).join('');

  renderFavChips();

  // Check user default city from session
  try {
    const meRes = await fetch('api/me');
    if (meRes.ok) {
      const me = await meRes.json();
      if (me.cityId && cities.some(c => c.id === me.cityId)) {
        $('city').value = me.cityId;
      }
    }
  } catch (_) {}

  if (!$('city').value && cities.length > 0) {
    $('city').value = cities[0].id;
  }

  updateFavButton();
  loadWeather();
}

function renderFavChips() {
  const favs = cities.filter(c => c.isFavorite);
  const container = $('favChips');
  if (favs.length === 0) {
    container.innerHTML = `<span class="text-secondary small fst-italic">No favorites starred yet. Click the star to favorite!</span>`;
    return;
  }
  container.innerHTML = favs.map(f => `
    <span class="badge bg-primary-subtle text-primary border border-primary-subtle rounded-pill px-3 py-2 fav-chip" data-id="${f.id}">
      ⭐ ${esc(f.name)}
    </span>
  `).join('');

  container.querySelectorAll('.fav-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      $('city').value = chip.dataset.id;
      updateFavButton();
      loadWeather();
    });
  });
}

function updateFavButton() {
  const curId = parseInt($('city').value);
  const city = cities.find(c => c.id === curId);
  const isFav = city ? city.isFavorite : false;
  $('favStar').textContent = isFav ? '⭐' : '✩';
  $('heroFavStar').textContent = isFav ? '⭐' : '✩';
  $('favBtn').classList.toggle('btn-warning', isFav);
  $('favBtn').classList.toggle('btn-outline-warning', !isFav);
}

async function toggleFavorite() {
  const curId = parseInt($('city').value);
  if (!curId) return;

  try {
    const r = await fetch('api/city-controller', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'favorite', cityId: curId })
    });
    const d = await r.json();
    if (d.ok) {
      const city = cities.find(c => c.id === curId);
      if (city) {
        city.isFavorite = d.isFavorite;
        // update option text
        const opt = $('city').querySelector(`option[value="${curId}"]`);
        if (opt) {
          opt.textContent = `${city.isFavorite ? '⭐ ' : ''}${city.name}, ${city.country}`;
        }
      }
      updateFavButton();
      renderFavChips();
    }
  } catch (err) {
    console.error('Failed to toggle favorite:', err);
  }
}

async function loadWeather() {
  const id = $('city').value;
  if (!id) return;
  $('loading').classList.remove('d-none');
  $('weather').classList.add('d-none');
  updateFavButton();

  try {
    const r = await fetch(`api/weather?cityId=${id}`);
    const d = await r.json();
    if (!r.ok) throw Error(d.message);
    currentWeatherData = d;
    render(d);
  } catch (e) {
    $('msg').innerHTML = `<div class="alert alert-danger alert-dismissible fade show">${esc(e.message)}<button class="btn-close" data-bs-dismiss="alert"></button></div>`;
  } finally {
    $('loading').classList.add('d-none');
  }
}

function render(d) {
  const c = d.city || {};
  const w = d.current || {};
  const wd = (w.weather && w.weather[0]) ? w.weather[0] : {};

  $('place').textContent = `${c.name || 'Selected City'}, ${c.country || 'India'}`;
  $('temp').textContent = formatTemp(w.temp ?? w.temperatureC);
  $('condition').textContent = wd.description || wd.main || w.description || 'Clear Sky';
  $('feels').textContent = `Feels like ${formatTemp(w.feels_like ?? w.feelsLikeC)}`;
  $('icon').src = icon(wd.icon || w.icon);
  $('localTime').textContent = `Updated: ${new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;

  const windMps = w.wind_speed ?? w.windSpeedMps ?? 0;
  const windKmh = w.windSpeedKmh ?? Math.round(windMps * 3.6 * 10) / 10;
  const curRain = w.rainfallMm ?? (w.rain ? (w.rain['1h'] || w.rain['3h'] || 0) : 0);

  const m = [
    ['Humidity', (w.humidity ?? 0) + '%'],
    ['Wind Speed', `${windKmh} km/h (${windMps} m/s)`],
    ['Pressure', (w.pressure ?? 1013) + ' hPa'],
    ['Precipitation', `${curRain} mm`],
    ['Cloud Cover', (w.clouds ?? 0) + '%'],
    ['Visibility', w.visibility ? Math.round(w.visibility / 1000) + ' km' : '10 km']
  ];

  $('metrics').innerHTML = m.map(x => `
    <div class="col-6 col-md-4">
      <div class="metric">
        <div class="small text-white-50">${x[0]}</div>
        <div class="fw-semibold fs-6">${x[1]}</div>
      </div>
    </div>
  `).join('');

  // Next 24 Hours (Hourly Forecast)
  const hourly = d.hourly || [];
  if (hourly.length > 0) {
    $('hourlyList').innerHTML = hourly.slice(0, 8).map((h, idx) => {
      const hTime = idx === 0 ? 'Now' : h.time ? h.time.split(' ')[1]?.slice(0, 5) || time(h.dt) : time(h.dt);
      const hTemp = formatTemp(h.temperatureC ?? h.temp);
      const hPop = Math.round((h.precipitationProbability ?? h.pop ?? 0) * 100);
      const hIcon = icon(h.icon || (h.weather?.[0]?.icon));
      return `
        <div class="hourly-card shadow-sm">
          <div class="small fw-bold text-secondary">${hTime}</div>
          <img src="${hIcon}" alt="hourly icon">
          <div class="fw-bold fs-5">${hTemp}</div>
          <div class="small text-info fw-semibold mt-1">💧 ${hPop}%</div>
        </div>
      `;
    }).join('');
  } else {
    $('hourlyList').innerHTML = `<div class="text-secondary small">No hourly intervals returned by provider.</div>`;
  }

  // Render Dashboard Chart (Chart.js)
  renderDashboardChart(hourly);

  // 7-Day Daily Forecast
  const dailyData = d.daily?.data || [];
  $('forecast').innerHTML = dailyData.slice(0, 7).map(x => {
    const q = x.weather?.[0] || {};
    const dTemp = formatTemp(x.temp?.day ?? x.dayTempC);
    const minT = formatTemp(x.temp?.min ?? x.minTemperatureC);
    const maxT = formatTemp(x.temp?.max ?? x.maxTemperatureC);
    const rainChance = Math.round((x.pop ?? x.precipitationProbability ?? 0) * 100);
    return `
      <div class="col-6 col-md-3 col-lg">
        <div class="forecast-card shadow-sm">
          <div class="fw-bold">${dt(x.dt)}</div>
          <img src="${icon(q.icon || x.icon)}" alt="daily icon">
          <div class="fw-bold fs-5 text-primary my-1">${dTemp}</div>
          <div class="small text-secondary fw-semibold">${minT} / ${maxT}</div>
          <div class="small text-muted text-capitalize my-1">${esc(q.description || x.description || '')}</div>
          <div class="badge bg-info-subtle text-info rounded-pill px-2 py-1 mt-1">💧 ${rainChance}%</div>
        </div>
      </div>
    `;
  }).join('');

  // 1-Day Step Timeline
  $('timeline').innerHTML = dailyData.slice(0, 7).map(x => {
    const q = x.weather?.[0] || {};
    const morn = formatTemp(x.temp?.morn ?? x.morningTempC);
    const day = formatTemp(x.temp?.day ?? x.dayTempC);
    const night = formatTemp(x.temp?.night ?? x.nightTempC);
    return `
      <div class="timeline-item shadow-sm">
        <div class="fw-bold">${dt(x.dt)}</div>
        <img src="${icon(q.icon || x.icon)}" width="50" alt="step icon">
        <div class="fs-5 fw-bold text-primary">${day}</div>
        <div class="small text-capitalize text-muted">${esc(q.description || x.description || '')}</div>
        <div class="small text-secondary mt-2">🌅 Morn: ${morn} · 🌙 Night: ${night}</div>
      </div>
    `;
  }).join('');

  // Alerts
  const aa = d.alerts || [];
  $('alertCount').textContent = aa.length;
  $('alerts').innerHTML = aa.length ? aa.map(alertBox).join('') : `
    <div class="text-center py-4 text-secondary">
      <div class="fs-1 mb-1">🌤</div>
      <h6 class="fw-bold text-dark">No active alerts reported by the configured provider</h6>
      <p class="small text-muted mb-0">Normal atmospheric conditions reported for ${esc(c.name || 'this location')}.</p>
    </div>
  `;

  $('weather').classList.remove('d-none');
}

function renderDashboardChart(hourly) {
  if (chartInstance) chartInstance.destroy();
  const canvas = $('dashboardChart');
  if (!canvas) return;

  const ctx = canvas.getContext('2d');
  const labels = hourly.slice(0, 8).map((h, i) => i === 0 ? 'Now' : h.time ? h.time.split(' ')[1]?.slice(0, 5) || time(h.dt) : time(h.dt));
  const temps = hourly.slice(0, 8).map(h => convertVal(h.temperatureC ?? h.temp));
  const pops = hourly.slice(0, 8).map(h => Math.round((h.precipitationProbability ?? h.pop ?? 0) * 100));

  chartInstance = new Chart(ctx, {
    data: {
      labels: labels,
      datasets: [
        {
          type: 'line',
          label: `Temperature (°${currentUnit})`,
          data: temps,
          borderColor: '#0d6efd',
          backgroundColor: 'rgba(13, 110, 253, 0.12)',
          fill: true,
          tension: 0.35,
          pointRadius: 4,
          pointHoverRadius: 6,
          yAxisID: 'yTemp'
        },
        {
          type: 'bar',
          label: 'Precipitation Chance (%)',
          data: pops,
          backgroundColor: 'rgba(13, 202, 240, 0.35)',
          borderColor: 'rgba(13, 202, 240, 0.8)',
          borderWidth: 1,
          borderRadius: 4,
          yAxisID: 'yPop'
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      plugins: {
        legend: { position: 'top' },
        tooltip: {
          callbacks: {
            label: ctx => ` ${ctx.dataset.label}: ${ctx.parsed.y}${ctx.datasetIndex === 0 ? '°' + currentUnit : '%'}`
          }
        }
      },
      scales: {
        x: { grid: { display: false } },
        yTemp: {
          type: 'linear',
          position: 'left',
          ticks: { callback: v => v + '°' }
        },
        yPop: {
          type: 'linear',
          position: 'right',
          min: 0,
          max: 100,
          ticks: { callback: v => v + '%' },
          grid: { drawOnChartArea: false }
        }
      }
    }
  });
}

// Event Listeners
$('city').addEventListener('change', loadWeather);
$('refresh').addEventListener('click', loadWeather);
$('favBtn').addEventListener('click', toggleFavorite);
$('heroFavStar').addEventListener('click', toggleFavorite);

$('unitC').addEventListener('click', () => {
  if (currentUnit !== 'C') {
    currentUnit = 'C';
    localStorage.setItem('weatherAppUnit', 'C');
    updateUnitButtons();
    if (currentWeatherData) render(currentWeatherData);
  }
});

$('unitF').addEventListener('click', () => {
  if (currentUnit !== 'F') {
    currentUnit = 'F';
    localStorage.setItem('weatherAppUnit', 'F');
    updateUnitButtons();
    if (currentWeatherData) render(currentWeatherData);
  }
});

// Initialize
loadCities();
