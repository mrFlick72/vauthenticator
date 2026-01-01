"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useStore = useStore;
var _withSelector = require("use-sync-external-store/shim/with-selector");
/* We need to import the shim because React 17 does not support the `useSyncExternalStore` API.
 * More info: https://github.com/mui/mui-x/issues/18303#issuecomment-2958392341 */

function useStore(store, selector, a1, a2, a3) {
  const selectorWithArgs = state => selector(state, a1, a2, a3);
  return (0, _withSelector.useSyncExternalStoreWithSelector)(store.subscribe, store.getSnapshot, store.getSnapshot, selectorWithArgs);
}