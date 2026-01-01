import * as React from 'react';
interface OutlineProps extends React.HTMLAttributes<HTMLFieldSetElement> {
  notched: boolean;
  shrink: boolean;
  label: React.ReactNode;
}
/**
 * @ignore - internal component.
 */
export default function Outline(props: OutlineProps): import("react/jsx-runtime").JSX.Element;
export {};