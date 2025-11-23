import React, {useEffect, useState} from "react";
import ErrorBanner from "../component/ErrorBanner";
import {getMfaMethods, MfaAccountEnrolledMethod, sendMfaCode} from "./MfaRepository";
import {Box, Button, Divider, Grid, ThemeProvider, Typography} from "@mui/material";
import theme from "../component/styles";
import Modal from "../component/Modal";
import Template from "../component/Template";
import {Call, CheckCircle, Email, Person, VpnKey} from "@mui/icons-material";
import FormInputTextField from "../component/FormInputTextField";
import Separator from "../component/Separator";
import FormButton from "../component/FormButton";

interface MfaChallengePageProps {
    rawI18nMessages: string
    rawErrors: string
    hasServerSideErrors: boolean
    csrfName: string
    csrfToken: string
}

const MfaChallengePage: React.FC<MfaChallengePageProps> = ({
                                                               rawI18nMessages,
                                                               rawErrors,
                                                               hasServerSideErrors,
                                                               csrfName,
                                                               csrfToken
                                                           }) => {
    const errorMessage = JSON.parse(rawErrors)["feedback"];
    const i18nMessages = JSON.parse(rawI18nMessages)

    const errorsBanner = <ErrorBanner errorMessage={errorMessage}/>
    const [openChooseMFAModal, setOpenChooseMFAModal] = React.useState(false)
    const [defaultMfaDevice, setDefaultMfaDevice] = useState<string>("")
    const [mfaAccountEnrolledMethod, setMfaAccountEnrolledMethod] = useState<MfaAccountEnrolledMethod[]>()
    const handleCloseChooseMFAModal = () => {
        setOpenChooseMFAModal(false);
    };
    const handleOpenChooseMFAModal = () => {
        setOpenChooseMFAModal(true);
    }

    useEffect(() => {
        getMfaMethods()
            .then(result => {
                setMfaAccountEnrolledMethod(result)
                result.forEach((method) => {
                    if (method.default) {
                        setDefaultMfaDevice(method.mfaDeviceId)
                    }
                })
            })

    }, [])

    const mfaIcon = (mfaMethod: MfaAccountEnrolledMethod) => {
        let icon
        if ("EMAIL_MFA_METHOD" === mfaMethod.mfaMethod) {
            icon = <Button
                type="button"
                style={{textTransform: "none", color: "black"}}
                onClick={() => setDefaultMfaDevice(mfaMethod.mfaDeviceId)}>
                <Email/> EMail: {mfaMethod.mfaChannel} {mfaIconCheckIconFor(mfaMethod)}
            </Button>
        } else if ("SMS_MFA_METHOD" === mfaMethod.mfaMethod) {
            icon = <Button
                type="button"
                style={{textTransform: "none", color: "black"}}
                onClick={() => setDefaultMfaDevice(mfaMethod.mfaDeviceId)}>
                <Call/> Phone: {mfaMethod.mfaChannel} {mfaIconCheckIconFor(mfaMethod)}
            </Button>
        }
        return icon
    }

    const mfaIconCheckIconFor = (mfaMethod: MfaAccountEnrolledMethod) => {
        let icon = <CheckCircle/>
        if (defaultMfaDevice === mfaMethod.mfaDeviceId) {
            icon = <CheckCircle sx={{color: "green"}}/>
        }

        return icon
    }


    return (
        <ThemeProvider theme={theme}>

            <Modal maxWidth="sm"
                   open={openChooseMFAModal}
                   onExecute={handleCloseChooseMFAModal}
                   onExecuteButtonLabel={i18nMessages["changeMfaMethodModalExecuteButtonText"]}
                   onClose={handleCloseChooseMFAModal}
                   onCloseButtonLabel={i18nMessages["changeMfaMethodModalCloseButtonText"]}
                   headerLabel={i18nMessages["changeMfaMethodModalHeaderText"]}
                   title={i18nMessages["changeMfaMethodModalTitleText"]}>
                {mfaAccountEnrolledMethod?.map(method => <p>{mfaIcon(method)}</p>)}
            </Modal>

            <Template maxWidth="sm">
                <Typography variant="h3" component="h3">
                    <VpnKey fontSize="large"/> {i18nMessages["pageTitleText"]}
                </Typography>

                <Grid style={{marginTop: '10px'}}>
                    <Divider/>
                </Grid>
                {hasServerSideErrors ? errorsBanner : ""}

                {<form action="mfa-challenge" method="post">
                    <Box>
                        <input name="mfa-method" type="hidden" value="EMAIL_MFA_METHOD"/>
                        <input name={csrfName} type="hidden" value={csrfToken}/>
                        <input name="mfa-device-id" type="hidden" value={defaultMfaDevice}/>

                        <FormInputTextField id="mfa-code"
                                            label={i18nMessages["mfaPlaceholderText"]}
                                            type="text"
                                            suffix={<Person fontSize="large"/>}/>

                        <Separator/>

                        <FormButton type="submit" label={i18nMessages["submitButtonText"]}
                                    buttonColor={"success"}/>

                        <Separator/>

                        <Grid container size={{ sm: 12 }}>
                            <Grid size={{ sm: 4 }} >
                                <FormButton type="button" label={i18nMessages["sendAgainButtonText"]}
                                            onClickHandler={() => sendMfaCode(defaultMfaDevice)}/>
                            </Grid>
                            <Grid size={{ sm: 4 }}> </Grid>
                            <Grid size={{ sm: 4 }}>
                                <FormButton type="button" label={i18nMessages["changeMfaMethodButtonText"]}
                                            direction={"rtl"}
                                            onClickHandler={() => handleOpenChooseMFAModal()}/>
                            </Grid>
                        </Grid>
                    </Box>
                </form>}
            </Template>
        </ThemeProvider>

    )
}

export default MfaChallengePage