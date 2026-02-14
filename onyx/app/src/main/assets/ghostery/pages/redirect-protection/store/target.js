globalThis.chrome = globalThis.browser;

import { parse } from '../../../npm/tldts-experimental/dist/es6/index.js';
import Tracker from '../../../store/tracker.js';
import { getTrackerByURL } from '../../../utils/trackerdb.js';
import store from '../../../npm/hybrids/src/store.js';

const Target = {
  url: "",
  hostname: "",
  tracker: Tracker,
  [store.connect]: async () => {
    let url = "";
    {
      const params = new URLSearchParams(window.location.search);
      const encodedUrl = params.get("url");
      try {
        url = atob(encodedUrl);
      } catch (e) {
        console.error("[redirect-protection] Failed to decode URL:", e);
      }
    }
    return {
      url,
      hostname: url && parse(url).hostname,
      tracker: url && await getTrackerByURL(url)
    };
  }
};

export { Target as default };
