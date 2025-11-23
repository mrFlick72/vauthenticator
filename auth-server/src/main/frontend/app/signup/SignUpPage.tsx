import React from "react";
import Template from "../component/Template";
import FormInputTextField from "../component/FormInputTextField";
import FormButton from "../component/FormButton";

import signUp from "./SignUpRepository";
import FormDatePicker, {ApiDateFormatPattern} from "../component/FormDatePicker";
import {Divider, Grid, ThemeProvider, Typography} from "@mui/material";
import {GroupAdd, VpnKey} from "@mui/icons-material";
import theme from "../component/styles";
import FormInputMask from "../component/FormInputMask";
import ComponentInitializer from "../utils/ComponentInitializer";
import getDataFromDomUtils from "../utils/getDataFromDomUtils";

interface SignUpProps {
    rawI18nMessages: string
}

const SignUpPage: React.FC<SignUpProps> = ({rawI18nMessages}) => {
    const i18nMessages = JSON.parse(rawI18nMessages);

    const [email, setEmail] = React.useState("")
    const [password, setPassword] = React.useState("")
    const [firstName, setFirstName] = React.useState("")
    const [lastName, setLastName] = React.useState("")
    const [birthDate, setBirthDate] = React.useState("")
    const [phone, setPhone] = React.useState("")

    return (
        <ThemeProvider theme={theme}>
            <Template maxWidth="sm">
                <Typography variant="h3" component="h3">
                    <VpnKey fontSize="large"/> {i18nMessages["pageTitleText"]} VAuthenticator
                </Typography>

                <Grid style={{marginTop: '10px'}}>
                    <Divider/>
                </Grid>

                <FormInputTextField id="email"
                                    label={i18nMessages["emailPlaceholderText"]}
                                    required={true}
                                    handler={(value) => {
                                        setEmail(value.target.value)
                                    }}
                                    value={email || ""}/>

                <FormInputTextField id="password"
                                    label={i18nMessages["passwordPlaceholderText"]}
                                    type="Password"
                                    required={true}
                                    handler={(value) => {
                                        setPassword(value.target.value)
                                    }}
                                    value={password || ""}/>

                <FormInputTextField id="firstName"
                                    label={i18nMessages["firstNamePlaceholderText"]}
                                    required={true}
                                    handler={(value) => {
                                        setFirstName(value.target.value)
                                    }}
                                    value={firstName || ""}/>

                <FormInputTextField id="lastName"
                                    label={i18nMessages["lastNamePlaceholderText"]}
                                    required={true}
                                    handler={(value) => {
                                        setLastName(value.target.value)
                                    }}
                                    value={lastName || ""}/>

                <FormDatePicker
                    value={birthDate}
                    pattern={ApiDateFormatPattern}
                    onClickHandler={(value) => {
                        let date = "";
                        try {
                            date = value.format(ApiDateFormatPattern);
                            setBirthDate(date)
                        } catch (e) {
                            console.error(e)
                        }
                    }}
                    label={i18nMessages["birthDatePlaceholderText"]}/>


                <FormInputMask id="phone"
                               label={i18nMessages["phonePlaceholderText"]}
                               required={false}
                               handler={(value: any) => {
                                   setPhone(value.target.value)
                               }}
                               value={phone || ""}/>

                <Grid style={{marginTop: '10px'}}>
                    <Divider/>
                </Grid>

                <FormButton type="button"
                            onClickHandler={() => {
                                signUp({
                                    email: email,
                                    password: password,
                                    firstName: firstName,
                                    lastName: lastName,
                                    phone: phone,
                                    birthDate: birthDate
                                })
                                    .then(r => {
                                        if (r.status === 201) {
                                            window.location.href = "/sign-up/succeeded";
                                        }
                                    })
                            }}
                            labelPrefix={<GroupAdd fontSize="large"/>}
                            label={i18nMessages["submitButtonText"]}/>
            </Template>
        </ThemeProvider>
    )
}

const  i18nMessages = getDataFromDomUtils('i18nMessages')

ComponentInitializer(<SignUpPage rawI18nMessages={i18nMessages}/>)