import React from 'react';
import {Divider, Grid, ThemeProvider, Typography} from "@mui/material";
import theme from "../component/styles";
import Template from "../component/Template";
import {VpnKey} from "@mui/icons-material";
import ComponentInitializer from "../utils/ComponentInitializer";
import getDataFromDomUtils from "../utils/getDataFromDomUtils";

interface ResetPasswordPageProps {
    rawI18nMessages: string
}

const ResetPasswordMainPage: React.FC<ResetPasswordPageProps> = ({rawI18nMessages}) => {
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

                <Typography variant="h3" component="h2">
                    {i18nMessages["pageSuccessfulMessageText"]}
                </Typography>
            </Template>
        </ThemeProvider>
    )
}

const i18nMessages = getDataFromDomUtils('i18nMessages')

ComponentInitializer(<ResetPasswordMainPage rawI18nMessages={i18nMessages}/>)