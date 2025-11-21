import * as React from 'react';
import {IMaskInput} from 'react-imask';
import {Grid, TextField} from "@mui/material";

interface CustomProps {
    onChange: (event: {
        target: {
            name: string;
            value: string
        }
    }) => void;
    name: string;
}

const InputMask = React.forwardRef<HTMLElement, CustomProps>(
    function TextMaskCustom(props, ref) {
    const {onChange, ...other} = props;
    return (
        <IMaskInput
            {...other}

            mask="+00 000 0000000"
            definitions={{
                '#': /[1-9]/,
            }}
            onAccept={(value) => props.onChange({target: {name: props.name, value}})}
            overwrite
        />
    );
});

interface FormInputMaskProps {
    id: string
    label: string
    required: boolean
    value: string
    handler: (value: any) => void
}

const FormInputMask: React.FC<FormInputMaskProps> = ({id, label, required, value, handler}) => {
    return <Grid container spacing={8} alignItems="flex-end">
        <Grid size={{ xs: 12 }}>
            <TextField
                fullWidth
                label={label}
                variant="outlined"
                required={required}
                value={value}
                onChange={handler}
                name={id}
                id={id}
                InputProps={{
                    inputComponent: InputMask as any,
                }}
            />
        </Grid>
    </Grid>
}


export default FormInputMask