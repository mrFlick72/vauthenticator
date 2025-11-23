import React, {useRef, useState} from 'react';
import AdminTemplate from "../../component/AdminTemplate";
import vauthenticatorStyles from "../../theme/styles";
import {Alert, Snackbar, Typography} from "@mui/material";

import Separator from "../../component/Separator";
import FormButton from "../../component/FormButton";
import {getMailTemplateFor, saveMailTemplateFor} from "./MailTemplateRepository";
import FormSelect from "../../component/FormSelect";

const MailTemplatePage = () => {
    const pageTitle = "EMail Template Management"
    const classes = vauthenticatorStyles();

    let mailContent = useRef<HTMLDivElement>(null)
    const [mailType, setMailType] = useState("")
    const [openSucceeded, setOpenSucceeded] = React.useState(false);
    const [openFailure, setOpenFailure] = React.useState(false);

    const handleClose = () => {
        setOpenSucceeded(false);
        setOpenFailure(false);
    };

    let saveMailTemplate = () => {
        saveMailTemplateFor({
            emailType: mailType,
            body: mailContent?.current?.innerText
        })
            .then(r => {
                if (r.status === 204) {
                    setOpenSucceeded(true);
                } else {
                    setOpenFailure(false);
                }
            })
    };
    return (
        <AdminTemplate maxWidth="xl" page={pageTitle}>
            <Snackbar open={openSucceeded} autoHideDuration={6000}>
                <Alert onClose={handleClose} severity="success" sx={{width: '100%'}}>
                    EMail Template save succeeded
                </Alert>
            </Snackbar>
            <Snackbar open={openFailure} autoHideDuration={6000}>
                <Alert onClose={handleClose} severity="error" sx={{width: '100%'}}>
                    EMail Template save failed
                </Alert>
            </Snackbar>
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
                                     if(mailContent && mailContent.current) {
                                         mailContent.current.innerText = content.body
                                     }
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

            <div ref={mailContent} contentEditable="true" style={classes.contentEditor}/>

            <Separator/>

            <FormButton label="Save" type="button" onClickHandler={saveMailTemplate}/>
        </AdminTemplate>
    )
}

export default MailTemplatePage