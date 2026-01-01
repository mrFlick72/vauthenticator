"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.useStoreEffect = useStoreEffect;
var _useLazyRef = _interopRequireDefault(require("@mui/utils/useLazyRef"));
var _useOnMount = _interopRequireDefault(require("@mui/utils/useOnMount"));
const noop = () => {};

/**
 * An Effect implementation for the Store. This should be used for side-effects only. To
 * compute and store derived state, use `createSelectorMemoized` instead.
 */
function useStoreEffect(store, selector, effect) {
  const instance = (0, _useLazyRef.default)(initialize, {
    store,
    selector
  }).current;
  instance.effect = effect;
  (0, _useOnMount.default)(instance.onMount);
}

// `useLazyRef` typings are incorrect, `params` should not be optional
function initialize(params) {
  const {
    store,
    selector
  } = params;
  let previousState = selector(store.state);
  const instance = {
    effect: noop,
    dispose: null,
    // We want a single subscription done right away and cleared on unmount only,
    // but React triggers `useOnMount` multiple times in dev, so we need to manage
    // the subscription anyway.
    subscribe: () => {
      instance.dispose ?? (instance.dispose = store.subscribe(state => {
        const nextState = selector(state);
        instance.effect(previousState, nextState);
        previousState = nextState;
      }));
    },
    onMount: () => {
      instance.subscribe();
      return () => {
        instance.dispose?.();
        instance.dispose = null;
      };
    }
  };
  instance.subscribe();
  return instance;
}