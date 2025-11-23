import React from 'react';
import Template from "../component/Template";
import {Divider, Grid, Paper, ThemeProvider, Typography} from "@mui/material";
import {Fingerprint, VpnKey} from "@mui/icons-material";
import theme from "../component/styles";
import getDataFromDomUtils from "../utils/getDataFromDomUtils";
import ComponentInitializer from "../utils/ComponentInitializer";
import FormButton from "../component/FormButton";
import FormInputTextField from "../component/FormInputTextField";
import Separator from "../component/Separator";
import ErrorBanner from "../component/ErrorBanner";

interface ChangePasswordPageProps {
    rawI18nMessages: string
    rawErrors: string
    hasServerSideErrors : boolean
    csrfName: string
    csrfToken: string
}

const ResetChangePasswordPage: React.FC<ChangePasswordPageProps> = ({
                                                                        rawI18nMessages,
                                                                        rawErrors,
                                                                        hasServerSideErrors,

                                                                        csrfName,
                                                                        csrfToken
                                                                    }) => {
    const i18nMessages = JSON.parse(rawI18nMessages);
    const errorMessage = JSON.parse(rawErrors)["feedback"];
    const errorsBanner = <ErrorBanner errorMessage={errorMessage}/>

    return (
        <ThemeProvider theme={theme}>
            <Template maxWidth="sm">
                <Typography variant="h3" component="h3">
                    <VpnKey fontSize="large"/> {i18nMessages["pageTitleText"]}
                </Typography>

                <Grid style={{marginTop: '10px'}}>
                    <Divider/>
                </Grid>

                <Paper>
                    {hasServerSideErrors ? errorsBanner : ""}

                    <form action="/change-password" method="post">
                        <input name={csrfName} type="hidden" value={csrfToken}/>

                        <FormInputTextField id="new-password"
                                            label={i18nMessages["passwordPlaceholderText"]}
                                            type="password"
                                            suffix={<Fingerprint fontSize="large"/>}/>

                        <Separator/>
                        <FormButton type="submit" label={i18nMessages["submitButtonTextReset"]}/>
                    </form>
                </Paper>
            </Template>
        </ThemeProvider>
    )
}

const rawI18nMessages = getDataFromDomUtils('i18nMessages')
const rawErrors = getDataFromDomUtils('errors')
const hasServerSideErrors = getDataFromDomUtils('hasServerSideErrors') === "true"
const csrfName = getDataFromDomUtils('csrfName')
const csrfToken = getDataFromDomUtils('csrfToken')

ComponentInitializer(<ResetChangePasswordPage csrfName={csrfName} csrfToken={csrfToken} hasServerSideErrors={hasServerSideErrors}
                                              rawErrors={rawErrors} rawI18nMessages={rawI18nMessages}/>)