globalThis.chrome = globalThis.browser;

import { getDefaultExportFromCjs } from './_commonjsHelpers.js';
import { __require as requireLottie } from '../npm/lottie-web/build/player/lottie.js';

var lottieExports = /*@__PURE__*/ requireLottie();
const lottie = /*@__PURE__*/getDefaultExportFromCjs(lottieExports);

export { lottie as default };
