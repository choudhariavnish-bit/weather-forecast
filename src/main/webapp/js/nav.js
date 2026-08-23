(async function(){
  const host=document.getElementById('appNav');
  if(!host)return;
  try{
    const r=await fetch('api/me');
    if(!r.ok){location.href='login.html';return;}
    const me=await r.json();
    host.innerHTML=`
    <nav class="navbar navbar-expand-lg navbar-dark weather-nav shadow-sm">
      <div class="container-fluid px-lg-4">
        <a class="navbar-brand fw-bold" href="dashboard.html">☁ Weather Portal</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav"><span class="navbar-toggler-icon"></span></button>
        <div class="collapse navbar-collapse" id="mainNav">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">
            <li class="nav-item"><a class="nav-link" href="dashboard.html">Dashboard</a></li>
            <li class="nav-item"><a class="nav-link" href="select-city.html">Select City</a></li>
            <li class="nav-item"><a class="nav-link" href="user-profile.html">User Profile</a></li>
            <li class="nav-item"><a class="nav-link" href="help.html">Help</a></li>
          </ul>
          <div class="d-flex align-items-center gap-2">
            <span class="navbar-text text-white">Welcome, ${esc(me.name)}</span>
            <button id="navLogout" class="btn btn-light btn-sm">Logout</button>
          </div>
        </div>
      </div>
    </nav>`;
    document.getElementById('navLogout').addEventListener('click',async()=>{await fetch('api/logout',{method:'POST'});location.href='login.html';});
    const current=location.pathname.split('/').pop()||'dashboard.html';
    host.querySelectorAll('.nav-link').forEach(a=>{if(a.getAttribute('href')===current)a.classList.add('active');});
  }catch(e){location.href='login.html';}
  function esc(s){return String(s??'').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));}
})();
