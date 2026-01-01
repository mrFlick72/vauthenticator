"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var _throttle = require("./throttle");
Object.keys(_throttle).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (key in exports && exports[key] === _throttle[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _throttle[key];
    }
  });
});