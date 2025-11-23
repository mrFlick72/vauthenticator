import React from "react";

import Template from "../component/Template";
import {Divider, Grid, ThemeProvider, Typography} from "@mui/material";
import {VpnKey} from "@mui/icons-material";
import theme from "../component/styles";
import ComponentInitializer from "../utils/ComponentInitializer";
import getDataFromDomUtils from "../utils/getDataFromDomUtils";

interface SuccessfulSignUpProps {
    rawI18nMessages: string
}

const SuccessfulSignUpPage: React.FC<SuccessfulSignUpProps> = ({rawI18nMessages}) => {
    const i18nMessages = JSON.parse(rawI18nMessages);

    return (
        <ThemeProvider theme={theme}>
            <Template maxWidth="lg">
                <Typography variant="h3" component="h3">
                    <VpnKey fontSize="large"/> {i18nMessages["pageTitleText"]} VAuthenticator
                </Typography>

                <Grid style={{marginTop: '10px'}}>
                    <Divider/>
                </Grid>

                <Typography variant="h3" component="h3">
                    {i18nMessages["successfulSignUpMessage"]}
                </Typography>
            </Template>
        </ThemeProvider>
    )
}

const i18nMessages = getDataFromDomUtils('i18nMessages')

ComponentInitializer(<SuccessfulSignUpPage rawI18nMessages={i18nMessages}/>)