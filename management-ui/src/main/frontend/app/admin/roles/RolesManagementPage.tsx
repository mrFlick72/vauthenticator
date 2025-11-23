import React, {SyntheticEvent, useEffect} from 'react';
import AdminTemplate from "../../component/AdminTemplate";
import StickyHeadTable, {StickyHeadTableColumn} from "../../component/StickyHeadTable";
import {deleteRoleFor, findAllRoles, Role} from "./RoleRepository";
import FormButton from "../../component/FormButton";
import ConfirmationDialog from "../../component/ConfirmationDialog";
import RolesDialog from "./RolesDialog";
import {AssignmentInd, Delete, Edit} from "@mui/icons-material";
import {Alert, Snackbar} from "@mui/material";

const columns: StickyHeadTableColumn[] = [
    {id: 'name', label: 'Role', minWidth: 170},
    {id: 'description', label: 'Description', minWidth: 170},
    {id: 'edit', label: 'Edit Role', minWidth: 170},
    {id: 'delete', label: 'Delete Role', minWidth: 170}
];

const RolesManagementPage = () => {
    const pageTitle = "Roles Management"
    const [roles, setRoles] = React.useState<Role[]>([])
    const [openConfirmationDeleteRoleDialog, setOpenConfirmationDeleteRoleDialog] = React.useState(false)
    const [openRolesManagementDialog, setOpenRolesManagementDialog] = React.useState(false)
    const [selectedRole, setSelectedRole] = React.useState("")
    const [role, setRole] = React.useState<Role>({name: "", description: ""})
    const [isRoleReadOnly, setIsRoleReadOnly] = React.useState(false)
    const [openFailure, setOpenFailure] = React.useState(false);
    const handleClose = (event: SyntheticEvent<Element, Event>) => {
        setOpenFailure(false);
    };
    const getEditLinkFor = (role: Role) => {
        return <Edit onClick={() => {
            setRole(role)
            setIsRoleReadOnly(true)
            setOpenRolesManagementDialog(true);
        }}/>;
    }

    const handleCloseConfirmationDialog = (refresh: boolean) => {
        setOpenConfirmationDeleteRoleDialog(false);
        if (refresh) {
            fetchAllRoles();
        }
    };

    const handleCloseRolesDialog = (refresh: boolean) => {
        setOpenRolesManagementDialog(false);
        if (refresh) {
            fetchAllRoles()
        }
    };

    const getDeleteLinkFor = (roleName: string) => {
        return <Delete onClick={() => {
            setSelectedRole(roleName)
            setOpenConfirmationDeleteRoleDialog(true);
        }}/>;
    }

    const deleteRole = () => {
        deleteRoleFor(selectedRole)
            .then(response => {
                if (response.status !== 204) {
                    setOpenFailure(true);
                }
                handleCloseConfirmationDialog(true)
            })
    }

    const fetchAllRoles = () => {
        findAllRoles()
            .then(values => {
                let rows = values.map(value => {
                    return {
                        name: value.name,
                        description: value.description,
                        edit: getEditLinkFor({name: value["name"], description: value["description"]}),
                        delete: getDeleteLinkFor(value["name"])
                    }
                })

                setRoles(rows)
            });
    }

    useEffect(() => {
        fetchAllRoles()
    }, []);

    return (
        <AdminTemplate maxWidth="xl" page={pageTitle}>
            <ConfirmationDialog maxWidth="md"
                                open={openConfirmationDeleteRoleDialog}
                                onExecute={deleteRole}
                                onClose={handleCloseConfirmationDialog}
                                message="Are you sure delete the selected role"
                                title="Role delete"/>

            <RolesDialog open={openRolesManagementDialog}
                         role={role} setRole={setRole}
                         isRoleReadOnly={isRoleReadOnly}
                         onClose={handleCloseRolesDialog}
                         title="Role management"/>

            <FormButton type="button"
                        onClickHandler={() => {
                            setRole({name: "", description: ""})
                            setOpenRolesManagementDialog(true);
                            setIsRoleReadOnly(false);
                        }}
                        labelPrefix={<AssignmentInd fontSize="large"/>}
                        label={"New Role"}/>

            <StickyHeadTable columns={columns} rows={roles}/>

            <Snackbar open={openFailure} autoHideDuration={600}>
                <Alert onClose={handleClose} severity="error" sx={{whiteSpace: 'pre-line'}}>
                    Delete this Role is not allowed it is a default role
                </Alert>
            </Snackbar>
        </AdminTemplate>
    )
}

export default RolesManagementPage