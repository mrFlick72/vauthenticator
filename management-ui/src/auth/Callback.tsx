import React from 'react';

import {useEffect} from "react";
import {authenticate} from "./Authenticator";
import ComponentInitializer from '../utils/ComponentInitializer';

const Callback = () => {

    let queryString = location.search
    let params = new URLSearchParams(queryString)

    useEffect(() => {
        let code = params.get("code")!!
        let sessionState = params.get("session_state")
        if (sessionState) {
            window.sessionStorage.setItem("SESSION_STATE", sessionState)
        }
        authenticate(code)
            .then(_ => {
                window.location.href = window.sessionStorage.getItem("returnTo")!!
            })
    }, [])
    return <div></div>
}


ComponentInitializer(<Callback/>,)
