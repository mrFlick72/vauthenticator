import {Button, Grid} from "@mui/material";
import React, {MouseEventHandler} from "react";

interface FormButtonProps {
    labelPrefix?: any,
    label: string,
    type: any,
    buttonColor?: "inherit" | "success" | "error" | "primary" | "secondary" | "info" | "warning",
    onClickHandler?: MouseEventHandler<HTMLButtonElement>,
    direction?: string
}

const FormButton: React.FC<FormButtonProps> = ({labelPrefix, label, type, onClickHandler, direction, buttonColor}) => {
    return <div dir={direction || ""}>
        <Grid size={{ xs: 12 }}>
            <Grid container alignItems="flex-end" style={{marginTop: '10px'}}>
                <Button
                    type={type || "button"}
                    variant="outlined"
                    color={buttonColor || "primary"}
                    onClick={onClickHandler}
                    style={{textTransform: "none"}}>
                    {labelPrefix} {label}
                </Button>
            </Grid>
        </Grid>
    </div>
}

export default FormButton