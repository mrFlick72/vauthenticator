import { Theme } from '@mui/material/styles';
import { SxProps, SystemStyleObject } from '@mui/system';
import * as React from 'react';
export declare function arrayIncludes<T>(array: T[] | readonly T[], itemOrItems: T | T[]): boolean;
export declare const onSpaceOrEnter: (innerFn: (ev: React.MouseEvent<any> | React.KeyboardEvent<any>) => void, externalEvent?: (event: React.KeyboardEvent<any>) => void) => (event: React.KeyboardEvent) => void;
export declare const executeInTheNextEventLoopTick: (fn: () => void) => void;
/**
 * Gets the currently active element within a given node's document.
 * This function traverses shadow DOM if necessary.
 * @param node - The node from which to get the active element.
 * @returns The currently active element, or null if none is found.
 */
export declare const getActiveElement: (node: Node | null | undefined) => Element | null;
/**
 * Gets the index of the focused list item in a given ul list element.
 *
 * @param {HTMLUListElement} listElement - The list element to search within.
 * @returns {number} The index of the focused list item, or -1 if none is focused.
 */
export declare const getFocusedListItemIndex: (listElement: HTMLUListElement) => number;
export declare const DEFAULT_DESKTOP_MODE_MEDIA_QUERY = "@media (pointer: fine)";
export declare function mergeSx(...sxProps: (SxProps<Theme> | undefined)[]): ReadonlyArray<boolean | SystemStyleObject<Theme> | ((theme: Theme) => SystemStyleObject<Theme>)>;