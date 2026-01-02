import React, {useEffect} from "react";
import {endOfSession} from "./Authenticator";
import ComponentInitializer from "../utils/ComponentInitializer";
const Logout = () => {

    useEffect(() => {
        endOfSession().then()
    }, [])
    return <div></div>
}

ComponentInitializer(<Logout/>,)