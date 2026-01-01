import { OverrideMemoizeOptions, UnknownMemoizer } from 'reselect';
import type { CreateSelectorFunction } from "./createSelectorType.js";
export type { CreateSelectorFunction } from "./createSelectorType.js";
export declare const createSelector: CreateSelectorFunction;
export declare const createSelectorMemoizedWithOptions: (options?: OverrideMemoizeOptions<UnknownMemoizer>) => CreateSelectorFunction;
export declare const createSelectorMemoized: CreateSelectorFunction;