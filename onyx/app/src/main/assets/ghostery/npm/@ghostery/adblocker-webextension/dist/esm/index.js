globalThis.chrome = globalThis.browser;

import '../../../../tldts-experimental/npm/tldts-core/dist/es6/src/options.js';
import '../../../adblocker/dist/esm/data-view.js';
export { adsAndTrackingLists, adsLists, fetchLists, fetchResources, fetchWithRetry, fullLists } from '../../../adblocker/dist/esm/fetch.js';
export { default as CosmeticFilter } from '../../../adblocker/dist/esm/filters/cosmetic.js';
export { FilterType, detectFilterType, getLinesWithFilters, mergeDiffs, parseFilters } from '../../../adblocker/dist/esm/lists.js';
export { default as Request, getHostnameHashesFromLabelsBackward } from '../../../adblocker/dist/esm/request.js';
import '../../../../@remusao/small/dist/esm/index.js';
export { default as NetworkFilter } from '../../../adblocker/dist/esm/filters/network.js';
export { default as Preprocessor } from '../../../adblocker/dist/esm/preprocessor.js';
import { isUTF8 } from '../../../adblocker/dist/esm/encoding.js';
import StreamingHtmlFilter from '../../../adblocker/dist/esm/html-filtering.js';

/*!
 * Copyright (c) 2017-present Ghostery GmbH. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
function isFirefox() {
    try {
        return navigator.userAgent.indexOf('Firefox') !== -1;
    }
    catch (e) {
        return false;
    }
}
/**
 * There are different ways to inject scriptlets ("push" vs "pull").
 * This function should decide based on the environment what to use:
 *
 * 1) "Pushing" means the adblocker will listen on "onCommitted" events
 *    and then execute scripts by running the tabs.executeScript API.
 * 2) "Pulling" means the adblocker will inject a content script, which
 *    runs before the page loads (and on the DOM changes), fetches
 *    scriplets from the background and runs them.
 *
 * Note:
 * - the "push" model requires permission to the webNavigation API.
 *   If that is not available, the implementation will fall back to the
 *   "pull" model, which does not have this requirement.
 */
function usePushScriptsInjection() {
    // There is no fundamental reason why it should not work on Firefox,
    // but given that there are no known issues with Firefox, let's keep
    // the old, proven technique until there is evidence that changes
    // are needed.
    //
    // Take YouTube as an example: on Chrome (or forks like Edge), the adblocker
    // will sometimes fail to block ads if you reload the page multiple times;
    // on Firefox, the same steps do not seem to trigger any ads.
    return !isFirefox();
}
usePushScriptsInjection();
/**
 * Helper used when injecting custom CSP headers to update `responseHeaders`.
 */
