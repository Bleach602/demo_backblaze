/* ═══════════════════════════════════════════════
   Pekekue Cloud — Frontend logic
   ═══════════════════════════════════════════════ */

// ─── Navigation entre onglets ───
function switchTab(name) {
    document.querySelectorAll('.tab').forEach(t => {
        t.classList.toggle('active', t.dataset.tab === name);
    });
    document.querySelectorAll('.tab-panel').forEach(p => {
        p.classList.toggle('active', p.id === `tab-${name}`);
    });
}

document.querySelectorAll('.tab').forEach(btn => {
    btn.addEventListener('click', () => switchTab(btn.dataset.tab));
});

// ─── Dropzone & sélection de fichier ───
const dropzone = document.getElementById('dropzone');
const fileInput = document.getElementById('fileInput');
const preview = document.getElementById('preview');
const previewContent = document.getElementById('previewContent');
const fileNameEl = document.getElementById('fileName');
const fileSizeEl = document.getElementById('fileSize');
const submitBtn = document.getElementById('submitBtn');
const clearFileBtn = document.getElementById('clearFile');

let selectedFile = null;

function formatSize(bytes) {
    if (bytes < 1024) return bytes + ' o';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' Ko';
    if (bytes < 1073741824) return (bytes / 1048576).toFixed(1) + ' Mo';
    return (bytes / 1073741824).toFixed(2) + ' Go';
}

function detectType(file) {
    if (file.type.startsWith('image/')) return 'image';
    if (file.type.startsWith('video/')) return 'video';
    if (file.type === 'application/pdf') return 'pdf';
    return 'other';
}

function showPreview(file) {
    selectedFile = file;
    fileNameEl.textContent = file.name;
    fileSizeEl.textContent = formatSize(file.size);

    const type = detectType(file);
    previewContent.innerHTML = '';

    if (type === 'image') {
        const url = URL.createObjectURL(file);
        const img = document.createElement('img');
        img.src = url;
        previewContent.appendChild(img);
    } else if (type === 'video') {
        const url = URL.createObjectURL(file);
        const vid = document.createElement('video');
        vid.src = url;
        vid.muted = true;
        previewContent.appendChild(vid);
    } else if (type === 'pdf') {
        previewContent.textContent = '📄';
    } else {
        previewContent.textContent = '📦';
    }

    preview.classList.remove('hidden');
    submitBtn.disabled = false;
}

function clearFile() {
    selectedFile = null;
    fileInput.value = '';
    preview.classList.add('hidden');
    submitBtn.disabled = true;
    document.getElementById('progressBar').classList.add('hidden');
}

dropzone.addEventListener('click', () => fileInput.click());

fileInput.addEventListener('change', e => {
    if (e.target.files[0]) showPreview(e.target.files[0]);
});

['dragenter', 'dragover'].forEach(evt => {
    dropzone.addEventListener(evt, e => {
        e.preventDefault();
        dropzone.classList.add('dragover');
    });
});
['dragleave', 'drop'].forEach(evt => {
    dropzone.addEventListener(evt, e => {
        e.preventDefault();
        dropzone.classList.remove('dragover');
    });
});
dropzone.addEventListener('drop', e => {
    const file = e.dataTransfer.files[0];
    if (file) {
        fileInput.files = e.dataTransfer.files;
        showPreview(file);
    }
});

clearFileBtn.addEventListener('click', clearFile);
document.getElementById('resetBtn').addEventListener('click', clearFile);

// ─── Upload via fetch (AJAX avec progression) ───
const uploadForm = document.getElementById('uploadForm');
const progressBar = document.getElementById('progressBar');
const progressFill = document.getElementById('progressFill');
const progressText = document.getElementById('progressText');
const uploadMessage = document.getElementById('uploadMessage');

uploadForm.addEventListener('submit', e => {
    e.preventDefault();
    if (!selectedFile) return;

    const formData = new FormData();
    formData.append('file', selectedFile);
    formData.append('folder', document.getElementById('folder').value);

    const xhr = new XMLHttpRequest();
    xhr.open('POST', uploadForm.action);

    progressBar.classList.remove('hidden');
    uploadMessage.classList.add('hidden');
    submitBtn.disabled = true;

    xhr.upload.addEventListener('progress', evt => {
        if (evt.lengthComputable) {
            const percent = Math.round((evt.loaded / evt.total) * 100);
            progressFill.style.width = percent + '%';
            progressText.textContent = percent + ' %';
        }
    });

    xhr.addEventListener('load', () => {
        if (xhr.status >= 200 && xhr.status < 300) {
            uploadMessage.className = 'message success';
            uploadMessage.textContent = '✅ Fichier envoyé avec succès !';
            uploadMessage.classList.remove('hidden');
            clearFile();
            loadLibrary(); // rafraîchit la bibliothèque
        } else {
            uploadMessage.className = 'message error';
            uploadMessage.textContent = '❌ Erreur lors de l\'envoi (' + xhr.status + ')';
            uploadMessage.classList.remove('hidden');
            submitBtn.disabled = false;
        }
    });

    xhr.addEventListener('error', () => {
        uploadMessage.className = 'message error';
        uploadMessage.textContent = '❌ Erreur réseau';
        uploadMessage.classList.remove('hidden');
        submitBtn.disabled = false;
    });

    xhr.send(formData);
});

