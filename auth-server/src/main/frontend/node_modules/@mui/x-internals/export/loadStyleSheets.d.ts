/**
 * Loads all stylesheets from the given root element into the document.
 * @returns an array of promises that resolve when each stylesheet is loaded
 * @param document Document to load stylesheets into
 * @param root Document or ShadowRoot to load stylesheets from
 * @param nonce Optional nonce to set on style elements for CSP compliance
 */
export declare function loadStyleSheets(document: Document, root: Document | ShadowRoot, nonce?: string): Promise<void>[];