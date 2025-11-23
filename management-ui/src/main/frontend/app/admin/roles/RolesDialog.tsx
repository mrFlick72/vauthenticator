import React from 'react';
import {saveRoleFor} from "./RoleRepository";
import FormButton from "../../component/FormButton";
import Separator from "../../component/Separator";
import FormInputTextField from "../../component/FormInputTextField";
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
                        saveRoleFor(role)
                            .then(response => {
                                if (response.status === 204) {
                                    onClose(true)
                                }
                            })
                    }}/>
                    <FormButton label="Cancel" onClickHandler={onClose}/>
                </DialogActions>
            </DialogContent>
        </Dialog>
    );
}

export default RoleDialog