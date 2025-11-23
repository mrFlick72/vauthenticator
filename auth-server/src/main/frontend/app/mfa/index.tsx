import React from "react";

import getDataFromDomUtils from "../utils/getDataFromDomUtils";
import ComponentInitializer from "../utils/ComponentInitializer";
import MfaChallengePage from "./MfaChallengePage";

const errors = getDataFromDomUtils('errors')
const hasServerSideErrors = getDataFromDomUtils('hasServerSideErrors') === "true"
const i18nMessages = getDataFromDomUtils('i18nMessages')
const csrfName = getDataFromDomUtils('csrfName')
const csrfToken = getDataFromDomUtils('csrfToken')


ComponentInitializer(<MfaChallengePage csrfName={csrfName} csrfToken={csrfToken} rawErrors={errors}
                                       hasServerSideErrors={hasServerSideErrors} rawI18nMessages={i18nMessages}/>)