import React, {useEffect} from "react";
import ComponentInitializer from "../utils/ComponentInitializer";

type SessionManagementConfiguration = {
    clientId: string
    sessionState: string
    opIframeId: string
    opIframeOrigin: string
    pollIntervalMs: number
}

type ParentMessage = {
    type: "start" | "stop"
    configuration?: SessionManagementConfiguration
}

type IframeStatus = "changed" | "unchanged" | "error"

// todo WIP
const SessionManagementRPFrame = () => {
    useEffect(() => {
        let configuration: SessionManagementConfiguration | null = null
        let timerId: number | null = null

        const stopPolling = () => {
            if (timerId !== null) {
                window.clearInterval(timerId)
                timerId = null
            }
        }

        const getOpIframeWindow = (): Window | null => {
            if (!configuration) {
                return null
            }

            const opIframe = window.parent.document.getElementById(configuration.opIframeId) as HTMLIFrameElement | null
            return opIframe?.contentWindow ?? null
        }

        const checkSession = () => {
            if (!configuration) {
                return
            }

            const opIframeWindow = getOpIframeWindow()
            if (!opIframeWindow) {
                return
            }

            const message = `${configuration.clientId} ${configuration.sessionState}`
            opIframeWindow.postMessage(message, configuration.opIframeOrigin)
        }

        const startPolling = (nextConfiguration: SessionManagementConfiguration) => {
            configuration = nextConfiguration
            stopPolling()
            checkSession()
            timerId = window.setInterval(checkSession, configuration.pollIntervalMs)
        }

        const notifyParent = (status: IframeStatus) => {
            window.parent.postMessage(status, window.location.origin)
        }

        const receiveMessage = (event: MessageEvent) => {
            if (configuration && event.origin === configuration.opIframeOrigin) {
                const status = event.data
                if (status === "changed" || status === "unchanged" || status === "error") {
                    if (status !== "unchanged") {
                        stopPolling()
                    }

                    notifyParent(status)
                }
                return
            }

            if (event.origin !== window.location.origin) {
                return
            }

            const message = event.data as ParentMessage | null
            if (!message) {
                return
            }

            if (message.type === "stop") {
                configuration = null
                stopPolling()
                return
            }

            if (message.type === "start" && message.configuration) {
                startPolling(message.configuration)
            }
        }

        window.addEventListener("message", receiveMessage)

        return () => {
            stopPolling()
            window.removeEventListener("message", receiveMessage)
        }
    }, [])

    return <div>
        <iframe >

        </iframe>
    </div>
}

ComponentInitializer(<SessionManagementRPFrame/>)
