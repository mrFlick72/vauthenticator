import React, {useState} from "react";

import {Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle} from "@mui/material";
import FormInputTextField from "../../../../components/FormInputTextField";
import Separator from "../../../../components/Separator";
import FormButton from "../../../../components/FormButton";
import {clientAppsMessageFor, resetSecretFor} from "../../domain/ClientAppRepository";
import ErrorToast from "../../../../components/ErrorToast";

type ResetClientAppSecretDialogProps = {
    onClose: () => void
    open: boolean
    clientAppId: string
}
const ResetClientAppSecretDialog: React.FC<ResetClientAppSecretDialogProps> = ({onClose, open, clientAppId}) => {
    const [secret, setSecret] = useState("")
    const [failure, setFailure] = useState<string | null>(null)
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
                        // on failure the dialog stays open with the typed secret, so the admin can retry
                        resetSecretFor(clientAppId, secret)
                            .then(() => {
                                setSecret("")
                                onClose()
                            })
                            .catch(error => setFailure(clientAppsMessageFor(error)))
                    }}/>
                    <FormButton label="Close" onClickHandler={onClose}/>
                </DialogActions>
            </DialogContent>
            <ErrorToast message={failure} onClose={() => setFailure(null)}/>
        </Dialog>
    );
}

export default ResetClientAppSecretDialog