import React, {useEffect, useMemo, useState} from "react";
import {applicationConfigLoader} from "../config/ConfigLoader";

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
    var mes = ${JSON.stringify(message)};
    var targetOrigin = ${JSON.stringify(targetOrigin)};
    var parentOrigin = ${JSON.stringify(parentOrigin)};
    var opFrameId = ${JSON.stringify(OP_IFRAME_ID)};
    var timerID;

      var stat = "unchanged";
    var mes = ${JSON.stringify(message)};
    var targetOrigin = ${JSON.stringify(targetOrigin)};
    var parentOrigin = ${JSON.stringify(parentOrigin)};
    var opFrameId = ${JSON.stringify(OP_IFRAME_ID)};
    var timerID;

    function notifyParent(status) {
        window.parent.postMessage({
            type: ${JSON.stringify(SESSION_MANAGEMENT_EVENT_TYPE)},
            status: status
        }, parentOrigin);
    }

    function check_session() {
        var frame = window.parent.document.getElementById(opFrameId);
        var win = frame && frame.contentWindow ? frame.contentWindow : window.parent.frames[opFrameId];

        if (win) {
            try {
                win.postMessage(mes, targetOrigin);
            } catch (e) {
                console.error(" win.postMessage(mes, targetOrigin); failed: ");
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
        if (e.origin !== targetOrigin) {
            return;
        }

        stat = e.data;

        if (stat === "changed" || stat === "error") {
            clearInterval(timerID);
            console.log("Session state " + stat);
            // notifyParent(stat);
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
        const receiveMessage = (event: MessageEvent<unknown>) => {
            if (event.origin !== window.location.origin || !isSessionManagementEvent(event.data)) {
                return;
            }

            if (event.data.status === "changed") {
                onSessionChanged?.();
            }

            if (event.data.status === "error") {
                onSessionError?.();
            }
        };

        window.addEventListener("message", receiveMessage, false);
        return () => window.removeEventListener("message", receiveMessage, false);
    }, [onSessionChanged, onSessionError]);

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

    if (!iframeData) {
        return null;
    }

    return (
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
    );
};

export default SessionManagement;
