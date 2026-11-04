import React, {useState} from "react";

import {Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle} from "@mui/material";
import FormInputTextField from "../../../../components/FormInputTextField";
import Separator from "../../../../components/Separator";
import FormButton from "../../../../components/FormButton";
import {CLIENT_APPLICATIONS, resetSecretFor} from "../../domain/ClientAppRepository";
import {failureMessage} from "../../../../utils/ApiError";
import ErrorToast from "../../../../components/ErrorToast";

type ResetClientAppSecretDialogProps = {
    onClose: () => void
    open: boolean
    clientAppId: string
}
const ResetClientAppSecretDialog: React.FC<ResetClientAppSecretDialogProps> = ({onClose, open, clientAppId}) => {
    const [secret, setSecret] = useState("")
    const [failure, setFailure] = useState<string | null>(null)
    // closing forgets the typed secret, so the dialog never opens pre-filled for another client application
    const close = () => {
        setSecret("")
        setFailure(null)
        onClose()
    }
    return (
        <Dialog onClose={close} aria-labelledby="simple-dialog-title" open={open} maxWidth="lg">
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
                            .then(close)
                            .catch(error => setFailure(
                                failureMessage("reset the client application secret", error, CLIENT_APPLICATIONS)
                            ))
                    }}/>
                    <FormButton label="Close" onClickHandler={close}/>
                </DialogActions>
            </DialogContent>
            <ErrorToast message={failure} onClose={() => setFailure(null)}/>
        </Dialog>
    );
}

export default ResetClientAppSecretDialog