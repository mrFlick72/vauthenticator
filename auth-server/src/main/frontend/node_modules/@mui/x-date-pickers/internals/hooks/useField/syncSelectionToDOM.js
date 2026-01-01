"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.syncSelectionToDOM = syncSelectionToDOM;
var _ownerDocument = _interopRequireDefault(require("@mui/utils/ownerDocument"));
var _utils = require("../../utils/utils");
function syncSelectionToDOM(parameters) {
  const {
    focused,
    domGetters,
    stateResponse: {
      // States and derived states
      parsedSelectedSections,
      state
    }
  } = parameters;
  if (!domGetters.isReady()) {
    return;
  }
  const selection = (0, _ownerDocument.default)(domGetters.getRoot()).getSelection();
  if (!selection) {
    return;
  }
  if (parsedSelectedSections == null) {
    // If the selection contains an element inside the field, we reset it.
    if (selection.rangeCount > 0 &&
    // Firefox can return a Restricted object here
    selection.getRangeAt(0).startContainer instanceof Node && domGetters.getRoot().contains(selection.getRangeAt(0).startContainer)) {
      selection.removeAllRanges();
    }
    if (focused) {
      domGetters.getRoot().blur();
    }
    return;
  }

  // On multi input range pickers we want to update selection range only for the active input
  if (!domGetters.getRoot().contains((0, _utils.getActiveElement)(domGetters.getRoot()))) {
    return;
  }
  const range = new window.Range();
  let target;
  if (parsedSelectedSections === 'all') {
    target = domGetters.getRoot();
  } else {
    const section = state.sections[parsedSelectedSections];
    if (section.type === 'empty') {
      target = domGetters.getSectionContainer(parsedSelectedSections);
    } else {
      target = domGetters.getSectionContent(parsedSelectedSections);
    }
  }
  range.selectNodeContents(target);
  target.focus();
  selection.removeAllRanges();
  selection.addRange(range);
}