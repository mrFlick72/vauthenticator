import {Alert, Grid} from "@mui/material";
import React from "react";

interface ErrorBannerProps {
    errorMessage: string
}

const ErrorBanner : React.FC<ErrorBannerProps> = ({errorMessage}) => {
    return <Grid style={{marginTop: '10px'}}>
        <Alert severity="error">{errorMessage}</Alert>
    </Grid>

}

export default ErrorBanner