(() => {
  const candidates = window.TALENTLENS_CANDIDATES || [];
  const $ = (id) => document.getElementById(id);
  const allSkills = [...new Set(candidates.flatMap(c => c.skills))].sort();
  const allCities = [...new Set(candidates.map(c => c.location))].sort();
  let activePage = "search";
  let currentResults = [];
  let databasePage = 0;
  const pageSize = 15;
  const normalize = value => String(value ?? "").toLowerCase().trim().replace(/\s+/g, " ");
  const initials = name => name.split(/\s+/).slice(0,2).map(s=>s[0]).join("").toUpperCase();
  const escapeHtml = value => String(value ?? "").replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
  const scoreCandidate = (candidate, required, minExp, location) => {
    const found = required.filter(skill => candidate.skills.some(actual => actual === skill || actual.includes(skill) || skill.includes(actual) && skill.length > 4));
    const skillScore = required.length ? found.length / required.length * 100 : 100;
    const expScore = Math.min(100, candidate.experience / 12 * 100);
    const locationScore = !location || location === "any" || normalize(candidate.location) === normalize(location) ? 100 : 0;
    return {candidate, found, score: 0.5*skillScore + 0.3*expScore + 0.2*locationScore};
  };
  function runSearch(event) {
    if (event) event.preventDefault();
    const required = $("skills-input").value.split(",").map(normalize).filter(Boolean);
    const minExp = Number($("experience-input").value);
    const location = $("location-input").value;
    currentResults = candidates.map(c => scoreCandidate(c, required, minExp, location))
      .filter(r => r.candidate.experience >= minExp)
      .filter(r => required.length === 0 || r.found.length > 0)
      .sort((a,b)=>b.score-a.score || b.found.length-a.found.length || b.candidate.experience-a.candidate.experience);
    $("results-caption").textContent = required.length ? `Matching ${required.join(", ")} · preferred ${location} · minimum ${minExp} years` : `Profiles ranked by experience and preferred location (${location}).`;
    $("result-count").textContent = `${currentResults.length} matching ${currentResults.length===1?"profile":"profiles"}`;
    renderResults();
  }
  function renderResults() {
    const list = $("results-list");
    $("empty-state").classList.toggle("hidden", currentResults.length > 0);
    list.innerHTML = currentResults.slice(0,100).map((r,i) => {
      const c=r.candidate;
      return `<article class="candidate-card">
        <div class="candidate-top"><div class="avatar">${escapeHtml(initials(c.name))}</div><div class="candidate-main"><div class="candidate-name">${i+1}. ${escapeHtml(c.name)}</div><div class="candidate-sub">${escapeHtml(c.education)} · ${escapeHtml(c.location)}</div></div><div class="score-block"><div class="score">${r.score.toFixed(1)}</div><div class="score-label">FIT / 100</div></div></div>
        <div class="skill-tags">${c.skills.slice(0,11).map(s=>`<span class="skill-tag">${escapeHtml(s)}</span>`).join("")}</div>
        <div class="candidate-meta"><strong>${c.experience} years experience</strong> · ${escapeHtml(c.email)}<br>${escapeHtml(c.summary)}</div>
        <div class="card-actions"><button class="text-btn" data-profile="${c.id}">View candidate profile ↗</button></div>
      </article>`;
    }).join("");
  }
  function renderDatabase() {
    const q=normalize($("database-search").value);
    const filtered=candidates.filter(c=>normalize([c.name,c.email,c.education,c.location,c.summary,...c.skills].join(" ")).includes(q));
    const pages=Math.max(1,Math.ceil(filtered.length/pageSize));
    databasePage=Math.min(databasePage,pages-1);
    const pageRows=filtered.slice(databasePage*pageSize,(databasePage+1)*pageSize);
    $("database-body").innerHTML=pageRows.map(c=>`<tr>
      <td><div class="table-person"><div class="avatar">${escapeHtml(initials(c.name))}</div><div><strong>${escapeHtml(c.name)}</strong><small>${escapeHtml(c.email)}</small></div></div></td>
      <td>${c.experience} yrs</td><td>${escapeHtml(c.location)}</td><td>${escapeHtml(c.education)}</td>
      <td>${c.skills.slice(0,4).map(s=>`<span class="pill">${escapeHtml(s)}</span>`).join(" ")}</td>
      <td><button class="text-btn" data-profile="${c.id}">View ↗</button></td></tr>`).join("");
    $("database-count").textContent=`Showing ${filtered.length ? databasePage*pageSize+1 : 0}–${Math.min((databasePage+1)*pageSize,filtered.length)} of ${filtered.length} profiles`;
    $("prev-page").disabled=databasePage===0;
    $("next-page").disabled=databasePage>=pages-1;
  }
  function renderInsights() {
    const expCounts=Array.from({length:13},(_,i)=>({label:`${i} yrs`,count:candidates.filter(c=>c.experience===i).length}));
    const maxExp=Math.max(...expCounts.map(x=>x.count));
    $("experience-chart").innerHTML=expCounts.filter((x,i)=>i%2===0 || i===12).map(x=>`<div class="bar-row"><span>${x.label}</span><div class="bar-track"><span style="width:${x.count/maxExp*100}%"></span></div><span class="bar-value">${x.count}</span></div>`).join("");
    const counts={}; candidates.forEach(c=>c.skills.forEach(s=>counts[s]=(counts[s]||0)+1));
    const top=Object.entries(counts).sort((a,b)=>b[1]-a[1]).slice(0,8), max=top[0]?.[1]||1;
    $("skills-chart").innerHTML=top.map(([skill,count])=>`<div class="skill-row"><span>${escapeHtml(skill)}</span><div class="bar-track"><span style="width:${count/max*100}%"></span></div><span class="bar-value">${count}</span></div>`).join("");
  }
  function showProfile(id) {
    const c=candidates.find(x=>x.id===Number(id)); if(!c)return;
    const modal=$("profile-modal");
    $("modal-content").innerHTML=`<div class="profile-head"><div class="avatar">${escapeHtml(initials(c.name))}</div><div><h2>${escapeHtml(c.name)}</h2><p>${escapeHtml(c.education)} · ${escapeHtml(c.location)}</p></div></div>
      <div class="detail-grid"><div class="detail-box"><small>Experience</small><strong>${c.experience} years</strong></div><div class="detail-box"><small>Candidate ID</small><strong>TL-${String(c.id).padStart(4,"0")}</strong></div><div class="detail-box"><small>Email</small><strong>${escapeHtml(c.email)}</strong></div><div class="detail-box"><small>Skills listed</small><strong>${c.skills.length} skills</strong></div></div>
      <div class="modal-label">PROFILE SUMMARY</div><p class="modal-summary">${escapeHtml(c.summary)}</p><div class="modal-label">SKILLS & TECHNOLOGIES</div><div class="modal-skills">${c.skills.map(s=>`<span class="skill-tag">${escapeHtml(s)}</span>`).join("")}</div>`;
    modal.classList.remove("hidden");
  }
  function navigate(page) {
    activePage=page;
    document.querySelectorAll(".nav-item").forEach(b=>b.classList.toggle("active",b.dataset.page===page));
    document.querySelectorAll(".page-view").forEach(el=>el.classList.add("hidden"));
    $(`page-${page}`).classList.remove("hidden");
    const meta={
      search:["Candidate screening","Find the right people for your role, with transparent fit scores."],
      database:["Resume database","Browse, inspect, and search the synthetic candidate pool."],
      insights:["Matching insights","Explore candidate-pool patterns and the logic behind each score."]
    }[page];
    $("page-title").textContent=meta[0]; $("page-subtitle").textContent=meta[1];
    if(page==="database")renderDatabase();
    if(page==="insights")renderInsights();
  }
  document.querySelectorAll("[data-page]").forEach(el=>el.addEventListener("click",e=>{e.preventDefault();navigate(el.dataset.page)}));
  $("search-form").addEventListener("submit",runSearch);
  $("experience-input").addEventListener("input",()=>{$("experience-label").textContent=`${$("experience-input").value} ${$("experience-input").value==="1"?"year":"years"}`});
  $("database-search").addEventListener("input",()=>{databasePage=0;renderDatabase()});
  $("prev-page").addEventListener("click",()=>{databasePage--;renderDatabase()});
  $("next-page").addEventListener("click",()=>{databasePage++;renderDatabase()});
  document.addEventListener("click",e=>{const button=e.target.closest("[data-profile]");if(button)showProfile(button.dataset.profile)});
  $("modal-close").addEventListener("click",()=>$("profile-modal").classList.add("hidden"));
  $("profile-modal").addEventListener("click",e=>{if(e.target===$("profile-modal"))$("profile-modal").classList.add("hidden")});
  document.addEventListener("keydown",e=>{if(e.key==="Escape")$("profile-modal").classList.add("hidden")});
  allCities.forEach(city=>{const option=document.createElement("option");option.value=city;option.textContent=city;$("location-input").appendChild(option)});
  $("metric-profiles").textContent=candidates.length.toLocaleString();
  $("metric-skills").textContent=allSkills.length;
  $("metric-locations").textContent=allCities.length;
  runSearch();
})();