// ─── Bibliothèque : chargement & rendu ───
let allFiles = [];
let currentFilter = 'all';
let currentSearch = '';

async function loadLibrary() {
    try {
        const res = await fetch('/api/files/list');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        allFiles = await res.json();
        renderLibrary();
    } catch (err) {
        console.error('Erreur chargement bibliothèque :', err);
        document.getElementById('fileGrid').innerHTML = `
            <div class="empty-state">
                <span>⚠️</span>
                <p>Impossible de charger les fichiers</p>
            </div>`;
    }
}

function renderLibrary() {
    const grid = document.getElementById('fileGrid');
    let files = allFiles;

    // Filtre par dossier
    if (currentFilter !== 'all') {
        files = files.filter(f => f.key.startsWith(currentFilter + '/'));
    }
    // Recherche
    if (currentSearch) {
        const q = currentSearch.toLowerCase();
        files = files.filter(f => f.name.toLowerCase().includes(q));
    }

    if (files.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <span>📭</span>
                <p>Aucun fichier</p>
            </div>`;
        return;
    }

    grid.innerHTML = files.map(f => {
        const type = detectTypeFromKey(f.key);
        let thumb;
        if (type === 'image') {
            thumb = `<img src="/files/view?key=${encodeURIComponent(f.key)}" alt="">`;
        } else if (type === 'video') {
            thumb = `<video src="/files/view?key=${encodeURIComponent(f.key)}" muted preload="metadata"></video>`;
        } else if (type === 'pdf') {
            thumb = '📄';
        } else {
            thumb = '📦';
        }

        return `
            <div class="file-card">
                <div class="file-thumb" onclick="openViewer('${f.key}')">${thumb}</div>
                <div class="file-meta">
                    <div class="file-name" title="${f.name}">${f.name}</div>
                    <div class="file-size">${formatSize(f.size)}</div>
                </div>
                <div class="file-actions">
                    <a href="/files/view?key=${encodeURIComponent(f.key)}" target="_blank">Voir</a>
                    <a href="/files/download?key=${encodeURIComponent(f.key)}">Télécharger</a>
                    <button onclick="deleteFile('${f.key}')">Suppr.</button>
                </div>
            </div>`;
    }).join('');
}

function detectTypeFromKey(key) {
    const ext = key.split('.').pop().toLowerCase();
    if (['jpg','jpeg','png','gif','webp','svg'].includes(ext)) return 'image';
    if (['mp4','webm','ogg','mov','avi'].includes(ext)) return 'video';
    if (ext === 'pdf') return 'pdf';
    return 'other';
}

// ─── Filtres & recherche ───
document.querySelectorAll('.filter').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.filter').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentFilter = btn.dataset.filter;
        renderLibrary();
    });
});

document.getElementById('searchInput').addEventListener('input', e => {
    currentSearch = e.target.value;
    renderLibrary();
});

// ─── Modal de visualisation ───
function openViewer(key) {
    const modal = document.getElementById('viewerModal');
    const body = document.getElementById('viewerBody');
    const url = `/files/view?key=${encodeURIComponent(key)}`;
    const type = detectTypeFromKey(key);

    body.innerHTML = '';
    if (type === 'image') {
        body.innerHTML = `<img src="${url}" alt="">`;
    } else if (type === 'video') {
        body.innerHTML = `<video src="${url}" controls autoplay></video>`;
    } else {
        body.innerHTML = `<iframe src="${url}" width="800" height="600"></iframe>`;
    }
    modal.classList.remove('hidden');
}

function closeViewer() {
    document.getElementById('viewerModal').classList.add('hidden');
    document.getElementById('viewerBody').innerHTML = '';
}

document.addEventListener('keydown', e => {
    if (e.key === 'Escape') closeViewer();
});

// ─── Suppression ───
async function deleteFile(key) {
    if (!confirm('Supprimer ce fichier ?')) return;
    try {
        const res = await fetch(`/api/files?key=${encodeURIComponent(key)}`, { method: 'DELETE' });
        if (res.ok) loadLibrary();
        else alert('Erreur suppression');
    } catch {
        alert('Erreur réseau');
    }
}

// ─── Initialisation ───
loadLibrary();