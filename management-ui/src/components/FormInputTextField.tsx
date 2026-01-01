import React from "react";
import {Grid, TextField} from "@mui/material";

interface FormInputTextFieldProps {
    id: string,
    label: string,
    type?: string,
    required?: boolean,
    autoFocus?: boolean,
    disabled?: boolean,
    prefix?: string,
    suffixItem?: React.ReactNode,
    value: string,
    handler: (input: any) => void
}

const FormInputTextField: React.FC<FormInputTextFieldProps> = ({
                                                                   id,
                                                                   label,
                                                                   type,
                                                                   required,
                                                                   autoFocus,
                                                                   disabled,
                                                                   suffixItem,
                                                                   prefix,
                                                                   value,
                                                                   handler
                                                               }) => {
    return <Grid container spacing={8} alignItems="flex-end">
        {prefix && <Grid item>
            {prefix}
        </Grid>}
        <Grid item md={true} sm={true} xs={true}>
            <TextField name={id} id={id} label={label} type={type || "text"} disabled={disabled}
                       variant="outlined" fullWidth autoFocus={autoFocus} required={required || false}
                       value={value}
                       InputProps={{
                           endAdornment: (suffixItem),
                       }}
                       onChange={handler}/>
        </Grid>
    </Grid>
}

export default FormInputTextField