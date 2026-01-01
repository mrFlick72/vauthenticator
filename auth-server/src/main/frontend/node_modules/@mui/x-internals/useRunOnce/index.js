"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var _useRunOnce = require("./useRunOnce");
Object.keys(_useRunOnce).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (key in exports && exports[key] === _useRunOnce[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _useRunOnce[key];
    }
  });
});