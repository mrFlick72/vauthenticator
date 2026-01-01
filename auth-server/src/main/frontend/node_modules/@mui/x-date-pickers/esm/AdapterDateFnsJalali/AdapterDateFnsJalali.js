import _extends from "@babel/runtime/helpers/esm/extends";
import { addSeconds } from 'date-fns-jalali/addSeconds';
import { addMinutes } from 'date-fns-jalali/addMinutes';
import { addHours } from 'date-fns-jalali/addHours';
import { addDays } from 'date-fns-jalali/addDays';
import { addWeeks } from 'date-fns-jalali/addWeeks';
import { addMonths } from 'date-fns-jalali/addMonths';
import { addYears } from 'date-fns-jalali/addYears';
import { endOfDay } from 'date-fns-jalali/endOfDay';
import { endOfWeek } from 'date-fns-jalali/endOfWeek';
import { endOfYear } from 'date-fns-jalali/endOfYear';
import { format as dateFnsFormat, longFormatters } from 'date-fns-jalali/format';
import { getHours } from 'date-fns-jalali/getHours';
import { getSeconds } from 'date-fns-jalali/getSeconds';
import { getMilliseconds } from 'date-fns-jalali/getMilliseconds';
import { getWeek } from 'date-fns-jalali/getWeek';
import { getYear } from 'date-fns-jalali/getYear';
import { getMonth } from 'date-fns-jalali/getMonth';
import { getDate } from 'date-fns-jalali/getDate';
import { getDaysInMonth } from 'date-fns-jalali/getDaysInMonth';
import { getMinutes } from 'date-fns-jalali/getMinutes';
import { isAfter } from 'date-fns-jalali/isAfter';
import { isBefore } from 'date-fns-jalali/isBefore';
import { isEqual } from 'date-fns-jalali/isEqual';
import { isSameDay } from 'date-fns-jalali/isSameDay';
import { isSameYear } from 'date-fns-jalali/isSameYear';
import { isSameMonth } from 'date-fns-jalali/isSameMonth';
import { isSameHour } from 'date-fns-jalali/isSameHour';
import { isValid } from 'date-fns-jalali/isValid';
import { parse as dateFnsParse } from 'date-fns-jalali/parse';
import { setDate } from 'date-fns-jalali/setDate';
import { setHours } from 'date-fns-jalali/setHours';
import { setMinutes } from 'date-fns-jalali/setMinutes';
import { setMonth } from 'date-fns-jalali/setMonth';
import { setSeconds } from 'date-fns-jalali/setSeconds';
import { setMilliseconds } from 'date-fns-jalali/setMilliseconds';
import { setYear } from 'date-fns-jalali/setYear';
import { startOfDay } from 'date-fns-jalali/startOfDay';
import { startOfMonth } from 'date-fns-jalali/startOfMonth';
import { endOfMonth } from 'date-fns-jalali/endOfMonth';
import { startOfWeek } from 'date-fns-jalali/startOfWeek';
import { startOfYear } from 'date-fns-jalali/startOfYear';
import { isWithinInterval } from 'date-fns-jalali/isWithinInterval';
import { faIR as defaultLocale } from 'date-fns-jalali/locale/fa-IR';
import { AdapterDateFnsBase } from "../AdapterDateFnsBase/index.js";
const defaultFormats = {
  year: 'yyyy',
  month: 'LLLL',
  monthShort: 'MMM',
  dayOfMonth: 'd',
  dayOfMonthFull: 'do',
  weekday: 'EEEE',
  weekdayShort: 'EEEEEE',
  hours24h: 'HH',
  hours12h: 'hh',
  meridiem: 'aa',
  minutes: 'mm',
  seconds: 'ss',
  fullDate: 'PPP',
  keyboardDate: 'P',
  shortDate: 'd MMM',
  normalDate: 'd MMMM',
  normalDateWithWeekday: 'EEE, d MMMM',
  fullTime12h: 'hh:mm aaa',
  fullTime24h: 'HH:mm',
  keyboardDateTime12h: 'P hh:mm aa',
  keyboardDateTime24h: 'P HH:mm'
};
const NUMBER_SYMBOL_MAP = {
  '1': '۱',
  '2': '۲',
  '3': '۳',
  '4': '۴',
  '5': '۵',
  '6': '۶',
  '7': '۷',
  '8': '۸',
  '9': '۹',
  '0': '۰'
};
/**
 * Based on `@date-io/date-fns-jalali`
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
export class AdapterDateFnsJalali extends AdapterDateFnsBase {
  constructor({
    locale,
    formats
  } = {}) {
    /* v8 ignore start */
    if (process.env.NODE_ENV !== 'production') {
      if (typeof addDays !== 'function') {
        throw new Error(['MUI: The `date-fns-jalali` package v2.x is not compatible with this adapter.', 'Please, install v3.x or v4.x of the package or use the `AdapterDateFnsJalaliV2` instead.'].join('\n'));
      }
      if (!longFormatters) {
        throw new Error('MUI: The minimum supported `date-fns-jalali` package version compatible with this adapter is `3.2.x`.');
      }
    }
    /* v8 ignore stop */
    super({
      locale: locale ?? defaultLocale,
      // some formats are different in jalali adapter,
      // this ensures that `AdapterDateFnsBase` formats are overridden
      formats: _extends({}, defaultFormats, formats),
      longFormatters,
      lib: 'date-fns-jalali'
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
  formatNumber = numberToFormat => {
    return numberToFormat.replace(/\d/g, match => NUMBER_SYMBOL_MAP[match]).replace(/,/g, '،');
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
    return isAfter(value, this.endOfYear(comparing));
  };
  isAfterDay = (value, comparing) => {
    return isAfter(value, this.endOfDay(comparing));
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
  getWeekNumber = date => {
    return getWeek(date, {
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