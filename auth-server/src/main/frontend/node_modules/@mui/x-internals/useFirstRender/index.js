"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var _useFirstRender = require("./useFirstRender");
Object.keys(_useFirstRender).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (key in exports && exports[key] === _useFirstRender[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _useFirstRender[key];
    }
  });
});