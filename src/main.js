const {
  app,
  BrowserWindow,
  ipcMain,
  Notification,
  safeStorage,
} = require('electron');
const path = require('path');
const fs = require('fs');
const { setApiKey, hasApiKey } = require('./anthropic');

let mainWindow;
let configPath;

function loadStoredKey() {
  try {
    const cfg = JSON.parse(fs.readFileSync(configPath, 'utf8'));
    if (cfg.apiKeyEnc && safeStorage.isEncryptionAvailable()) {
      return safeStorage.decryptString(Buffer.from(cfg.apiKeyEnc, 'base64'));
    }
    return cfg.apiKey || '';
  } catch {
    return '';
  }
}

function storeKey(key) {
  let cfg;
  if (safeStorage.isEncryptionAvailable()) {
    cfg = { apiKeyEnc: safeStorage.encryptString(key).toString('base64') };
  } else {
    cfg = { apiKey: key };
  }
  fs.writeFileSync(configPath, JSON.stringify(cfg, null, 2));
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 960,
    height: 720,
    backgroundColor: '#0e1116',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
  });
  mainWindow.loadFile(path.join(__dirname, 'renderer', 'index.html'));
}

app.whenReady().then(() => {
  configPath = path.join(app.getPath('userData'), 'config.json');
  const storedKey = loadStoredKey();
  if (storedKey) setApiKey(storedKey);

  ipcMain.handle('get-status', () => ({ hasKey: hasApiKey() }));

  ipcMain.handle('set-api-key', (_event, key) => {
    setApiKey(key);
    try {
      storeKey(key);
    } catch (err) {
      return { ok: false, error: err.message };
    }
    return { ok: true, hasKey: hasApiKey() };
  });

  ipcMain.handle('notify', (_event, { title, body }) => {
    new Notification({ title, body }).show();
  });

  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});
