import React from 'react';
import {Divider, Grid, ThemeProvider, Typography} from "@mui/material";
import theme from "../component/styles";
import Template from "../component/Template";
import {Mail} from "@mui/icons-material";
import ComponentInitializer from "../utils/ComponentInitializer";
import getDataFromDomUtils from "../utils/getDataFromDomUtils";

interface SuccessfulMailVerifyPagePageProps {
    rawI18nMessages: string
}

const SuccessfulEMailVerifyPage: React.FC<SuccessfulMailVerifyPagePageProps> = ({rawI18nMessages}) => {
    let i18nMessages = JSON.parse(rawI18nMessages);

    return (
        <ThemeProvider theme={theme}>
            <Template maxWidth="lg">
                <Typography variant="h3" component="h3">
                    <Mail fontSize="large"/> {i18nMessages["pageTitleText"]}
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

ComponentInitializer(<SuccessfulEMailVerifyPage rawI18nMessages={i18nMessages}/>)