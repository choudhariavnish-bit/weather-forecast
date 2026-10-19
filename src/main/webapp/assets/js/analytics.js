// Weather Analytics & Chart.js Visualizations
(function() {
  const $ = id => document.getElementById(id);
  let tempChartInstance = null;
  let rainChartInstance = null;
  let weeklyChartInstance = null;
  let currentUnit = localStorage.getItem('weatherAppUnit') || 'C';

  function esc(s) {
    return String(s ?? '').replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
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

  function formatTime(dtSec) {
    return new Date(dtSec * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  function formatDate(dtSec) {
    return new Date(dtSec * 1000).toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric' });
  }

  async function init() {
    $('unitBadge').textContent = '°' + currentUnit;
    try {
      // Get preferences for unit
      const prefRes = await fetch('api/preferences');
      if (prefRes.ok) {
        const pref = await prefRes.json();
        currentUnit = pref.temperatureUnit || 'C';
        localStorage.setItem('weatherAppUnit', currentUnit);
        $('unitBadge').textContent = '°' + currentUnit;
      }

      // Load Cities
      const r = await fetch('api/cities');
      if (r.status === 401) { location.href = 'login.html'; return; }
      const cities = await r.json();

      $('citySelect').innerHTML = cities.map(c => `
        <option value="${c.id}">${esc(c.name)}, ${esc(c.country)}</option>
      `).join('');

      // Check me for user city
      const meRes = await fetch('api/me');
      if (meRes.ok) {
        const me = await meRes.json();
        if (me.cityId && cities.some(c => c.id === me.cityId)) {
          $('citySelect').value = me.cityId;
        }
      }

      loadAnalytics();
    } catch (e) {
      $('msg').innerHTML = `<div class="alert alert-danger">${esc(e.message)}</div>`;
    }

    $('citySelect').addEventListener('change', loadAnalytics);
    $('refreshBtn').addEventListener('click', loadAnalytics);
  }

  async function loadAnalytics() {
    const cityId = $('citySelect').value;
    if (!cityId) return;

    $('loading').classList.remove('d-none');
    $('analyticsContent').classList.add('opacity-50');

    try {
      const res = await fetch(`api/weather?cityId=${cityId}`);
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Failed to fetch weather analytics');

      renderSummary(data);
      renderTemperatureChart(data.hourly || []);
      renderRainfallChart(data.hourly || []);
      renderWeeklyChart(data.daily?.data || []);
    } catch (e) {
      $('msg').innerHTML = `<div class="alert alert-danger alert-dismissible fade show">${esc(e.message)}<button class="btn-close" data-bs-dismiss="alert"></button></div>`;
    } finally {
      $('loading').classList.add('d-none');
      $('analyticsContent').classList.remove('opacity-50');
    }
  }

  function renderSummary(data) {
    const hourly = data.hourly || [];
    const daily = data.daily?.data || [];

    // 24H Average Temp
    if (hourly.length > 0) {
      const avg = hourly.reduce((sum, h) => sum + (h.temperatureC || h.temp || 0), 0) / hourly.length;
      $('statAvgTemp').textContent = formatTemp(avg);

      const maxPop = Math.round(Math.max(...hourly.map(h => h.precipitationProbability || h.pop || 0)) * 100);
      $('statMaxPop').textContent = maxPop + '%';
      $('statRainWarning').textContent = maxPop > 60 ? 'High probability of rain' : maxPop > 25 ? 'Scattered showers possible' : 'Dry conditions expected';
    }

    // Peak Wind
    const curWind = data.current?.windSpeedKmh || Math.round((data.current?.wind_speed || 0) * 3.6);
    $('statMaxWind').textContent = curWind + ' km/h';

    // Weekly Range
    if (daily.length > 0) {
      const mins = daily.map(d => d.minTemperatureC ?? d.temp?.min ?? 0);
      const maxs = daily.map(d => d.maxTemperatureC ?? d.temp?.max ?? 0);
      const minWeek = Math.min(...mins);
      const maxWeek = Math.max(...maxs);
      $('statWeeklyRange').textContent = `${formatTemp(minWeek)} / ${formatTemp(maxWeek)}`;
    }
  }

  function renderTemperatureChart(hourly) {
    if (tempChartInstance) tempChartInstance.destroy();
    const ctx = $('tempChart').getContext('2d');

    const labels = hourly.map(h => h.time ? h.time.split(' ')[1]?.slice(0, 5) || h.time : formatTime(h.dt));
    const temps = hourly.map(h => convertVal(h.temperatureC ?? h.temp));

    tempChartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [{
          label: `Temperature (°${currentUnit})`,
          data: temps,
          borderColor: '#0d6efd',
          backgroundColor: 'rgba(13, 110, 253, 0.12)',
          fill: true,
          tension: 0.38,
          pointRadius: 4,
          pointHoverRadius: 6,
          pointBackgroundColor: '#0d6efd'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: ctx => ` ${ctx.parsed.y}°${currentUnit}`
            }
          }
        },
        scales: {
          x: { grid: { display: false } },
          y: {
            ticks: {
              callback: val => val + '°'
            }
          }
        }
      }
    });
  }

  function renderRainfallChart(hourly) {
    if (rainChartInstance) rainChartInstance.destroy();
    const ctx = $('rainChart').getContext('2d');

    const labels = hourly.map(h => h.time ? h.time.split(' ')[1]?.slice(0, 5) || h.time : formatTime(h.dt));
    const rainMm = hourly.map(h => h.rainfallMm ?? 0);
    const pop = hourly.map(h => Math.round((h.precipitationProbability ?? h.pop ?? 0) * 100));

    rainChartInstance = new Chart(ctx, {
      data: {
        labels: labels,
        datasets: [
          {
            type: 'bar',
            label: 'Rainfall (mm)',
            data: rainMm,
            backgroundColor: 'rgba(54, 162, 235, 0.6)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 1,
            yAxisID: 'yRain'
          },
          {
            type: 'line',
            label: 'Precipitation Chance (%)',
            data: pop,
            borderColor: '#ff6384',
            backgroundColor: 'rgba(255, 99, 132, 0.1)',
            tension: 0.3,
            pointRadius: 3,
            yAxisID: 'yPop'
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'top' }
        },
        scales: {
          x: { grid: { display: false } },
          yRain: {
            type: 'linear',
            position: 'left',
            title: { display: true, text: 'Rainfall (mm)' },
            beginAtZero: true,
            suggestedMax: 5
          },
          yPop: {
            type: 'linear',
            position: 'right',
            title: { display: true, text: 'Chance (%)' },
            min: 0,
            max: 100,
            grid: { drawOnChartArea: false }
          }
        }
      }
    });
  }

  function renderWeeklyChart(daily) {
    if (weeklyChartInstance) weeklyChartInstance.destroy();
    const ctx = $('weeklyChart').getContext('2d');

    const labels = daily.map(d => d.date ? formatDate(d.dt) : 'Day');
    const highs = daily.map(d => convertVal(d.maxTemperatureC ?? d.temp?.max));
    const lows = daily.map(d => convertVal(d.minTemperatureC ?? d.temp?.min));

    weeklyChartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          {
            label: `Daily High (°${currentUnit})`,
            data: highs,
            borderColor: '#fd7e14',
            backgroundColor: 'rgba(253, 126, 20, 0.1)',
            tension: 0.3,
            pointRadius: 5,
            pointBackgroundColor: '#fd7e14'
          },
          {
            label: `Daily Low (°${currentUnit})`,
            data: lows,
            borderColor: '#0dcaf0',
            backgroundColor: 'rgba(13, 202, 240, 0.1)',
            tension: 0.3,
            pointRadius: 5,
            pointBackgroundColor: '#0dcaf0'
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'top' },
          tooltip: {
            callbacks: {
              label: ctx => ` ${ctx.dataset.label}: ${ctx.parsed.y}°${currentUnit}`
            }
          }
        },
        scales: {
          x: { grid: { display: false } },
          y: {
            ticks: {
              callback: val => val + '°'
            }
          }
        }
      }
    });
  }

  init();
})();
