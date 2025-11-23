import React from 'react';
import FormButton from "../../component/FormButton";
import Separator from "../../component/Separator";
import {Dialog, DialogActions, DialogContent, DialogTitle} from "@mui/material";

interface KeyDialogProps {
    kid: string
    title: string
    content: string
    open: boolean
    handleClose: (open: boolean) => void
    handler: (kid: string) => void
}

const KeyDialog: React.FC<KeyDialogProps> = ({open, handleClose, title, content, handler}) => {

    return (
        <Dialog onClose={handleClose} aria-labelledby="simple-dialog-title" open={open} maxWidth="md">
            <DialogTitle id="simple-dialog-title">{title}</DialogTitle>
            <DialogContent>

                {content}

                <Separator/>

                <DialogActions>
                    <FormButton label="Save" onClickHandler={handler}/>
                    <FormButton  label="Cancel" onClickHandler={handleClose}/>
                </DialogActions>
            </DialogContent>
        </Dialog>
    );
}

export default KeyDialog