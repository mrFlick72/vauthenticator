"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.isJSDOM = exports.isFirefox = exports.default = void 0;
const userAgent = typeof navigator !== 'undefined' ? navigator.userAgent.toLowerCase() : 'empty';
const isFirefox = exports.isFirefox = userAgent.includes('firefox');
const isJSDOM = exports.isJSDOM = typeof window !== 'undefined' && /jsdom|HappyDOM/.test(window.navigator.userAgent);
var _default = exports.default = {
  isFirefox,
  isJSDOM
};