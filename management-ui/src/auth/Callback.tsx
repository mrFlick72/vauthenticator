import React from 'react';

import {useEffect} from "react";
import {authenticate, getAuthorizeUrlFor, isInteractionRequired} from "./Authenticator";
import ComponentInitializer from '../utils/ComponentInitializer';

const Callback = () => {

    let queryString = location.search
    let params = new URLSearchParams(queryString)

    useEffect(() => {
        // a silent renewal (prompt=none) the authorization server answers with an interaction-required error:
        // only a login can continue, and returnTo still points at the page to come back to
        if (isInteractionRequired(params.get("error"))) {
            getAuthorizeUrlFor("login").then(url => window.location.href = url)
            return
        }
        let code = params.get("code")!!
        let sessionState = params.get("session_state")
        if (sessionState) {
            window.sessionStorage.setItem("SESSION_STATE", sessionState)
        }
        authenticate(code)
            .then(_ => {
                // a callback reached without a page to return to, such as a renewal from a fresh tab, lands home
                window.location.href = window.sessionStorage.getItem("returnTo") ?? "/secure/admin/index"
            })
    }, [])
    return <div></div>
}


ComponentInitializer(<Callback/>,)
