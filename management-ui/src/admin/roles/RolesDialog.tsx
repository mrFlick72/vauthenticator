import React, {useState} from 'react';
import {rolesMessageFor, saveRoleFor} from "./RoleRepository";
import ErrorToast from "../../components/ErrorToast";
import FormButton from "../../components/FormButton";
import Separator from "../../components/Separator";
import FormInputTextField from "../../components/FormInputTextField";
import {Dialog, DialogActions, DialogContent, DialogTitle} from "@mui/material";

interface RoleDialogProps {
    onClose: any,
    open: boolean,
    title: string,
    role: any,
    setRole: any,
    isRoleReadOnly: boolean
}

const RoleDialog: React.FC<RoleDialogProps> = ({onClose, open, title, role, setRole, isRoleReadOnly}) => {
    const [failure, setFailure] = useState<string | null>(null)
    return (
        <Dialog onClose={onClose} aria-labelledby="simple-dialog-title" open={open} maxWidth="md">
            <DialogTitle id="simple-dialog-title">{title}</DialogTitle>
            <DialogContent>
                <FormInputTextField id="name"
                                    label="Role Name"
                                    type="text"
                                    disabled={isRoleReadOnly}
                                    value={role.name}
                                    handler={(value) => {
                                        setRole({name: value.target.value, description: role.description})
                                    }}/>

                <FormInputTextField id="description"
                                    label="Role Description"
                                    type="text"
                                    value={role.description}
                                    handler={(value) => {
                                        setRole({name: role.name, description: value.target.value})
                                    }}/>

                <Separator/>

                <DialogActions>
                    <FormButton label="Save" onClickHandler={() => {
                        // on failure the dialog stays open with the typed role, so the admin can retry
                        saveRoleFor(role)
                            .then(() => onClose(true))
                            .catch(error => setFailure(rolesMessageFor(error)))
                    }}/>
                    <FormButton label="Cancel" onClickHandler={onClose}/>
                </DialogActions>
            </DialogContent>
            <ErrorToast message={failure} onClose={() => setFailure(null)}/>
        </Dialog>
    );
}

export default RoleDialog