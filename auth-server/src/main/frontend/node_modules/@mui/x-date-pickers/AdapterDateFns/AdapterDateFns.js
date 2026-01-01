"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.AdapterDateFns = void 0;
var _addDays = require("date-fns/addDays");
var _addSeconds = require("date-fns/addSeconds");
var _addMinutes = require("date-fns/addMinutes");
var _addHours = require("date-fns/addHours");
var _addWeeks = require("date-fns/addWeeks");
var _addMonths = require("date-fns/addMonths");
var _addYears = require("date-fns/addYears");
var _endOfDay = require("date-fns/endOfDay");
var _endOfWeek = require("date-fns/endOfWeek");
var _endOfYear = require("date-fns/endOfYear");
var _format = require("date-fns/format");
var _getDate = require("date-fns/getDate");
var _getDaysInMonth = require("date-fns/getDaysInMonth");
var _getHours = require("date-fns/getHours");
var _getMinutes = require("date-fns/getMinutes");
var _getMonth = require("date-fns/getMonth");
var _getSeconds = require("date-fns/getSeconds");
var _getMilliseconds = require("date-fns/getMilliseconds");
var _getWeek = require("date-fns/getWeek");
var _getYear = require("date-fns/getYear");
var _isAfter = require("date-fns/isAfter");
var _isBefore = require("date-fns/isBefore");
var _isEqual = require("date-fns/isEqual");
var _isSameDay = require("date-fns/isSameDay");
var _isSameYear = require("date-fns/isSameYear");
var _isSameMonth = require("date-fns/isSameMonth");
var _isSameHour = require("date-fns/isSameHour");
var _isValid = require("date-fns/isValid");
var _parse = require("date-fns/parse");
var _setDate = require("date-fns/setDate");
var _setHours = require("date-fns/setHours");
var _setMinutes = require("date-fns/setMinutes");
var _setMonth = require("date-fns/setMonth");
var _setSeconds = require("date-fns/setSeconds");
var _setMilliseconds = require("date-fns/setMilliseconds");
var _setYear = require("date-fns/setYear");
var _startOfDay = require("date-fns/startOfDay");
var _startOfMonth = require("date-fns/startOfMonth");
var _endOfMonth = require("date-fns/endOfMonth");
var _startOfWeek = require("date-fns/startOfWeek");
var _startOfYear = require("date-fns/startOfYear");
var _isWithinInterval = require("date-fns/isWithinInterval");
var _enUS = require("date-fns/locale/en-US");
var _AdapterDateFnsBase = require("../AdapterDateFnsBase");
/**
 * Based on `@date-io/date-fns`
 *
 * MIT License
 *
 * Copyright (c) 2017 Dmitriy Kovalenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class AdapterDateFns extends _AdapterDateFnsBase.AdapterDateFnsBase {
  constructor({
    locale,
    formats
  } = {}) {
    /* v8 ignore start */
    if (process.env.NODE_ENV !== 'production') {
      if (typeof _addDays.addDays !== 'function') {
        throw new Error(['MUI: The `date-fns` package v2.x is not compatible with this adapter.', 'Please, install v3.x or v4.x of the package or use the `AdapterDateFnsV2` instead.'].join('\n'));
      }
      if (!_format.longFormatters) {
        throw new Error('MUI: The minimum supported `date-fns` package version compatible with this adapter is `3.2.x`.');
      }
    }
    /* v8 ignore stop */
    super({
      locale: locale ?? _enUS.enUS,
      formats,
      longFormatters: _format.longFormatters
    });
  }

  // TODO: explicit return types can be removed once there is only one date-fns version supported
  parse = (value, format) => {
    if (value === '') {
      return null;
    }
    return (0, _parse.parse)(value, format, new Date(), {
      locale: this.locale
    });
  };
  isValid = value => {
    if (value == null) {
      return false;
    }
    return (0, _isValid.isValid)(value);
  };
  format = (value, formatKey) => {
    return this.formatByString(value, this.formats[formatKey]);
  };
  formatByString = (value, formatString) => {
    return (0, _format.format)(value, formatString, {
      locale: this.locale
    });
  };
  isEqual = (value, comparing) => {
    if (value === null && comparing === null) {
      return true;
    }
    if (value === null || comparing === null) {
      return false;
    }
    return (0, _isEqual.isEqual)(value, comparing);
  };
  isSameYear = (value, comparing) => {
    return (0, _isSameYear.isSameYear)(value, comparing);
  };
  isSameMonth = (value, comparing) => {
    return (0, _isSameMonth.isSameMonth)(value, comparing);
  };
  isSameDay = (value, comparing) => {
    return (0, _isSameDay.isSameDay)(value, comparing);
  };
  isSameHour = (value, comparing) => {
    return (0, _isSameHour.isSameHour)(value, comparing);
  };
  isAfter = (value, comparing) => {
    return (0, _isAfter.isAfter)(value, comparing);
  };
  isAfterYear = (value, comparing) => {
    return (0, _isAfter.isAfter)(value, (0, _endOfYear.endOfYear)(comparing));
  };
  isAfterDay = (value, comparing) => {
    return (0, _isAfter.isAfter)(value, (0, _endOfDay.endOfDay)(comparing));
  };
  isBefore = (value, comparing) => {
    return (0, _isBefore.isBefore)(value, comparing);
  };
  isBeforeYear = (value, comparing) => {
    return (0, _isBefore.isBefore)(value, this.startOfYear(comparing));
  };
  isBeforeDay = (value, comparing) => {
    return (0, _isBefore.isBefore)(value, this.startOfDay(comparing));
  };
  isWithinRange = (value, [start, end]) => {
    return (0, _isWithinInterval.isWithinInterval)(value, {
      start,
      end
    });
  };
  startOfYear = value => {
    return (0, _startOfYear.startOfYear)(value);
  };
  startOfMonth = value => {
    return (0, _startOfMonth.startOfMonth)(value);
  };
  startOfWeek = value => {
    return (0, _startOfWeek.startOfWeek)(value, {
      locale: this.locale
    });
  };
  startOfDay = value => {
    return (0, _startOfDay.startOfDay)(value);
  };
  endOfYear = value => {
    return (0, _endOfYear.endOfYear)(value);
  };
  endOfMonth = value => {
    return (0, _endOfMonth.endOfMonth)(value);
  };
  endOfWeek = value => {
    return (0, _endOfWeek.endOfWeek)(value, {
      locale: this.locale
    });
  };
  endOfDay = value => {
    return (0, _endOfDay.endOfDay)(value);
  };
  addYears = (value, amount) => {
    return (0, _addYears.addYears)(value, amount);
  };
  addMonths = (value, amount) => {
    return (0, _addMonths.addMonths)(value, amount);
  };
  addWeeks = (value, amount) => {
    return (0, _addWeeks.addWeeks)(value, amount);
  };
  addDays = (value, amount) => {
    return (0, _addDays.addDays)(value, amount);
  };
  addHours = (value, amount) => {
    return (0, _addHours.addHours)(value, amount);
  };
  addMinutes = (value, amount) => {
    return (0, _addMinutes.addMinutes)(value, amount);
  };
  addSeconds = (value, amount) => {
    return (0, _addSeconds.addSeconds)(value, amount);
  };
  getYear = value => {
    return (0, _getYear.getYear)(value);
  };
  getMonth = value => {
    return (0, _getMonth.getMonth)(value);
  };
  getDate = value => {
    return (0, _getDate.getDate)(value);
  };
  getHours = value => {
    return (0, _getHours.getHours)(value);
  };
  getMinutes = value => {
    return (0, _getMinutes.getMinutes)(value);
  };
  getSeconds = value => {
    return (0, _getSeconds.getSeconds)(value);
  };
  getMilliseconds = value => {
    return (0, _getMilliseconds.getMilliseconds)(value);
  };
  setYear = (value, year) => {
    return (0, _setYear.setYear)(value, year);
  };
  setMonth = (value, month) => {
    return (0, _setMonth.setMonth)(value, month);
  };
  setDate = (value, date) => {
    return (0, _setDate.setDate)(value, date);
  };
  setHours = (value, hours) => {
    return (0, _setHours.setHours)(value, hours);
  };
  setMinutes = (value, minutes) => {
    return (0, _setMinutes.setMinutes)(value, minutes);
  };
  setSeconds = (value, seconds) => {
    return (0, _setSeconds.setSeconds)(value, seconds);
  };
  setMilliseconds = (value, milliseconds) => {
    return (0, _setMilliseconds.setMilliseconds)(value, milliseconds);
  };
  getDaysInMonth = value => {
    return (0, _getDaysInMonth.getDaysInMonth)(value);
  };
  getWeekArray = value => {
    const start = this.startOfWeek(this.startOfMonth(value));
    const end = this.endOfWeek(this.endOfMonth(value));
    let count = 0;
    let current = start;
    const nestedWeeks = [];
    while (this.isBefore(current, end)) {
      const weekNumber = Math.floor(count / 7);
      nestedWeeks[weekNumber] = nestedWeeks[weekNumber] || [];
      nestedWeeks[weekNumber].push(current);
      current = this.addDays(current, 1);
      count += 1;
    }
    return nestedWeeks;
  };
  getWeekNumber = value => {
    return (0, _getWeek.getWeek)(value, {
      locale: this.locale
    });
  };
  getYearRange = ([start, end]) => {
    const startDate = this.startOfYear(start);
    const endDate = this.endOfYear(end);
    const years = [];
    let current = startDate;
    while (this.isBefore(current, endDate)) {
      years.push(current);
      current = this.addYears(current, 1);
    }
    return years;
  };
}
exports.AdapterDateFns = AdapterDateFns;