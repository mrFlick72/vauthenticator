import React, {useState} from "react";

import {Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle} from "@mui/material";
import FormInputTextField from "../../../../component/FormInputTextField";
import Separator from "../../../../component/Separator";
import FormButton from "../../../../component/FormButton";
import {resetSecretFor} from "../../repository/ClientAppRepository";

type ResetClientAppSecretDialogProps = {
    onClose: () => void
    open: boolean
    clientAppId: string
}
const ResetClientAppSecretDialog: React.FC<ResetClientAppSecretDialogProps> = ({onClose, open, clientAppId}) => {
    const [secret, setSecret] = useState("")
    return (
        <Dialog onClose={onClose} aria-labelledby="simple-dialog-title" open={open} maxWidth="lg">
            <DialogTitle id="simple-dialog-title">Reset Client App <b>{clientAppId}</b> Secret-Key</DialogTitle>
            <DialogContent>
                <DialogContentText id="alert-dialog-description">
                    Put hear the new client app secret for client app: <b>{clientAppId}</b>
                </DialogContentText>

                <FormInputTextField id="resetSecretKeyField"
                                    label="Client App Secret"
                                    type="password"
                                    required={true}
                                    value={secret}
                                    handler={(value) => {
                                        setSecret(value.target.value)
                                    }}/>
                <Separator/>

                <DialogActions>
                    <FormButton label="Save" onClickHandler={() => {
                        resetSecretFor(clientAppId, secret)
                            .then(_ => setSecret(""))
                            .then(_ => onClose())
                    }}/>
                    <FormButton label="Close" onClickHandler={onClose}/>
                </DialogActions>
            </DialogContent>
        </Dialog>
    );
}

export default ResetClientAppSecretDialog