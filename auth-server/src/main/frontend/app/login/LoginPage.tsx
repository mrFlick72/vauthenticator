import React from "react";
import {Box, Divider, Grid, ThemeProvider} from "@mui/material";
import theme from "../component/styles";
import Template from "../component/Template";
import FormInputTextField from "../component/FormInputTextField";
import {Fingerprint, Person} from "@mui/icons-material";
import Separator from "../component/Separator";
import FormButton from "../component/FormButton";
import ErrorBanner from "../component/ErrorBanner";
import getDataFromDomUtils from "../utils/getDataFromDomUtils";
import ComponentInitializer from "../utils/ComponentInitializer";


const VAuthenticatorTitle = () => {
    return <svg version="1.0" xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 500.000000 174.000000"
                preserveAspectRatio="xMidYMid meet">

        <g transform="translate(0.000000,174.000000) scale(0.100000,-0.100000)"
           fill="#000000" stroke="none">
            <path d="M785 1479 c-102 -80 -247 -125 -371 -116 -55 4 -85 2 -93 -6 -14 -14
-15 -176 0 -304 30 -274 118 -463 296 -634 65 -64 174 -142 214 -154 30 -10
134 52 216 129 156 146 250 327 298 577 20 103 31 332 18 379 l-9 34 -125 1
c-111 1 -132 4 -194 28 -38 15 -96 43 -128 62 -32 19 -64 35 -70 35 -7 0 -30
-14 -52 -31z m118 -73 c104 -58 209 -89 309 -90 84 -1 88 -2 88 -23 0 -22 -4
-23 -83 -23 -105 0 -217 28 -306 76 l-68 36 -52 -37 c-98 -70 -241 -106 -364
-91 -48 6 -54 10 -50 27 5 16 14 19 73 19 116 0 254 45 350 114 19 14 37 25
38 26 2 0 31 -15 65 -34z m162 -151 c39 -8 107 -15 153 -15 l82 0 0 -67 c0
-271 -104 -543 -273 -717 -55 -56 -155 -126 -180 -126 -56 0 -260 193 -339
320 -77 124 -122 298 -130 500 l-3 75 110 0 c130 0 217 24 304 83 l55 37 75
-38 c42 -20 108 -44 146 -52z"/>
            <path d="M2580 845 c0 -168 1 -175 20 -175 18 0 20 7 20 73 0 82 15 107 61
107 41 0 49 -18 49 -102 0 -71 2 -78 20 -78 18 0 20 7 20 88 0 80 -2 91 -23
110 -28 26 -83 29 -109 5 -17 -15 -18 -13 -18 65 0 75 -2 82 -20 82 -19 0 -20
-7 -20 -175z"/>
            <path d="M1540 986 c0 -2 27 -73 59 -157 80 -207 83 -207 162 -5 33 82 59 153
59 158 0 5 -10 8 -22 6 -17 -2 -30 -25 -67 -125 -25 -67 -49 -119 -52 -115 -4
4 -26 60 -49 125 -38 104 -45 117 -66 117 -13 0 -24 -2 -24 -4z"/>
            <path d="M1877 838 c-37 -85 -67 -157 -67 -161 0 -4 11 -7 23 -7 19 0 28 9 40
40 l15 40 77 0 77 0 15 -40 c12 -31 21 -40 40 -40 12 0 23 4 23 8 0 4 -29 75
-64 157 -89 206 -90 206 -179 3z m142 -35 c1 -9 -16 -13 -54 -13 -30 0 -55 4
-55 9 0 5 12 38 27 72 l27 62 27 -59 c15 -32 28 -65 28 -71z"/>
            <path d="M3504 975 c-4 -9 -2 -21 4 -27 16 -16 47 -5 47 17 0 26 -42 34 -51
10z"/>
            <path d="M2427 943 c-4 -3 -7 -17 -7 -30 0 -16 -6 -23 -20 -23 -13 0 -20 -7
-20 -20 0 -13 7 -20 19 -20 16 0 19 -10 23 -75 3 -41 11 -81 18 -90 7 -9 29
-15 51 -15 33 0 39 3 39 21 0 15 -5 19 -15 15 -35 -13 -45 4 -45 74 0 67 0 69
28 72 16 2 27 9 27 18 0 9 -11 16 -27 18 -24 3 -28 8 -28 33 0 21 -5 29 -18
29 -10 0 -22 -3 -25 -7z"/>
            <path d="M3357 943 c-4 -3 -7 -17 -7 -30 0 -16 -6 -23 -20 -23 -13 0 -20 -7
-20 -20 0 -13 7 -20 20 -20 18 0 20 -7 20 -62 0 -88 15 -112 68 -116 37 -3 42
-1 42 18 0 16 -5 20 -15 16 -35 -13 -45 4 -45 74 0 67 0 69 28 72 16 2 27 9
27 18 0 9 -11 16 -27 18 -24 3 -28 8 -28 33 0 21 -5 29 -18 29 -10 0 -22 -3
-25 -7z"/>
            <path d="M4090 920 c0 -25 -4 -30 -25 -30 -18 0 -25 -5 -25 -20 0 -15 7 -20
25 -20 24 0 25 -2 25 -71 0 -92 10 -109 60 -109 34 0 40 3 40 21 0 12 -5 19
-12 16 -7 -2 -21 0 -30 5 -14 7 -18 22 -18 73 l0 65 30 0 c23 0 30 4 30 20 0
16 -7 20 -30 20 -27 0 -30 3 -30 30 0 23 -4 30 -20 30 -16 0 -20 -7 -20 -30z"/>
            <path d="M2160 800 c0 -77 3 -93 20 -110 24 -24 77 -26 108 -4 20 14 22 14 22
0 0 -9 9 -16 20 -16 19 0 20 7 20 110 0 103 -1 110 -20 110 -18 0 -20 -7 -20
-59 0 -71 -12 -106 -40 -121 -17 -9 -26 -8 -46 5 -22 14 -24 22 -24 95 0 73
-2 80 -20 80 -19 0 -20 -7 -20 -90z"/>
            <path d="M2854 856 c-27 -27 -34 -42 -34 -73 0 -38 19 -76 50 -100 24 -20 104
-16 131 6 34 28 14 47 -27 26 -16 -8 -36 -15 -44 -15 -22 0 -60 29 -60 46 0
11 17 14 79 14 44 0 82 4 85 9 11 17 -7 71 -32 96 -19 19 -35 25 -69 25 -38 0
-50 -5 -79 -34z m118 -13 c34 -31 23 -43 -42 -43 -66 0 -79 15 -38 44 29 21
57 20 80 -1z"/>
            <path d="M3084 877 c-3 -8 -3 -56 -2 -108 3 -89 4 -94 26 -97 21 -3 22 0 22
68 0 90 10 110 56 110 43 0 54 -23 54 -116 0 -58 2 -64 21 -64 20 0 21 4 17
89 -2 70 -7 93 -22 110 -21 23 -74 28 -107 11 -13 -7 -19 -7 -19 0 0 15 -40
12 -46 -3z"/>
            <path d="M3510 780 c0 -103 1 -110 20 -110 19 0 20 7 20 110 0 103 -1 110 -20
110 -19 0 -20 -7 -20 -110z"/>
            <path d="M3661 876 c-49 -27 -65 -99 -36 -156 20 -38 43 -50 97 -50 55 0 88
21 70 43 -11 13 -16 13 -41 1 -35 -17 -74 -6 -91 27 -13 23 -7 74 11 96 13 15
67 17 89 3 9 -5 19 -5 27 2 19 15 16 23 -12 36 -34 16 -84 15 -114 -2z"/>
            <path d="M3863 878 c-13 -6 -23 -15 -23 -20 0 -15 31 -21 43 -9 15 15 60 14
75 -1 7 -7 12 -20 12 -30 0 -14 -8 -18 -41 -18 -41 0 -78 -14 -91 -34 -14 -20
-8 -63 10 -79 23 -21 81 -22 110 -1 20 14 22 14 22 -1 0 -10 7 -15 18 -13 15
3 17 16 17 92 -1 80 -3 90 -24 107 -26 21 -92 25 -128 7z m107 -133 c0 -17 -8
-29 -24 -36 -32 -15 -76 -4 -76 19 0 28 21 42 62 42 34 0 38 -3 38 -25z"/>
            <path d="M4272 862 c-38 -36 -47 -70 -32 -116 16 -50 53 -76 107 -76 38 0 50
5 79 34 28 28 34 42 34 76 0 34 -6 48 -34 76 -29 29 -41 34 -79 34 -36 0 -51
-6 -75 -28z m123 -26 c25 -18 27 -89 3 -113 -22 -22 -73 -21 -98 2 -25 22 -27
81 -3 107 19 22 71 24 98 4z"/>
            <path d="M4510 779 c0 -109 0 -110 23 -107 20 3 22 10 27 81 5 82 14 97 58 97
15 0 22 6 22 20 0 22 -28 27 -60 10 -13 -7 -21 -7 -25 0 -3 5 -15 10 -26 10
-18 0 -19 -8 -19 -111z"/>
        </g>
    </svg>
}

