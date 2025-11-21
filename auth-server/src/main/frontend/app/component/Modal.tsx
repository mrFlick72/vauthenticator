import React from "react";

import FormButton from "../component/FormButton";
import Separator from "../component/Separator";
import {Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle} from "@mui/material";
import {Breakpoint} from "@mui/system";

interface ConfirmationDialogProps {
    onExecuteButtonLabel : string,
    onExecute: () => void,
    onCloseButtonLabel : string,
    onClose: () => void,
    open: boolean,
    title: string,
    headerLabel: string,
    maxWidth: Breakpoint,
    children: React.ReactNode
}

const Modal: React.FC<ConfirmationDialogProps> = ({
                                                      onExecute,
                                                      onExecuteButtonLabel,
                                                      onClose,
                                                      onCloseButtonLabel,
                                                      open,
                                                      title,
                                                      headerLabel,
                                                      maxWidth,
                                                      children
                                                  }) => {
    return (
        <Dialog onClose={onClose} aria-labelledby="simple-dialog-title" open={open} maxWidth={maxWidth}>
            <DialogTitle id="simple-dialog-title">{title}</DialogTitle>
            <DialogContent>
                <DialogContentText id="alert-dialog-description">
                    {headerLabel}
                </DialogContentText>

                {children}

                <Separator/>

                <DialogActions>
                    <FormButton label={onExecuteButtonLabel} type={"button"} onClickHandler={onExecute}/>
                    <FormButton label={onCloseButtonLabel} type={"button"} onClickHandler={onClose}/>
                </DialogActions>
            </DialogContent>
        </Dialog>
    );
}

export default Modal