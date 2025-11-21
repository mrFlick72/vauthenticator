import React from "react";
import {Box, Divider, Grid, ThemeProvider, Typography} from "@mui/material";
import theme from "../component/styles";
import Template from "../component/Template";
import {Person, VpnKey} from "@mui/icons-material";
import FormInputTextField from "../component/FormInputTextField";
import Separator from "../component/Separator";
import FormButton from "../component/FormButton";
import ComponentInitializer from "../utils/ComponentInitializer";
import getDataFromDomUtils from "../utils/getDataFromDomUtils";

interface ResetPasswordChallengeSenderPageProps {
    rawI18nMessages: string
}

const ResetPasswordChallengeSender: React.FC<ResetPasswordChallengeSenderPageProps> = ({rawI18nMessages}) => {
    const i18nMessages = JSON.parse(rawI18nMessages)
    const [email, setEmail] = React.useState("")

    const sentResetPasswordChallenge = async (email: string) => {
        let r = await fetch(`/api/reset-password-challenge`, {
            method: "PUT",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({"email": email}),
            credentials: 'same-origin'
        });
        if (r.status === 204) {
            window.location.href = "/reset-password/successful-reset-password-email-challenge";
        }
    }

    return (
        <ThemeProvider theme={theme}>
            <Template maxWidth="sm">
                <Typography variant="h3" component="h3">
                    <VpnKey fontSize="large"/> {i18nMessages["pageTitleText"]}
                </Typography>

                <Grid style={{marginTop: '10px'}}>
                    <Divider/>
                </Grid>

                <Box>
                    <FormInputTextField id="email"
                                        label="Email"

                                        required={true}
                                        handler={(value) => {
                                            setEmail(value.target.value)
                                        }}
                                        value={email || ""}
                                        suffix={<Person fontSize="large"/>}/>

                    <Separator/>

                    <FormButton type="button" label={i18nMessages["submitButtonText"]} onClickHandler={() => {
                        sentResetPasswordChallenge(email)
                    }}/>

                </Box>

            </Template>
        </ThemeProvider>
    )
}

const i18nMessages = getDataFromDomUtils('i18nMessages')

ComponentInitializer(<ResetPasswordChallengeSender rawI18nMessages={i18nMessages}/>)