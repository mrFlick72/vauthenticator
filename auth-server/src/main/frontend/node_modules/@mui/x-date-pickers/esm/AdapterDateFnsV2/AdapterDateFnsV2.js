// date-fns@<3 has no exports field defined
// See https://github.com/date-fns/date-fns/issues/1781
/* eslint-disable import/extensions */
/* v8 ignore start */
// @ts-nocheck
import addDays from 'date-fns/addDays/index.js';
import addSeconds from 'date-fns/addSeconds/index.js';
import addMinutes from 'date-fns/addMinutes/index.js';
import addHours from 'date-fns/addHours/index.js';
import addWeeks from 'date-fns/addWeeks/index.js';
import addMonths from 'date-fns/addMonths/index.js';
import addYears from 'date-fns/addYears/index.js';
import endOfDay from 'date-fns/endOfDay/index.js';
import endOfWeek from 'date-fns/endOfWeek/index.js';
import endOfYear from 'date-fns/endOfYear/index.js';
import dateFnsFormat from 'date-fns/format/index.js';
import getDate from 'date-fns/getDate/index.js';
import getDaysInMonth from 'date-fns/getDaysInMonth/index.js';
import getHours from 'date-fns/getHours/index.js';
import getMinutes from 'date-fns/getMinutes/index.js';
import getMonth from 'date-fns/getMonth/index.js';
import getSeconds from 'date-fns/getSeconds/index.js';
import getMilliseconds from 'date-fns/getMilliseconds/index.js';
import getWeek from 'date-fns/getWeek/index.js';
import getYear from 'date-fns/getYear/index.js';
import isAfter from 'date-fns/isAfter/index.js';
import isBefore from 'date-fns/isBefore/index.js';
import isEqual from 'date-fns/isEqual/index.js';
import isSameDay from 'date-fns/isSameDay/index.js';
import isSameYear from 'date-fns/isSameYear/index.js';
import isSameMonth from 'date-fns/isSameMonth/index.js';
import isSameHour from 'date-fns/isSameHour/index.js';
import isValid from 'date-fns/isValid/index.js';
import dateFnsParse from 'date-fns/parse/index.js';
import setDate from 'date-fns/setDate/index.js';
import setHours from 'date-fns/setHours/index.js';
import setMinutes from 'date-fns/setMinutes/index.js';
import setMonth from 'date-fns/setMonth/index.js';
import setSeconds from 'date-fns/setSeconds/index.js';
import setMilliseconds from 'date-fns/setMilliseconds/index.js';
import setYear from 'date-fns/setYear/index.js';
import startOfDay from 'date-fns/startOfDay/index.js';
import startOfMonth from 'date-fns/startOfMonth/index.js';
import endOfMonth from 'date-fns/endOfMonth/index.js';
import startOfWeek from 'date-fns/startOfWeek/index.js';
import startOfYear from 'date-fns/startOfYear/index.js';
import isWithinInterval from 'date-fns/isWithinInterval/index.js';
import defaultLocale from 'date-fns/locale/en-US/index.js';
import longFormatters from 'date-fns/_lib/format/longFormatters/index.js';
/* v8 ignore end */

import { AdapterDateFnsBase } from "../AdapterDateFnsBase/index.js";
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
export class AdapterDateFns extends AdapterDateFnsBase {
  constructor({
    locale,
    formats
  } = {}) {
    /* v8 ignore start */
    if (process.env.NODE_ENV !== 'production') {
      if (typeof addDays !== 'function') {
        throw new Error(['MUI: This adapter is only compatible with `date-fns` v2.x package versions.', 'Please, install v2.x of the package or use the `AdapterDateFns` instead.'].join('\n'));
      }
    }
    /* v8 ignore stop */
    super({
      locale: locale ?? defaultLocale,
      formats,
      longFormatters
    });
  }
  parse = (value, format) => {
    if (value === '') {
      return null;
    }
    return dateFnsParse(value, format, new Date(), {
      locale: this.locale
    });
  };
  isValid = value => {
    if (value == null) {
      return false;
    }
    return isValid(value);
  };
  format = (value, formatKey) => {
    return this.formatByString(value, this.formats[formatKey]);
  };
  formatByString = (value, formatString) => {
    return dateFnsFormat(value, formatString, {
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
    return isEqual(value, comparing);
  };
  isSameYear = (value, comparing) => {
    return isSameYear(value, comparing);
  };
  isSameMonth = (value, comparing) => {
    return isSameMonth(value, comparing);
  };
  isSameDay = (value, comparing) => {
    return isSameDay(value, comparing);
  };
  isSameHour = (value, comparing) => {
    return isSameHour(value, comparing);
  };
  isAfter = (value, comparing) => {
    return isAfter(value, comparing);
  };
  isAfterYear = (value, comparing) => {
    return isAfter(value, endOfYear(comparing));
  };
  isAfterDay = (value, comparing) => {
    return isAfter(value, endOfDay(comparing));
  };
  isBefore = (value, comparing) => {
    return isBefore(value, comparing);
  };
  isBeforeYear = (value, comparing) => {
    return isBefore(value, this.startOfYear(comparing));
  };
  isBeforeDay = (value, comparing) => {
    return isBefore(value, this.startOfDay(comparing));
  };
  isWithinRange = (value, [start, end]) => {
    return isWithinInterval(value, {
      start,
      end
    });
  };
  startOfYear = value => {
    return startOfYear(value);
  };
  startOfMonth = value => {
    return startOfMonth(value);
  };
  startOfWeek = value => {
    return startOfWeek(value, {
      locale: this.locale
    });
  };
  startOfDay = value => {
    return startOfDay(value);
  };
  endOfYear = value => {
    return endOfYear(value);
  };
  endOfMonth = value => {
    return endOfMonth(value);
  };
  endOfWeek = value => {
    return endOfWeek(value, {
      locale: this.locale
    });
  };
  endOfDay = value => {
    return endOfDay(value);
  };
  addYears = (value, amount) => {
    return addYears(value, amount);
  };
  addMonths = (value, amount) => {
    return addMonths(value, amount);
  };
  addWeeks = (value, amount) => {
    return addWeeks(value, amount);
  };
  addDays = (value, amount) => {
    return addDays(value, amount);
  };
  addHours = (value, amount) => {
    return addHours(value, amount);
  };
  addMinutes = (value, amount) => {
    return addMinutes(value, amount);
  };
  addSeconds = (value, amount) => {
    return addSeconds(value, amount);
  };
  getYear = value => {
    return getYear(value);
  };
  getMonth = value => {
    return getMonth(value);
  };
  getDate = value => {
    return getDate(value);
  };
  getHours = value => {
    return getHours(value);
  };
  getMinutes = value => {
    return getMinutes(value);
  };
  getSeconds = value => {
    return getSeconds(value);
  };
  getMilliseconds = value => {
    return getMilliseconds(value);
  };
  setYear = (value, year) => {
    return setYear(value, year);
  };
  setMonth = (value, month) => {
    return setMonth(value, month);
  };
  setDate = (value, date) => {
    return setDate(value, date);
  };
  setHours = (value, hours) => {
    return setHours(value, hours);
  };
  setMinutes = (value, minutes) => {
    return setMinutes(value, minutes);
  };
  setSeconds = (value, seconds) => {
    return setSeconds(value, seconds);
  };
  setMilliseconds = (value, milliseconds) => {
    return setMilliseconds(value, milliseconds);
  };
  getDaysInMonth = value => {
    return getDaysInMonth(value);
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
    return getWeek(value, {
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