interface LoginProps {
    rawFeatures: string
    rawErrors: string
    hasServerSideErrors: boolean
    rawI18nMessages: string
    csrfName: string
    csrfToken: string
}

const Login: React.FC<LoginProps> = ({
                                         rawFeatures,
                                         rawErrors,
                                         hasServerSideErrors,
                                         rawI18nMessages,
                                         csrfName,
                                         csrfToken
                                     }) => {
    const features = JSON.parse(rawFeatures);
    const errorMessage = JSON.parse(rawErrors)["feedback"];
    const i18nMessages = JSON.parse(rawI18nMessages);
    const errorsBanner = <ErrorBanner errorMessage={errorMessage}/>

    const signUpLink = <div>
        <h3>{i18nMessages["signUpText"]} <a href="/sign-up">{i18nMessages["linkText"]}</a></h3>
    </div>
    const resetPasswordLink = <div>
        <h3>{i18nMessages["recoveryPasswordTextBeforeLink"]} <a
            href='/reset-password/reset-password-challenge-sender'>{i18nMessages["linkText"]}</a> {i18nMessages["recoveryPasswordTextAfterLink"]}
        </h3>
    </div>

    return (
        <ThemeProvider theme={theme}>

            <Template maxWidth="sm">
                <VAuthenticatorTitle/>
                <Grid style={{marginTop: '10px'}}>
                    <Divider/>
                </Grid>
                {hasServerSideErrors ? errorsBanner : ""}

                {<form action="login" method="post">
                    <Box>
                        <input name={csrfName} type="hidden" value={csrfToken}/>

                        <FormInputTextField id="username"
                                            label={i18nMessages["userNamePlaceholderText"]}
                                            type="email"
                                            suffix={<Person fontSize="large"/>}/>

                        <FormInputTextField id="password"
                                            label={i18nMessages["passwordPlaceholderText"]}
                                            type="password"
                                            suffix={<Fingerprint fontSize="large"/>}/>

                        <Separator/>

                        <FormButton type="submit" label={i18nMessages["submitButtonText"]}/>
                    </Box>


                    <Grid style={{marginTop: '10px'}}>
                        <Divider/>
                    </Grid>


                    <Grid style={{marginTop: '10px'}}>
                        {features.signup === true ? signUpLink : ""}
                        {features["reset-password"] === true ? resetPasswordLink : ""}
                    </Grid>
                </form>}
            </Template>
        </ThemeProvider>

    )
}

const features = getDataFromDomUtils('features')
const hasServerSideErrors = getDataFromDomUtils('hasServerSideErrors') === 'true'
const errors = getDataFromDomUtils('errors')
const i18nMessages = getDataFromDomUtils('i18nMessages')
const csrfName = getDataFromDomUtils('csrfName')
const csrfToken = getDataFromDomUtils('csrfToken')

ComponentInitializer(<Login csrfName={csrfName} csrfToken={csrfToken} rawFeatures={features} rawErrors={errors}
                            hasServerSideErrors={hasServerSideErrors} rawI18nMessages={i18nMessages}/>)
