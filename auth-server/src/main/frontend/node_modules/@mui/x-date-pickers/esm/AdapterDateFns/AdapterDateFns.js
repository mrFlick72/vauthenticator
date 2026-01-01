import { addDays } from 'date-fns/addDays';
import { addSeconds } from 'date-fns/addSeconds';
import { addMinutes } from 'date-fns/addMinutes';
import { addHours } from 'date-fns/addHours';
import { addWeeks } from 'date-fns/addWeeks';
import { addMonths } from 'date-fns/addMonths';
import { addYears } from 'date-fns/addYears';
import { endOfDay } from 'date-fns/endOfDay';
import { endOfWeek } from 'date-fns/endOfWeek';
import { endOfYear } from 'date-fns/endOfYear';
import { format as dateFnsFormat, longFormatters } from 'date-fns/format';
import { getDate } from 'date-fns/getDate';
import { getDaysInMonth } from 'date-fns/getDaysInMonth';
import { getHours } from 'date-fns/getHours';
import { getMinutes } from 'date-fns/getMinutes';
import { getMonth } from 'date-fns/getMonth';
import { getSeconds } from 'date-fns/getSeconds';
import { getMilliseconds } from 'date-fns/getMilliseconds';
import { getWeek } from 'date-fns/getWeek';
import { getYear } from 'date-fns/getYear';
import { isAfter } from 'date-fns/isAfter';
import { isBefore } from 'date-fns/isBefore';
import { isEqual } from 'date-fns/isEqual';
import { isSameDay } from 'date-fns/isSameDay';
import { isSameYear } from 'date-fns/isSameYear';
import { isSameMonth } from 'date-fns/isSameMonth';
import { isSameHour } from 'date-fns/isSameHour';
import { isValid } from 'date-fns/isValid';
import { parse as dateFnsParse } from 'date-fns/parse';
import { setDate } from 'date-fns/setDate';
import { setHours } from 'date-fns/setHours';
import { setMinutes } from 'date-fns/setMinutes';
import { setMonth } from 'date-fns/setMonth';
import { setSeconds } from 'date-fns/setSeconds';
import { setMilliseconds } from 'date-fns/setMilliseconds';
import { setYear } from 'date-fns/setYear';
import { startOfDay } from 'date-fns/startOfDay';
import { startOfMonth } from 'date-fns/startOfMonth';
import { endOfMonth } from 'date-fns/endOfMonth';
import { startOfWeek } from 'date-fns/startOfWeek';
import { startOfYear } from 'date-fns/startOfYear';
import { isWithinInterval } from 'date-fns/isWithinInterval';
import { enUS } from 'date-fns/locale/en-US';
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
        throw new Error(['MUI: The `date-fns` package v2.x is not compatible with this adapter.', 'Please, install v3.x or v4.x of the package or use the `AdapterDateFnsV2` instead.'].join('\n'));
      }
      if (!longFormatters) {
        throw new Error('MUI: The minimum supported `date-fns` package version compatible with this adapter is `3.2.x`.');
      }
    }
    /* v8 ignore stop */
    super({
      locale: locale ?? enUS,
      formats,
      longFormatters
    });
  }

  // TODO: explicit return types can be removed once there is only one date-fns version supported
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