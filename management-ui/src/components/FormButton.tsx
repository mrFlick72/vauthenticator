import React from "react";
import {Button, Grid} from "@mui/material";

interface FormButtonProps {
    labelPrefix?: React.ReactNode,
    label: string,
    type?: "button" | "submit" | "reset",
    onClickHandler?: React.MouseEventHandler<HTMLButtonElement>
    direction?: React.HTMLAttributes<HTMLDivElement>["dir"]
}

const FormButton: React.FC<FormButtonProps> = ({labelPrefix, label, type, onClickHandler, direction}) => {

    return <div dir={direction || ""}>
        <Grid size="grow">
            <Grid container sx={{alignItems: "flex-end", marginTop: "10px"}}>
                <Button type={type || "button"}
                        variant="outlined"
                        color="primary"
                        onClick={onClickHandler}
                        style={{textTransform: "none"}}>
                    {labelPrefix} {label}
                </Button>
            </Grid>
        </Grid>
    </div>
}

export default FormButton
