"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var _useEffectAfterFirstRender = require("./useEffectAfterFirstRender");
Object.keys(_useEffectAfterFirstRender).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (key in exports && exports[key] === _useEffectAfterFirstRender[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _useEffectAfterFirstRender[key];
    }
  });
});