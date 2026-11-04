import React, {useState} from 'react';
import AdminTemplate from "../../components/AdminTemplate";
import {Alert, Snackbar, Typography} from "@mui/material";

import Separator from "../../components/Separator";
import FormButton from "../../components/FormButton";
import {EMAIL_TEMPLATES, getMailTemplateFor, saveMailTemplateFor} from "./MailTemplateRepository";
import {failureMessage} from "../../utils/ApiError";
import ErrorToast from "../../components/ErrorToast";
import FormSelect from "../../components/FormSelect";
import TemplateEditor from "../../components/TemplateEditor";

const MailTemplatePage = () => {
    const pageTitle = "EMail Template Management"
    const [mailContent, setMailContent] = useState("")
    const [mailType, setMailType] = useState("")
    const [openSucceeded, setOpenSucceeded] = React.useState(false);
    const [loadFailure, setLoadFailure] = React.useState<string | null>(null);
    const [writeFailure, setWriteFailure] = React.useState<string | null>(null);

    const handleClose = () => {
        setOpenSucceeded(false);
    };

    let saveMailTemplate = () => {
        saveMailTemplateFor({
            emailType: mailType,
            body: mailContent
        })
            .then(() => setOpenSucceeded(true))
            .catch(error => setWriteFailure(failureMessage("save the email template", error, EMAIL_TEMPLATES)))
    };
    return (
        <AdminTemplate maxWidth="xl" page={pageTitle}>
            <Snackbar open={openSucceeded} autoHideDuration={6000}>
                <Alert onClose={handleClose} severity="success" sx={{width: '100%'}}>
                    EMail Template save succeeded
                </Alert>
            </Snackbar>
            <ErrorToast message={writeFailure} onClose={() => setWriteFailure(null)}/>
            <Typography variant="h2">
                EMail Template
            </Typography>

            <FormSelect id="email-template-type"
                        label="EMail Template Type"
                        multi={false}
                        onChangeHandler={(event) => {
                             setMailType(event.value)
                             getMailTemplateFor(event.value)
                                 .then(content => {
                                     setMailContent(content.body)
                                     setLoadFailure(null)
                                 })
                                 .catch(error => {
                                     setMailContent("")
                                     setLoadFailure(failureMessage("load the email template", error, EMAIL_TEMPLATES))
                                 })
                         }}
                        options={[
                             {value: "WELCOME", label: "WELCOME"},
                             {value: "EMAIL_VERIFICATION", label: "EMAIL_VERIFICATION"},
                             {value: "RESET_PASSWORD", label: "RESET_PASSWORD"},
                             {value: "MFA", label: "MFA"}
                         ]}
            />

            <Separator/>

            {/* a template that failed to load is not shown, so it cannot be saved over the stored one */}
            {loadFailure
                ? <Alert severity="error">{loadFailure}</Alert>
                : <>
                <TemplateEditor label="EMail Template" language="html" value={mailContent} onChange={setMailContent}/>

                <Separator/>

                <FormButton label="Save" type="button" onClickHandler={saveMailTemplate}/>
                </>}
        </AdminTemplate>
    )
}

export default MailTemplatePage