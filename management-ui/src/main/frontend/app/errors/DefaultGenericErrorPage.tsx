import React from "react";

import {ThemeProvider, Typography} from "@mui/material";
import {SentimentVeryDissatisfied} from "@mui/icons-material";
import getDataFromDomUtils from "../utils/getDataFromDomUtils";
import ComponentInitializer from "../utils/ComponentInitializer";
import Template from "../component/Template";
import themeProvider from "../theme/ThemeProvider";

interface DefaultGenericErrorPageProps {
    messages: string
}

interface ErrorMessage {
    defaultMessage: string
}

const DefaultGenericErrorPage: React.FC<DefaultGenericErrorPageProps> = ({messages}) => {
    let errors: ErrorMessage = JSON.parse(messages)
    return <ThemeProvider theme={themeProvider}>
        <Template maxWidth="sm">
            <Typography variant="h3" component="h3">
                <SentimentVeryDissatisfied fontSize="large"/> {errors.defaultMessage}
            </Typography>
        </Template>
    </ThemeProvider>

}

let errors: string = getDataFromDomUtils('errors')
let page = <DefaultGenericErrorPage messages={errors}/>;

ComponentInitializer(page);