globalThis.chrome = globalThis.browser;

import { debugMode, stagingMode } from './debug.js';

const GHOSTERY_DOMAIN = debugMode ? "ghosterystage.com" : "ghostery.com";
const TERMS_AND_CONDITIONS_URL = `https://www.${GHOSTERY_DOMAIN}/privacy/ghostery-terms-and-conditions?utm_source=gbe&utm_campaign=onboarding`;
const HOME_PAGE_URL = `https://www.${GHOSTERY_DOMAIN}/`;
const WTM_PAGE_URL = `https://www.${GHOSTERY_DOMAIN}/whotracksme`;
const SUPPORT_PAGE_URL = `https://www.${GHOSTERY_DOMAIN}/support`;
const WHATS_NEW_PAGE_URL = `https://www.${GHOSTERY_DOMAIN}/blog/ghostery-extension-v10-5?embed=1&utm_campaign=whatsnew`;
const REVIEW_PAGE_URL = (() => {
  return "https://mygho.st/ReviewFirefoxPanel";
})();
const BECOME_A_CONTRIBUTOR_PAGE_URL = "https://www.ghostery.com/become-a-contributor";
const CDN_URL = stagingMode ? "https://staging-cdn.ghostery.com/" : "https://cdn.ghostery.com/";
const PAUSE_ASSISTANT_LEARN_MORE_URL = `https://www.${GHOSTERY_DOMAIN}/blog/browsing-assistant-user-agent`;
const TRACKERS_PREVIEW_LEARN_MORE_URL = `https://www.${GHOSTERY_DOMAIN}/blog/introducing-wtm-serp-report`;
const ZAP_AUTORELOAD_DISABLED_HOSTNAMES = ["youtube.com"];

export { BECOME_A_CONTRIBUTOR_PAGE_URL, CDN_URL, GHOSTERY_DOMAIN, HOME_PAGE_URL, PAUSE_ASSISTANT_LEARN_MORE_URL, REVIEW_PAGE_URL, SUPPORT_PAGE_URL, TERMS_AND_CONDITIONS_URL, TRACKERS_PREVIEW_LEARN_MORE_URL, WHATS_NEW_PAGE_URL, WTM_PAGE_URL, ZAP_AUTORELOAD_DISABLED_HOSTNAMES };
