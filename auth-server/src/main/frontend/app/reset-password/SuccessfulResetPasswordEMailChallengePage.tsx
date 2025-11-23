import React from "react";
import {Divider, Grid, ThemeProvider, Typography} from "@mui/material";
import theme from "../component/styles";
import Template from "../component/Template";
import {VpnKey} from "@mui/icons-material";
import ComponentInitializer from "../utils/ComponentInitializer";
import getDataFromDomUtils from "../utils/getDataFromDomUtils";

interface SuccessfulResetPasswordMailChallengePageProps {
    rawI18nMessages: string
}

const SuccessfulResetPasswordMailChallenge: React.FC<SuccessfulResetPasswordMailChallengePageProps> = ({rawI18nMessages}) => {
    const i18nMessages = JSON.parse(rawI18nMessages)
    return (
        <ThemeProvider theme={theme}>
            <Template maxWidth="lg">
                <Typography variant="h3" component="h3">
                    <VpnKey fontSize="large"/> {i18nMessages["pageTitleText"]}
                </Typography>

                <Grid style={{marginTop: '10px'}}>
                    <Divider/>
                </Grid>

                <Typography variant="h3" component="h3">
                    {i18nMessages["pageSuccessfulMessageText"]}
                </Typography>
            </Template>
        </ThemeProvider>
    )
}

const i18nMessages = getDataFromDomUtils('i18nMessages')

ComponentInitializer(<SuccessfulResetPasswordMailChallenge rawI18nMessages={i18nMessages}/>)