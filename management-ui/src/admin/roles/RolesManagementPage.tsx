import React, {useEffect} from 'react';
import AdminTemplate from "../../components/AdminTemplate";
import StickyHeadTable, {StickyHeadTableColumn} from "../../components/StickyHeadTable";
import {deleteRoleFor, findAllRoles, Role, rolesMessageFor} from "./RoleRepository";
import FormButton from "../../components/FormButton";
import ConfirmationDialog from "../../components/ConfirmationDialog";
import RolesDialog from "./RolesDialog";
import {AssignmentInd, Delete, Edit} from "@mui/icons-material";
import {Alert} from "@mui/material";
import ErrorToast from "../../components/ErrorToast";
import {ApiError} from "../../utils/ApiError";

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
    const [loadFailure, setLoadFailure] = React.useState<string | null>(null);
    const [writeFailure, setWriteFailure] = React.useState<string | null>(null);
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
            .catch(error => setWriteFailure(
                // a protected role answers 412 with no body
                error instanceof ApiError && error.status === 412
                    ? "Delete this Role is not allowed it is a protected role"
                    : rolesMessageFor(error)
            ))
            .finally(() => handleCloseConfirmationDialog(true))
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
                setLoadFailure(null)
            })
            .catch(error => {
                setRoles([])
                setLoadFailure(rolesMessageFor(error))
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

            {loadFailure
                ? <Alert severity="error">{loadFailure}</Alert>
                : <StickyHeadTable columns={columns} rows={roles}/>}

            <ErrorToast message={writeFailure} onClose={() => setWriteFailure(null)}/>
        </AdminTemplate>
    )
}

export default RolesManagementPage