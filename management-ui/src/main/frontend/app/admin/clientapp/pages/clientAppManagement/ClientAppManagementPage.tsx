import React, {useEffect, useState} from 'react';
import {useNavigate, useParams} from "react-router";
import {
    findClientApplicationFor,
    newClientApplicationRandomSecret,
    saveClientApplicationFor
} from "../../repository/ClientAppRepository";
import FormInputTextField from "../../../../component/FormInputTextField";
import AdminTemplate from "../../../../component/AdminTemplate";
import Separator from "../../../../component/Separator";
import FormButton from "../../../../component/FormButton";
import TabPanel from "../../../../component/TabPanel";
import LeftRightComponentRow from "../../../../component/LeftRightComponentRow";
import CheckboxesGroup from "../../../../component/CheckboxesGroup";
import {
    AuthorizedGrantType,
    authorizedGrantTypesParam,
    authorizedGrantTypesRegistry,
    ClientAppAuthorizedGrantType
} from "./AuthorizedGrantTypes";
import vauthenticatorStyles from "../../../../theme/styles";
import FormSelect, {SelectOption} from "../../../../component/FormSelect";
import {findAllScopes} from "../../repository/ScopeRepository";
import {Alert, Box, Card, CardContent, CardHeader, Snackbar, Tab, Tabs, Typography} from "@mui/material";
import {Apps, Autorenew, ContentCopy} from "@mui/icons-material";
import randomClientApplicationIdGenerator from "./RandomClientApplicationIdGenerator";
import AddCircleIcon from '@mui/icons-material/AddCircle';
import copy from "copy-to-clipboard";
import { ClientAppManagementPageTypes } from './ClientAppManagementPageTypes';
import {ClientApplicationDetails} from "../../repository/ClientAppApiTypes";

function newClientApplicationPageDetails(
    clientAppId: string | undefined,
    authorizedGrantType: ClientAppAuthorizedGrantType
): ClientAppManagementPageTypes {
    return {
        clientApplicationId: clientAppId,
        clientAppName: "",
        secret: "",
        applicationType: CONFIDENTIAL_CLIENT_APP_TYPE,
        scopes: [],
        authorizedGrantTypes: authorizedGrantType,
        webServerRedirectUri: "",
        allowedOrigins: "",
        withPkce: false,
        accessTokenValidity: "",
        refreshTokenValidity: "",
        postLogoutRedirectUri: "",
        logoutUri: ""
    }
}

function allProps(index: string) {
    return {
        id: `vertical-tab-${index}`,
        'aria-controls': `vertical-tabpanel-${index}`,
    };
}

const CONFIDENTIAL_CLIENT_APP_TYPE = {value: "true", label: "CONFIDENTIAL"}
const PUBLIC_CLIENT_APP_TYPE = {value: "false", label: "PUBLIC"}

function isClientApplicationConfidential(applicationType: { value: string; label: string }) {
    return applicationType.value === "true";
}

