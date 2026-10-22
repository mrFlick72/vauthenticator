import React, {useRef, useState} from 'react';
import AdminTemplate from "../../components/AdminTemplate";
import vauthenticatorStyles from "../../theme/styles";
import {Alert, Snackbar, Typography} from "@mui/material";

import Separator from "../../components/Separator";
import FormButton from "../../components/FormButton";
import {getSmsTemplateFor, saveSmsTemplateFor} from "./SmsTemplateRepository";
import FormSelect from "../../components/FormSelect";

const SmsTemplatePage = () => {
    const pageTitle = "SMS Template Management"
    const classes = vauthenticatorStyles();

    let smsContent = useRef<HTMLDivElement>(null)
    const [smsType, setSmsType] = useState("")
    const [openSucceeded, setOpenSucceeded] = React.useState(false);
    const [openFailure, setOpenFailure] = React.useState(false);

    const handleClose = () => {
        setOpenSucceeded(false);
        setOpenFailure(false);
    };

    let saveSmsTemplate = () => {
        saveSmsTemplateFor({
            smsType: smsType,
            body: smsContent?.current?.innerText
        })
            .then(r => {
                if (r.status === 204) {
                    setOpenSucceeded(true);
                } else {
                    setOpenFailure(true);
                }
            })
    };
    return (
        <AdminTemplate maxWidth="xl" page={pageTitle}>
            <Snackbar open={openSucceeded} autoHideDuration={6000}>
                <Alert onClose={handleClose} severity="success" sx={{width: '100%'}}>
                    SMS Template save succeeded
                </Alert>
            </Snackbar>
            <Snackbar open={openFailure} autoHideDuration={6000}>
                <Alert onClose={handleClose} severity="error" sx={{width: '100%'}}>
                    SMS Template save failed
                </Alert>
            </Snackbar>
            <Typography variant="h2">
                SMS Template
            </Typography>

            <FormSelect id="sms-template-type"
                        label="SMS Template Type"
                        multi={false}
                        onChangeHandler={(event) => {
                             setSmsType(event.value)
                             getSmsTemplateFor(event.value)
                                 .then(content => {
                                     if(smsContent && smsContent.current) {
                                         smsContent.current.innerText = content.body
                                     }
                                 })
                         }}
                        options={[
                             {value: "MFA", label: "MFA"}
                         ]}
            />

            <Separator/>

            <div ref={smsContent} contentEditable="true" style={classes.contentEditor}/>

            <Separator/>

            <FormButton label="Save" type="button" onClickHandler={saveSmsTemplate}/>
        </AdminTemplate>
    )
}

export default SmsTemplatePage
