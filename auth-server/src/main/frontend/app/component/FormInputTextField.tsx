import { Grid, InputAdornment, TextField } from "@mui/material";
import React, { ChangeEventHandler } from "react";

interface FormInputTextFieldProps {
  id: string;
  label: string;
  type?: string;
  required?: boolean;
  autoFocus?: boolean;
  disabled?: boolean;
  suffix?: any;
  value?: string;
  handler?: ChangeEventHandler<HTMLInputElement | HTMLTextAreaElement>;
}

const FormInputTextField: React.FC<FormInputTextFieldProps> = ({
  id,
  label,
  type,
  required,
  autoFocus,
  disabled,
  suffix,
  value,
  handler,
}) => {
  return (
    <Grid container spacing={8} alignItems="flex-end">
      <Grid size={{ xs: 12 }}>
        <TextField
          name={id}
          id={id}
          label={label}
          type={type || "text"}
          disabled={disabled}
          variant="outlined"
          fullWidth
          autoFocus={autoFocus}
          required={required || false}
          value={value}
          onChange={handler}
          slotProps={{
            input: suffix
              ? {
                  startAdornment: (
                    <InputAdornment position="start">{suffix}</InputAdornment>
                  ),
                }
              : undefined,
          }}
        />
      </Grid>
    </Grid>
  );
};

export default FormInputTextField;
