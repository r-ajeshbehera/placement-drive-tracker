// Frontend for the Placement Drive Tracker.
// Talks to the Java backend through the REST API (fetch + JSON).

let students = [];
let drives = [];

// ---------- small helpers ----------

async function api(path, form) {
  const options = form
    ? { method: "POST", body: new URLSearchParams(form) }
    : {};
  const res = await fetch(path, options);
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.error || "Something went wrong");
  }
  return data;
}

function toast(message, isError) {
  const t = document.getElementById("toast");
  t.textContent = message;
  t.className = "toast" + (isError ? " error" : "");
  t.hidden = false;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => (t.hidden = true), 3000);
}

// builds a table cell safely (textContent, so names can't inject HTML)
function cell(text, className) {
  const td = document.createElement("td");
  td.textContent = text;
  if (className) td.className = className;
  return td;
}

function button(label, className, onClick) {
  const b = document.createElement("button");
  b.type = "button";
  b.textContent = label;
  b.className = "small " + (className || "");
  b.addEventListener("click", onClick);
  return b;
}

function emptyRow(tbody, cols, text) {
  const tr = document.createElement("tr");
  const td = cell(text, "empty");
  td.colSpan = cols;
  tr.appendChild(td);
  tbody.appendChild(tr);
}

function formatDate(iso) {
  const d = new Date(iso + "T00:00:00");
  return d.toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" });
}

function eligibleCount(drive) {
  return students.filter((s) => s.cgpa >= drive.minCgpa).length;
}

// ---------- tabs ----------

document.querySelectorAll(".tabs button").forEach((btn) => {
  btn.addEventListener("click", () => showTab(btn.dataset.tab));
});

function showTab(name) {
  document.querySelectorAll(".tabs button").forEach((b) => b.classList.toggle("active", b.dataset.tab === name));
  document.querySelectorAll(".tab-panel").forEach((p) => (p.hidden = p.id !== name));
}

// ---------- load everything ----------

async function loadAll() {
  try {
    [students, drives] = await Promise.all([api("/api/students"), api("/api/drives")]);
    const stats = await api("/api/stats");
    document.getElementById("sStudents").textContent = stats.students;
    document.getElementById("sDrives").textContent = stats.drives;
    document.getElementById("sUpcoming").textContent = stats.upcoming;
    document.getElementById("sAvg").textContent = Number(stats.avgCgpa).toFixed(2);
    renderUpcoming();
    renderStudents();
    renderDrives(drives);
    fillDriveSelect();
  } catch (e) {
    toast(e.message, true);
  }
}

// ---------- dashboard ----------

function renderUpcoming() {
  const tbody = document.getElementById("upcomingBody");
  tbody.innerHTML = "";
  const now = new Date();
  const today = now.getFullYear() + "-" + String(now.getMonth() + 1).padStart(2, "0") + "-" + String(now.getDate()).padStart(2, "0");
  const upcoming = drives.filter((d) => d.date >= today);
  if (upcoming.length === 0) return emptyRow(tbody, 6, "No upcoming drives. Add one in the Drives tab.");
  upcoming.forEach((d) => {
    const tr = document.createElement("tr");
    tr.append(cell(formatDate(d.date)), cell(d.company), cell(d.role), cell(d.minCgpa.toFixed(1), "num"));
    const pillCell = document.createElement("td");
    const pill = document.createElement("span");
    pill.className = "pill ok";
    pill.textContent = eligibleCount(d) + " of " + students.length;
    pillCell.appendChild(pill);
    const act = document.createElement("td");
    act.appendChild(button("View eligible", "", () => openEligible(d.id)));
    tr.append(pillCell, act);
    tbody.appendChild(tr);
  });
}

// ---------- students ----------

function renderStudents() {
  const tbody = document.getElementById("studentBody");
  const filter = document.getElementById("studentFilter").value.trim().toLowerCase();
  tbody.innerHTML = "";
  const list = students.filter(
    (s) => s.name.toLowerCase().includes(filter) || s.branch.toLowerCase().includes(filter)
  );
  if (list.length === 0) return emptyRow(tbody, 5, "No students found.");
  list.forEach((s) => {
    const tr = document.createElement("tr");
    const cgpaCell = cell(s.cgpa.toFixed(2), "num");
    const act = document.createElement("td");
    const box = document.createElement("div");
    box.className = "actions";
    box.append(
      button("Edit CGPA", "", () => editCgpa(s, cgpaCell)),
      button("Delete", "danger", () => deleteStudent(s))
    );
    act.appendChild(box);
    tr.append(cell(s.id), cell(s.name), cell(s.branch), cgpaCell, act);
    tbody.appendChild(tr);
  });
}

