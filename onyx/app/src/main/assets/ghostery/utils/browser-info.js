globalThis.chrome = globalThis.browser;

import Bowser from '../npm/bowser/src/bowser.js';

let ua;
function getUA() {
  if (ua) return ua;
  ua = Bowser.parse(navigator.userAgent);
  return ua;
}
function getBrowser() {
  {
    return { name: "firefox", token: "ff" };
  }
}
function isBrave() {
  return getBrowser().name === "brave";
}
function isFirefox() {
  return getBrowser().name === "firefox";
}
function isWebkit() {
  return false;
}
function getOS() {
  const platform = getUA().os?.name?.toLowerCase() || "";
  if (platform.includes("mac")) {
    return "mac";
  } else if (platform.includes("win")) {
    return "win";
  } else if (platform.includes("android")) {
    return "android";
  } else if (platform.includes("ios")) {
    return "ios";
  } else if (platform.includes("chromium os")) {
    return "cros";
  } else if (platform.includes("bsd")) {
    return "openbsd";
  } else if (platform.includes("linux")) {
    return "linux";
  }
  return "other";
}
let browserInfo = null;
async function getBrowserInfo() {
  if (browserInfo) return browserInfo;
  const ua2 = getUA();
  const { name, token } = getBrowser();
  browserInfo = {
    name,
    token,
    version: parseInt(ua2.browser.version, 10),
    os: getOS(),
    osVersion: ua2.os.version || ""
  };
  return browserInfo;
}

export { getBrowserInfo as default, getBrowser, getOS, isBrave, isFirefox, isWebkit };
