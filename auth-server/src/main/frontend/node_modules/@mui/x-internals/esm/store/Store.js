import _extends from "@babel/runtime/helpers/esm/extends";
/* eslint-disable no-cond-assign */

export class Store {
  // HACK: `any` fixes adding listeners that accept partial state.

  // Internal state to handle recursive `setState()` calls

  static create(state) {
    return new Store(state);
  }
  constructor(state) {
    this.state = state;
    this.listeners = new Set();
    this.updateTick = 0;
  }
  subscribe = fn => {
    this.listeners.add(fn);
    return () => {
      this.listeners.delete(fn);
    };
  };
  getSnapshot = () => {
    return this.state;
  };
  setState(newState) {
    this.state = newState;
    this.updateTick += 1;
    const currentTick = this.updateTick;
    const it = this.listeners.values();
    let result;
    while (result = it.next(), !result.done) {
      if (currentTick !== this.updateTick) {
        // If the tick has changed, a recursive `setState` call has been made,
        // and it has already notified all listeners.
        return;
      }
      const listener = result.value;
      listener(newState);
    }
  }
  update(changes) {
    for (const key in changes) {
      if (!Object.is(this.state[key], changes[key])) {
        this.setState(_extends({}, this.state, changes));
        return;
      }
    }
  }
  set(key, value) {
    if (!Object.is(this.state[key], value)) {
      this.setState(_extends({}, this.state, {
        [key]: value
      }));
    }
  }
}