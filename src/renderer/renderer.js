const els = {
  apiKey: document.getElementById('apiKey'),
  saveKey: document.getElementById('saveKey'),
  keyStatus: document.getElementById('keyStatus'),
  status: document.getElementById('status'),
};

function setStatus(text, isError) {
  els.status.textContent = text;
  els.status.classList.toggle('error', !!isError);
}

function showKeyStatus(hasKey) {
  els.keyStatus.textContent = hasKey ? 'Key saved.' : 'No key set.';
  els.keyStatus.classList.toggle('ok', hasKey);
  els.keyStatus.classList.toggle('missing', !hasKey);
}

async function refreshKeyStatus() {
  try {
    const status = await window.api.getStatus();
    showKeyStatus(status.hasKey);
  } catch {
    showKeyStatus(false);
  }
}

async function saveKey() {
  const res = await window.api.setApiKey(els.apiKey.value.trim());
  if (res.ok) {
    showKeyStatus(res.hasKey);
    setStatus('API key saved.');
  } else {
    setStatus(`Could not save key: ${res.error}`, true);
  }
}

els.saveKey.addEventListener('click', saveKey);

refreshKeyStatus();
