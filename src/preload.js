const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('api', {
  getStatus: () => ipcRenderer.invoke('get-status'),
  setApiKey: (key) => ipcRenderer.invoke('set-api-key', key),
  notify: (title, body) => ipcRenderer.invoke('notify', { title, body }),
});