function updateResponseHeadersWithCSP(details, policies) {
    if (policies === undefined) {
        return {};
    }
    let responseHeaders = details.responseHeaders || [];
    const CSP_HEADER_NAME = 'content-security-policy';
    // Collect existing CSP headers from response
    responseHeaders.forEach(({ name, value }) => {
        if (name.toLowerCase() === CSP_HEADER_NAME) {
            policies += `; ${value}`;
        }
    });
    // Remove all CSP headers from response
    responseHeaders = responseHeaders.filter(({ name }) => name.toLowerCase() !== CSP_HEADER_NAME);
    // Add updated CSP header
    responseHeaders.push({ name: CSP_HEADER_NAME, value: policies });
    return { responseHeaders };
}
const HTML_FILTERABLE_REQUEST_TYPES = new Set([
    'main_frame',
    'mainFrame',
    'sub_frame',
    'subFrame',
    'stylesheet',
    'script',
    'document',
    'fetch',
    'prefetch',
    'preflight',
    'xhr',
    'xmlhttprequest',
]);
// html filters are applied to text/* and those additional mime types
const HTML_FILTERABLE_NON_TEXT_MIME_TYPES = new Set([
    'application/javascript',
    'application/json',
    'application/mpegurl',
    'application/vnd.api+json',
    'application/vnd.apple.mpegurl',
    'application/vnd.apple.mpegurl.audio',
    'application/x-javascript',
    'application/x-mpegurl',
    'application/xhtml+xml',
    'application/xml',
    'audio/mpegurl',
    'audio/x-mpegurl',
]);
const MAXIMUM_RESPONSE_BUFFER_SIZE = 10 * 1024 * 1024;
function getHeaderFromDetails(details, headerName) {
    var _a, _b;
    return (_b = (_a = details.responseHeaders) === null || _a === void 0 ? void 0 : _a.find((header) => header.name === headerName)) === null || _b === void 0 ? void 0 : _b.value;
}
// $replace filters are applied to complete response bodies
// To avoid performance problem we should ignore large request or binary data
function shouldApplyReplaceSelectors(request) {
    const details = request._originalRequestDetails;
    // In case of undefined error of xhr/fetch and any kind of network activities
    if (details.statusCode === 0) {
        return false;
    }
    if (details.statusCode < 200 || details.statusCode > 299) {
        return false;
    }
    // ignore file downloads
    const contentDisposition = (getHeaderFromDetails(details, 'content-disposition') || '').toLowerCase();
    if (contentDisposition !== '' && contentDisposition.startsWith('inline') === false) {
        return false;
    }
    const contentLength = Number(getHeaderFromDetails(details, 'content-length'));
    if (contentLength !== 0 && contentLength >= MAXIMUM_RESPONSE_BUFFER_SIZE) {
        return false;
    }
    const contentTypeHeader = (getHeaderFromDetails(details, 'content-type') || '').toLowerCase();
    if (contentTypeHeader.startsWith('text') ||
        HTML_FILTERABLE_NON_TEXT_MIME_TYPES.has(contentTypeHeader)) {
        return true;
    }
    if (HTML_FILTERABLE_REQUEST_TYPES.has(request.type)) {
        return true;
    }
    return false;
}
/**
 * Enable stream HTML filter on request `id` using `rules`.
 */
function filterRequestHTML(filterResponseData, request, rules) {
    if (shouldApplyReplaceSelectors(request) === false) {
        rules = rules.filter(([type]) => type !== 'replace');
    }
    if (rules.length === 0) {
        return;
    }
    // Create filter to observe loading of resource
    const filter = filterResponseData(request.id);
    const decoder = new TextDecoder();
    const encoder = new TextEncoder();
    const htmlFilter = new StreamingHtmlFilter(rules);
    let accumulatedBufferSize = 0;
    const teardown = (event) => {
        // Before disconnecting our streaming filter, we need to be extra careful
        // and make sure that no data remains in either our streaming `TextDecoder`
        // instance or the HTML filterer.
        //
        // In case any data remains, we write it to filter.
        try {
            const remaining = htmlFilter.write(decoder.decode()) +
                htmlFilter.flush(accumulatedBufferSize < MAXIMUM_RESPONSE_BUFFER_SIZE);
            if (remaining.length !== 0) {
                filter.write(encoder.encode(remaining));
            }
        }
        catch (ex) {
            // If we reach this point, there is probably no way we can recover...
            console.error('Failed to flush HTML filterer', ex);
        }
        // If latest event had some data attached (i.e. 'ondata' event), we make
        // sure to flush it through the filterer before disconnecting.
        if (event.data !== undefined) {
            filter.write(event.data);
        }
        // Disconnect streaming filter.
        filter.disconnect();
    };
    filter.ondata = (event) => {
        // On any chunk of data we implementa very fast UTF-8 validity check to make
        // sure that we will be able to decode it. Note that in theory it should be
        // possible that a chunk ends on the boundary of a multi-byte UTF-8 code and
        // this check would fail?
        if (isUTF8(new Uint8Array(event.data)) === false) {
            return teardown(event);
        }
        accumulatedBufferSize += event.data.byteLength;
        if (accumulatedBufferSize > MAXIMUM_RESPONSE_BUFFER_SIZE) {
            return teardown(event);
        }
        try {
            filter.write(encoder.encode(htmlFilter.write(decoder.decode(event.data, { stream: true }))));
        }
        catch (ex) {
            // If we fail to decode a chunk, we need to be extra conservative and stop
            // listening to streaming response. Teardown takes care of flushing any
            // data remaining in the pipeline and disconnecting the listener.
            return teardown(event);
        }
    };
    filter.onstop = () => {
        teardown({});
    };
}

export { MAXIMUM_RESPONSE_BUFFER_SIZE, StreamingHtmlFilter, filterRequestHTML, isUTF8, shouldApplyReplaceSelectors, updateResponseHeadersWithCSP };
