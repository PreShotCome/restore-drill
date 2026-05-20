const Anthropic = require('@anthropic-ai/sdk');

let client = null;

function setApiKey(key) {
  const trimmed = (key || '').trim();
  client = trimmed ? new Anthropic({ apiKey: trimmed }) : null;
}

function hasApiKey() {
  return !!client;
}

function getClient() {
  return client;
}

if (process.env.ANTHROPIC_API_KEY) setApiKey(process.env.ANTHROPIC_API_KEY);

module.exports = { setApiKey, hasApiKey, getClient };
