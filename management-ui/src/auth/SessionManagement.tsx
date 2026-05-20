import React, {useEffect, useMemo, useState} from "react";
import {Box, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle} from "@mui/material";
import {Security} from "@mui/icons-material";
import {applicationConfigLoader} from "../config/ConfigLoader";
import {checkOfSession, invalidateSession, isAuthenticated} from "./Authenticator";

const SESSION_STATE_STORAGE_KEY = "SESSION_STATE";
const OP_IFRAME_ID = "op";
const DEFAULT_CHECK_INTERVAL_MS = 5 * 1000;
const SESSION_MANAGEMENT_EVENT_TYPE = "oidc-session-management";

type SessionManagementStatus = "changed" | "error";

type SessionManagementEvent = {
    type: typeof SESSION_MANAGEMENT_EVENT_TYPE;
    status: SessionManagementStatus;
};

type SessionManagementProps = {
    onSessionChanged?: () => void;
    onSessionError?: () => void;
};

type SessionManagementConfig = {
    clientApplicationId: string;
    idpBaseUrl: string;
};

const isSessionManagementEvent = (data: unknown): data is SessionManagementEvent => {
    if (typeof data !== "object" || data === null) {
        return false;
    }

    const event = data as Partial<SessionManagementEvent>;
    return event.type === SESSION_MANAGEMENT_EVENT_TYPE &&
        (event.status === "changed" || event.status === "error");
};

const withoutTrailingSlash = (value: string) => value.replace(/\/$/, "");

const originFor = (url: string) => new URL(url).origin;

const buildRpIframeDocument = (
    clientId: string,
    sessionState: string,
    targetOrigin: string,
    parentOrigin: string
) => {
    const message = `${clientId} ${sessionState}`;

    return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>RP Session Management IFrame</title>
</head>
<body>
<script>
    var stat = "unchanged";
    var eventType = ${JSON.stringify(SESSION_MANAGEMENT_EVENT_TYPE)};
    var mes = ${JSON.stringify(message)};
    var targetOrigin = ${JSON.stringify(targetOrigin)};
    var parentOrigin = ${JSON.stringify(parentOrigin)};
    var opFrameId = ${JSON.stringify(OP_IFRAME_ID)};
    var timerID;

    function check_session() {
        var frame = window.parent.document.getElementById(opFrameId);
        var win = frame && frame.contentWindow ? frame.contentWindow : window.parent.frames[opFrameId];

        if (win) {
            try {
                win.postMessage(mes, targetOrigin);
            } catch (e) {
                console.error("win.postMessage(mes, targetOrigin); failed: ");
                console.error(e);
            }
        }
    }

    function setTimer() {
        timerID = setInterval(check_session, ${DEFAULT_CHECK_INTERVAL_MS});
        window.setTimeout(check_session, 0);
    }

    window.addEventListener("message", receiveMessage, false);

    function receiveMessage(e) {
        console.log("Received message", e);
        if (e.origin !== targetOrigin) {
            return;
        }

        stat = e.data;

        if (stat === "changed" || stat === "error") {
            clearInterval(timerID);
            console.log("Session state " + stat);
            window.parent.postMessage({
                type: eventType,
                status: stat
            }, parentOrigin);
        }
    }

    setTimer();
</script>
</body>
</html>`;
};

const SessionManagement: React.FC<SessionManagementProps> = ({onSessionChanged, onSessionError}) => {
    const [config, setConfig] = useState<SessionManagementConfig | null>(null);
    const [sessionState, setSessionState] = useState<string | null>(() =>
        window.sessionStorage.getItem(SESSION_STATE_STORAGE_KEY)
    );
    const [sessionExpired, setSessionExpired] = useState(false);

    useEffect(() => {
        let active = true;

        applicationConfigLoader()
            .then((applicationConfig) => {
                if (!active || !applicationConfig.clientApplicationId || !applicationConfig.idpBaseUrl) {
                    return;
                }

                setConfig({
                    clientApplicationId: applicationConfig.clientApplicationId,
                    idpBaseUrl: applicationConfig.idpBaseUrl,
                });
                setSessionState(window.sessionStorage.getItem(SESSION_STATE_STORAGE_KEY));
            });

        return () => {
            active = false;
        };
    }, []);

    useEffect(() => {
        let active = true;

        const handleSessionStatus = async (status: SessionManagementStatus) => {
            const isActive = await checkOfSession();
            if (!active || isActive) {
                return;
            }

            if (status === "changed") {
                invalidateSession();
                setSessionExpired(true);
            }

            if (status === "error") {
                invalidateSession();
                setSessionExpired(true);
            }
        };

        const receiveMessage = (event: MessageEvent<unknown>) => {
            console.log("Received message in RP iframe", event);
            if (event.origin !== window.location.origin || !isSessionManagementEvent(event.data)) {
                return;
            }

            handleSessionStatus(event.data.status).then();
        };

        window.addEventListener("message", receiveMessage, false);
        return () => {
            active = false;
            window.removeEventListener("message", receiveMessage, false);
        };
    }, [onSessionChanged, onSessionError]);

    const relogin = () => {
        isAuthenticated().then();
    };

    const iframeData = useMemo(() => {
        if (!config || !sessionState) {
            return null;
        }

        const idpBaseUrl = withoutTrailingSlash(config.idpBaseUrl);
        const targetOrigin = originFor(idpBaseUrl);

        return {
            opIframeUrl: `${idpBaseUrl}/session/management`,
            rpIframeDocument: buildRpIframeDocument(
                config.clientApplicationId,
                sessionState,
                targetOrigin,
                window.location.origin
            ),
        };
    }, [config, sessionState]);

    return (
        <>
            {iframeData ? (
                <>
                    <iframe
                        hidden
                        id={OP_IFRAME_ID}
                        name={OP_IFRAME_ID}
                        src={iframeData.opIframeUrl}
                        title="OIDC OP session management"
                    />
                    <iframe
                        hidden
                        srcDoc={iframeData.rpIframeDocument}
                        title="OIDC RP session management"
                    />
                </>
            ) : null}

            <Dialog
                aria-labelledby="session-expired-dialog-title"
                aria-describedby="session-expired-dialog-description"
                open={sessionExpired}
                maxWidth="sm"
            >
                <DialogTitle id="session-expired-dialog-title">
                    <Box component="span" sx={{display: "flex", alignItems: "center", gap: 1.5}}>
                        <Box
                            component="span"
                            sx={{
                                alignItems: "center",
                                bgcolor: "primary.main",
                                borderRadius: "50%",
                                color: "primary.contrastText",
                                display: "inline-flex",
                                height: 36,
                                justifyContent: "center",
                                width: 36,
                            }}
                        >
                            <Security aria-hidden="true" fontSize="small"/>
                        </Box>
                        <span>Session expired</span>
                    </Box>
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="session-expired-dialog-description">
                        Your session is over. Log in again to continue.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button variant="contained" color="primary" onClick={relogin}>
                        Log in again
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
};

export default SessionManagement;
