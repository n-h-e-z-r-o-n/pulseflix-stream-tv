globalThis.chrome = globalThis.browser;

import Options from '../../../store/options.js';
import { REVIEW_PAGE_URL, BECOME_A_CONTRIBUTOR_PAGE_URL } from '../../../utils/urls.js';
import callForReviewImage from '../assets/call-for-review.js';
import { msg } from '../../../npm/hybrids/src/localize.js';
import store from '../../../npm/hybrids/src/store.js';

const NOTIFICATIONS = {
  terms: {
    icon: "triangle",
    type: "danger",
    text: msg`Due to browser restrictions and additional permissions missing, Ghostery is not able to protect you.`,
    url: "https://www.ghostery.com/support?utm_source=gbe&utm_campaign=permissions",
    action: msg`Get help`
  },
  opera: {
    text: msg`Expand Ghostery ad blocking to search engines in a few easy steps.`,
    action: msg`Enable Ad Blocking Now`
  },
  edgeMobile: {
    text: msg`Android and iPhone just got a new Edge. Ghostery included.`,
    action: msg`Scan and take Ghostery from desktop to mobile`
  },
  review: {
    img: callForReviewImage,
    type: "review",
    text: msg`We're so glad Ghostery has your heart! Help others find us too - it only takes a moment.`,
    url: REVIEW_PAGE_URL,
    action: msg`Leave a review today`
  },
  contribution: {
    icon: "heart",
    type: "",
    text: msg`Hey, do you enjoy Ghostery and want to support our work?`,
    url: `${BECOME_A_CONTRIBUTOR_PAGE_URL}?utm_source=gbe&utm_campaign=panel-becomeacontributor`,
    action: msg`Become a Contributor`
  }
};
const randomize = Math.random();
const Notification = {
  icon: "",
  img: "",
  type: "",
  text: "",
  url: "",
  action: "",
  [store.connect]: async () => {
    const { terms, panel } = await store.resolve(Options);
    if (!terms) return NOTIFICATIONS.terms;
    if (!panel.notifications) return null;
    if (randomize < 0.5) {
      return NOTIFICATIONS.review;
    }
    return NOTIFICATIONS.contribution;
  }
};

export { Notification as default };
