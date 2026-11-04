import React, {useState} from 'react';
import AdminTemplate from "../../components/AdminTemplate";
import {Alert, Snackbar, Typography} from "@mui/material";

import Separator from "../../components/Separator";
import FormButton from "../../components/FormButton";
import {getSmsTemplateFor, smsTemplatesMessageFor, saveSmsTemplateFor} from "./SmsTemplateRepository";
import ErrorToast from "../../components/ErrorToast";
import FormSelect from "../../components/FormSelect";
import TemplateEditor from "../../components/TemplateEditor";

const SmsTemplatePage = () => {
    const pageTitle = "SMS Template Management"
    const [smsContent, setSmsContent] = useState("")
    const [smsType, setSmsType] = useState("")
    const [openSucceeded, setOpenSucceeded] = React.useState(false);
    const [loadFailure, setLoadFailure] = React.useState<string | null>(null);
    const [writeFailure, setWriteFailure] = React.useState<string | null>(null);

    const handleClose = () => {
        setOpenSucceeded(false);
    };

    let saveSmsTemplate = () => {
        saveSmsTemplateFor({
            smsType: smsType,
            body: smsContent
        })
            .then(() => setOpenSucceeded(true))
            .catch(error => setWriteFailure(smsTemplatesMessageFor(error)))
    };
    return (
        <AdminTemplate maxWidth="xl" page={pageTitle}>
            <Snackbar open={openSucceeded} autoHideDuration={6000}>
                <Alert onClose={handleClose} severity="success" sx={{width: '100%'}}>
                    SMS Template save succeeded
                </Alert>
            </Snackbar>
            <ErrorToast message={writeFailure} onClose={() => setWriteFailure(null)}/>
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
                                     setSmsContent(content.body)
                                     setLoadFailure(null)
                                 })
                                 .catch(error => {
                                     setSmsContent("")
                                     setLoadFailure(smsTemplatesMessageFor(error))
                                 })
                         }}
                        options={[
                             {value: "MFA", label: "MFA"}
                         ]}
            />

            <Separator/>

            {/* a template that failed to load is not shown, so it cannot be saved over the stored one */}
            {loadFailure
                ? <Alert severity="error">{loadFailure}</Alert>
                : <>
                <TemplateEditor label="SMS Template" language="text" value={smsContent} onChange={setSmsContent}/>

                <Separator/>

                <FormButton label="Save" type="button" onClickHandler={saveSmsTemplate}/>
                </>}
        </AdminTemplate>
    )
}

export default SmsTemplatePage