function editCgpa(student, td) {
  td.textContent = "";
  const input = document.createElement("input");
  input.type = "number";
  input.step = "0.01";
  input.min = "0";
  input.max = "10";
  input.value = student.cgpa;
  input.style.width = "90px";
  input.setAttribute("aria-label", "New CGPA for " + student.name);
  const save = button("Save", "primary", async () => {
    try {
      const r = await api("/api/students/update", { id: student.id, cgpa: input.value });
      toast(r.message);
      loadAll();
    } catch (e) {
      toast(e.message, true);
    }
  });
  td.append(input, " ", save);
  input.focus();
}

async function deleteStudent(s) {
  if (!confirm("Delete " + s.name + "?")) return;
  try {
    const r = await api("/api/students/delete", { id: s.id });
    toast(r.message);
    loadAll();
  } catch (e) {
    toast(e.message, true);
  }
}

document.getElementById("studentFilter").addEventListener("input", renderStudents);

document.getElementById("studentForm").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  try {
    const r = await api("/api/students", new FormData(ev.target));
    toast(r.message);
    ev.target.reset();
    document.getElementById("stBranch").value = "MCA";
    loadAll();
  } catch (e) {
    toast(e.message, true);
  }
});

// ---------- drives ----------

function renderDrives(list) {
  const tbody = document.getElementById("driveBody");
  tbody.innerHTML = "";
  if (list.length === 0) return emptyRow(tbody, 6, "No drives found.");
  list.forEach((d) => {
    const tr = document.createElement("tr");
    const act = document.createElement("td");
    const box = document.createElement("div");
    box.className = "actions";
    box.append(
      button("Eligible", "", () => openEligible(d.id)),
      button("Delete", "danger", () => deleteDrive(d))
    );
    act.appendChild(box);
    tr.append(cell(d.id), cell(formatDate(d.date)), cell(d.company), cell(d.role), cell(d.minCgpa.toFixed(1), "num"), act);
    tbody.appendChild(tr);
  });
}

async function deleteDrive(d) {
  if (!confirm("Delete the " + d.company + " drive?")) return;
  try {
    const r = await api("/api/drives/delete", { id: d.id });
    toast(r.message);
    loadAll();
  } catch (e) {
    toast(e.message, true);
  }
}

document.getElementById("searchForm").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const q = document.getElementById("driveSearch").value.trim();
  try {
    renderDrives(await api("/api/drives?q=" + encodeURIComponent(q)));
  } catch (e) {
    toast(e.message, true);
  }
});

document.getElementById("driveForm").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  try {
    const r = await api("/api/drives", new FormData(ev.target));
    toast(r.message);
    ev.target.reset();
    loadAll();
  } catch (e) {
    toast(e.message, true);
  }
});

// ---------- eligibility ----------

function fillDriveSelect() {
  const sel = document.getElementById("eligDrive");
  const keep = sel.value;
  sel.innerHTML = "";
  drives.forEach((d) => {
    const o = document.createElement("option");
    o.value = d.id;
    o.textContent = d.company + " – " + d.role + " (min " + d.minCgpa.toFixed(1) + ", " + formatDate(d.date) + ")";
    sel.appendChild(o);
  });
  if (keep) sel.value = keep;
}

function openEligible(driveId) {
  showTab("eligible");
  document.getElementById("eligDrive").value = driveId;
  showEligible();
}

async function showEligible() {
  const id = document.getElementById("eligDrive").value;
  const box = document.getElementById("eligResult");
  if (!id) {
    box.textContent = "Add a drive first.";
    return;
  }
  try {
    const data = await api("/api/eligible?driveId=" + id);
    box.innerHTML = "";
    const head = document.createElement("div");
    head.className = "result-head";
    const b = document.createElement("b");
    b.textContent = data.students.length + " student(s) eligible";
    head.append(b, " for " + data.drive.company + " (" + data.drive.role + "), minimum CGPA " + data.drive.minCgpa.toFixed(1) + ".");
    box.appendChild(head);

    const wrap = document.createElement("div");
    wrap.className = "table-wrap";
    const table = document.createElement("table");
    table.innerHTML = "<thead><tr><th>Rank</th><th>Name</th><th>Branch</th><th>CGPA</th></tr></thead>";
    const tbody = document.createElement("tbody");
    if (data.students.length === 0) emptyRow(tbody, 4, "Nobody meets the minimum CGPA for this drive.");
    data.students.forEach((s, i) => {
      const tr = document.createElement("tr");
      tr.append(cell(i + 1), cell(s.name), cell(s.branch), cell(s.cgpa.toFixed(2), "num"));
      tbody.appendChild(tr);
    });
    table.appendChild(tbody);
    wrap.appendChild(table);
    box.appendChild(wrap);
  } catch (e) {
    toast(e.message, true);
  }
}

document.getElementById("eligBtn").addEventListener("click", showEligible);

loadAll();
