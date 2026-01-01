import { UseDesktopPickerParams, UseDesktopPickerProps } from "./useDesktopPicker.types.js";
import { DateOrTimeViewWithMeridiem } from "../../models/index.js";
/**
 * Hook managing all the single-date desktop pickers:
 * - DesktopDatePicker
 * - DesktopDateTimePicker
 * - DesktopTimePicker
 */
export declare const useDesktopPicker: <TView extends DateOrTimeViewWithMeridiem, TEnableAccessibleFieldDOMStructure extends boolean, TExternalProps extends UseDesktopPickerProps<TView, TEnableAccessibleFieldDOMStructure, any, TExternalProps>>({
  props,
  steps,
  ...pickerParams
}: UseDesktopPickerParams<TView, TEnableAccessibleFieldDOMStructure, TExternalProps>) => {
  renderPicker: () => import("react/jsx-runtime").JSX.Element;
};