const ClientAppManagementPage = () => {
    const classes = vauthenticatorStyles();
    let {clientAppId} = useParams();
    const storePassword = !clientAppId
    const navigate = useNavigate();

    const [clientApplication, setClientApplication] = useState<ClientAppManagementPageTypes>(newClientApplicationPageDetails(clientAppId, authorizedGrantTypesRegistry([])));

    const [availableClientApplicationTypes] = useState<SelectOption[]>([CONFIDENTIAL_CLIENT_APP_TYPE, PUBLIC_CLIENT_APP_TYPE])
    const [availableScopes, setAvailableScopes] = useState<SelectOption[]>([])
    const [openSecretAlert, setOpenSecretAlert] = useState(false)
    const [tab, setTab] = React.useState('0');
    const handleTabChange = (event: React.SyntheticEvent, newValue: any) => {
        setTab(newValue);
    };


    const generateRandomClientApplicationId = () => {
        setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
            let copiedClientApplication = Object.assign({}, clientApplication)
            copiedClientApplication.clientApplicationId = randomClientApplicationIdGenerator()
            return copiedClientApplication
        })
    }

    const generateRandomClientApplicationIdItem = <AddCircleIcon onClick={generateRandomClientApplicationId}/>


    const generateRandomClientApplicationSecret = async () => {
        let randomSecret = await newClientApplicationRandomSecret();
        setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
            let copiedClientApplication = Object.assign({}, clientApplication)
            copiedClientApplication.secret = randomSecret.pwd
            return copiedClientApplication
        })

        setOpenSecretAlert(true)
    }

    const generateRandomClientApplicationSecretItem = <Autorenew onClick={generateRandomClientApplicationSecret}/>

    const handleCloseSecretAlert = () => {
        setOpenSecretAlert(false)
    }
    let maskedContentWith = (content: string, mask: string = "*") => content.replace(/./g, mask);

    const saveClientApp = () => {
        let clientApp: ClientApplicationDetails = {
            clientAppName: clientApplication.clientAppName,
            secret: isClientApplicationConfidential(clientApplication.applicationType) ? clientApplication.secret : "",
            withPkce: clientApplication.withPkce,
            confidential: isClientApplicationConfidential(clientApplication.applicationType),
            storePassword: storePassword,
            scopes: clientApplication.scopes.map(scope => scope.value),
            authorizedGrantTypes: authorizedGrantTypesParam(clientApplication.authorizedGrantTypes),
            webServerRedirectUri: clientApplication.webServerRedirectUri,
            accessTokenValidity: clientApplication.accessTokenValidity,
            refreshTokenValidity: clientApplication.refreshTokenValidity,
            postLogoutRedirectUri: clientApplication.postLogoutRedirectUri,
            allowedOrigins: clientApplication.allowedOrigins.split(","),
            logoutUri: clientApplication.logoutUri
        }

        saveClientApplicationFor(clientApplication.clientApplicationId!!, clientApp)
            .then(response => {
                if (response.status === 204) {
                    navigate(-1);
                }
            })
    }

    useEffect(() => {
        async function init() {
            let scopes = await findAllScopes()
            setAvailableScopes(scopes.map(scope => {
                return {value: scope, label: scope}
            }))

            let clientApp = await findClientApplicationFor(clientApplication.clientApplicationId!!)

            setClientApplication(
                {
                    clientApplicationId: clientApplication.clientApplicationId,
                    clientAppName: clientApp.clientAppName,
                    secret: clientApp.secret,
                    applicationType: CONFIDENTIAL_CLIENT_APP_TYPE,
                    scopes: clientApp.scopes.map(scope => {
                        return {value: scope, label: scope} as SelectOption;
                    }),
                    authorizedGrantTypes: authorizedGrantTypesRegistry(clientApp.authorizedGrantTypes as AuthorizedGrantType[]),
                    webServerRedirectUri: clientApp.webServerRedirectUri,
                    allowedOrigins: clientApp.allowedOrigins.join(","),
                    withPkce: clientApp.withPkce,
                    accessTokenValidity: clientApp.accessTokenValidity,
                    refreshTokenValidity: clientApp.refreshTokenValidity,
                    postLogoutRedirectUri: clientApp.postLogoutRedirectUri,
                    logoutUri: clientApp.logoutUri,
                }
            )
        }

        init().then()
    }, [])

    let pageTitle = clientApplication.clientApplicationId ? `: Client Application: ${clientApplication.clientApplicationId}` : "";
    return <AdminTemplate maxWidth="xl" page={pageTitle}>
        <Snackbar open={openSecretAlert}>
            <Alert onClose={handleCloseSecretAlert} icon={false} severity="success" sx={{whiteSpace: 'pre-line'}}>
                <span>Secret: {maskedContentWith(clientApplication.secret)} <ContentCopy sx={{margin: "4px 8px -4px 0"}}
                                                                                         onClick={async () => {
                                                                                             if (('clipboard' in navigator)) {
                                                                                                 await navigator.clipboard.writeText(maskedContentWith(clientApplication.secret))
                                                                                             } else {
                                                                                                 copy(clientApplication.secret)
                                                                                             }
                                                                                         }}/> </span>
            </Alert>
        </Snackbar>

        <Typography variant="h3" component="h3">
            <Apps fontSize="large"/> Client Application: {clientApplication.clientApplicationId}
        </Typography>

        <Box style={classes.tabs}>
            <Tabs value={tab}
                  orientation="vertical"
                  onChange={handleTabChange}
                  aria-label="wrapped label tabs example">
                <Tab value="0"
                     label="1) Client Application Credentials Section"
                     wrapped
                     {...allProps('0')}
                />
                <Tab value="1"
                     label="2) Client Application Permission Definition Section"
                     {...allProps('1')} />

                <Tab value="2"
                     label="3) Client Application Uri Section"
                     {...allProps('2')} />
            </Tabs>
            <TabPanel value={tab} index={'0'}>
                <Card>
                    <CardHeader title="Client Application base definition" color="textSecondary"/>
                    <CardContent>
                        <FormInputTextField id="clientAppId"
                                            label="Client Application Id"
                                            required={true}
                                            handler={(value) => {
                                                setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                                    let copiedClientApplication = Object.assign({}, clientApplication)
                                                    copiedClientApplication.clientApplicationId = value.target.value
                                                    return copiedClientApplication
                                                })
                                            }}
                                            suffixItem={generateRandomClientApplicationIdItem}
                                            value={clientApplication.clientApplicationId || ""}/>

                        <FormSelect id="applicationType"
                                    label="Application Type"
                                    multi={false}
                                    onChangeHandler={(event) => {
                                        setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                            let copiedClientApplication = Object.assign({}, clientApplication)
                                            copiedClientApplication.applicationType = event
                                            return copiedClientApplication
                                        })
                                    }}
                                    options={availableClientApplicationTypes}
                                    value={clientApplication.applicationType}/>

                        {isClientApplicationConfidential(clientApplication.applicationType) &&
                            <FormInputTextField id="secret"
                                                label="Password"
                                                required={true}
                                                type="Password"
                                                disabled={clientAppId != undefined}
                                                handler={(value) => {
                                                    setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                                        let copiedClientApplication = Object.assign({}, clientApplication)
                                                        copiedClientApplication.secret = value.target.value
                                                        return copiedClientApplication
                                                    })
                                                }}
                                                suffixItem={clientAppId != undefined ?
                                                    <div/> : generateRandomClientApplicationSecretItem}
                                                value={clientApplication.secret}/>}


                        <FormInputTextField id="clientAppName"
                                            label="Client Application Displayed Name"
                                            required={true}
                                            handler={(value) => {
                                                setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                                    let copiedClientApplication = Object.assign({}, clientApplication)
                                                    copiedClientApplication.clientAppName = value.target.value
                                                    return copiedClientApplication
                                                })
                                            }}
                                            value={clientApplication.clientAppName}/>
                    </CardContent>
                </Card>

                <Separator/>

                <LeftRightComponentRow leftComponentColumnsSize={2}
                                       leftComponents={<FormButton label="Next Tab"
                                                                   onClickHandler={() => setTab('1')}/>}
                                       rightComponentsColumnSize={2}
                                       rightComponents={<FormButton label="Save Client Application" direction="rtl"
                                                                    onClickHandler={saveClientApp}/>}/>
            </TabPanel>

            <TabPanel value={tab} index={'1'}>
                <Card>
                    <CardContent>
                        <CardHeader title="Client Application permission specification" color="textSecondary"/>

                        <FormSelect id="scopes"
                                    label="Scopes"
                                    multi={true}
                                    onChangeHandler={(event) => {
                                        setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                            let copiedClientApplication = Object.assign({}, clientApplication)
                                            copiedClientApplication.scopes = event
                                            return copiedClientApplication
                                        })
                                    }}
                                    options={availableScopes}
                                    value={clientApplication.scopes}/>

                        <CheckboxesGroup id="withPkce"
                                         handler={(value) => {
                                             setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                                 let copiedClientApplication = Object.assign({}, clientApplication)
                                                 copiedClientApplication.withPkce = value.target.checked
                                                 return copiedClientApplication
                                             })
                                         }}
                                         choicesRegistry={{
                                             with_pkce: clientApplication.withPkce
                                         }}
                                         legend="Enable/Disable PKCE"/>

                        <CheckboxesGroup id="authorizedGrantTypes"
                                         handler={(value) => {
                                             setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                                 let copiedClientApplication = Object.assign({}, clientApplication)
                                                 copiedClientApplication.authorizedGrantTypes = {
                                                     ...clientApplication.authorizedGrantTypes,
                                                     [value.target.name]: value.target.checked
                                                 }
                                                 return copiedClientApplication
                                             })
                                         }}
                                         choicesRegistry={clientApplication.authorizedGrantTypes}
                                         legend="Authorized Grant Types"/>


                        <FormInputTextField id="accessTokenValidity"
                                            label="Access Token Validity"
                                            required={true}
                                            handler={(value) => {
                                                setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                                    let copiedClientApplication = Object.assign({}, clientApplication)
                                                    copiedClientApplication.accessTokenValidity = value.target.value
                                                    return copiedClientApplication
                                                })
                                            }}
                                            value={clientApplication.accessTokenValidity}/>

                        <FormInputTextField id="refreshTokenValidity"
                                            label="Refresh Token Validity"
                                            required={true}
                                            handler={(value) => {
                                                setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                                    let copiedClientApplication = Object.assign({}, clientApplication)
                                                    copiedClientApplication.refreshTokenValidity = value.target.value
                                                    return copiedClientApplication
                                                })
                                            }}
                                            value={clientApplication.refreshTokenValidity}/>

                    </CardContent>
                </Card>
                <Separator/>

                <LeftRightComponentRow leftComponentColumnsSize={2}
                                       leftComponents={
                                           <div style={{
                                               flex: "0 0 auto",
                                               display: "flex",
                                           }}>
                                               <FormButton label="Previous Tab"
                                                           onClickHandler={() => setTab('0')}/>
                                               <FormButton label="Next Tab" onClickHandler={() => setTab('2')}/>
                                           </div>
                                       }
                                       rightComponentsColumnSize={2}
                                       rightComponents={<FormButton label="Save Client Application" direction="rtl"
                                                                    onClickHandler={saveClientApp}/>}/>

            </TabPanel>
            <TabPanel value={tab} index={'2'}>
                <Card>
                    <CardContent>
                        <CardHeader title="Client Application urls definitions" color="textSecondary"/>

                        <FormInputTextField id="allowedOrigins"
                                            label="Allowed Origins"
                                            required={true}
                                            handler={(value) => {
                                                setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                                    let copiedClientApplication = Object.assign({}, clientApplication)
                                                    copiedClientApplication.allowedOrigins = value.target.value
                                                    return copiedClientApplication
                                                })
                                            }}
                                            value={clientApplication.allowedOrigins}/>

                        <FormInputTextField id="webServerRedirectUri"
                                            label="Web Server Redirect Uri"
                                            required={true}
                                            handler={(value) => {
                                                setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                                    let copiedClientApplication = Object.assign({}, clientApplication)
                                                    copiedClientApplication.webServerRedirectUri = value.target.value
                                                    return copiedClientApplication
                                                })
                                            }}
                                            value={clientApplication.webServerRedirectUri}/>

                        <FormInputTextField id="postLogoutRedirectUri"
                                            label="Post Logout Redirect Uri"
                                            required={true}
                                            handler={(value) => {
                                                setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                                    let copiedClientApplication = Object.assign({}, clientApplication)
                                                    copiedClientApplication.postLogoutRedirectUri = value.target.value
                                                    return copiedClientApplication
                                                })
                                            }}
                                            value={clientApplication.postLogoutRedirectUri}/>

                        <FormInputTextField id="logoutUri"
                                            label="Logout Uri"
                                            required={true}
                                            handler={(value) => {
                                                setClientApplication((clientApplication: ClientAppManagementPageTypes) => {
                                                    let copiedClientApplication = Object.assign({}, clientApplication)
                                                    copiedClientApplication.logoutUri = value.target.value
                                                    return copiedClientApplication
                                                })
                                            }}
                                            value={clientApplication.logoutUri}/>
                    </CardContent>
                </Card>
                <Separator/>
                <LeftRightComponentRow leftComponentColumnsSize={2}
                                       leftComponents={<FormButton label="Previous Tab"
                                                                   onClickHandler={() => setTab('1')}/>}
                                       rightComponentsColumnSize={2}
                                       rightComponents={<FormButton label="Save Client Application" direction="rtl"
                                                                    onClickHandler={saveClientApp}/>}/>
            </TabPanel>
        </Box>
    </AdminTemplate>
}

export default ClientAppManagementPage