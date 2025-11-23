import React, {useEffect, useState} from 'react';
import {Link} from "react-router-dom";
import {useNavigate} from "react-router";
import {Apps, Delete, Edit, VpnKey} from "@mui/icons-material";
import {deleteClientApplicationFor, findAllClientApplications} from "../../repository/ClientAppRepository";
import AdminTemplate from "../../../../component/AdminTemplate";
import ConfirmationDialog from '../../../../component/ConfirmationDialog';
import FormButton from "../../../../component/FormButton";
import StickyHeadTable, {StickyHeadTableColumn} from '../../../../component/StickyHeadTable';
import {ClientAppListPageTableRow} from "./ClientAppListTypes";
import ResetClientAppSecretDialog from "./ResetClientAppSecretDialog";

const columns: StickyHeadTableColumn[] = [
    {id: 'clientAppName', label: 'Client Application Name', minWidth: 170},
    {id: 'clientAppId', label: 'Client Application Id', minWidth: 170},
    {id: 'scopes', label: 'Client Scopes', minWidth: 170},
    {id: 'authorizedGrantTypes', label: 'Client Application Autorized Grant Type', minWidth: 170},
    {id: 'edit', label: 'Edit Application', minWidth: 170},
    {id: 'delete', label: 'Delete Application', minWidth: 170},
    {id: 'secretKey', label: 'Reset Password', minWidth: 170}
];

const ClientAppListPage = () => {
    const [applications, setApplications] = useState<ClientAppListPageTableRow[]>([])
    const [openResetClientAppSecretDialog, setOpenResetClientAppSecretDialog] = useState(false)
    const [openDeleteClientAppDialog, setOpenDeleteClientAppDialog] = useState(false)
    const [currentClientAppId, setCurrentClientAppId] = useState("")

    const navigate = useNavigate()

    const editLinkFor = (clientAppId: string) => {
        return <Edit onClick={() => {
            navigate(`/client-applications/edit/${clientAppId}`)
        }}/>
    }

    const handleCloseResetClientAppSecretDialog = () => {
        setOpenResetClientAppSecretDialog(false);
    };

    const handleCloseDeleteClientAppDialog = () => {
        setOpenDeleteClientAppDialog(false);
    };

    const deleteClientAppFor = (clientAppId: string) => {
        return <Delete onClick={() => {
            setOpenDeleteClientAppDialog(true)
            setCurrentClientAppId(clientAppId)
        }}/>;
    }
    const deleteClientApp = () => {
        deleteClientApplicationFor(currentClientAppId)
            .then(response => {
                if (response.status === 204) {
                    handleCloseDeleteClientAppDialog()
                    fetchAllApplications()
                }
            })
    }

    const resetSecretKeyFor = (clientAppId: string) => {
        return <VpnKey onClick={() => {
            setOpenResetClientAppSecretDialog(true)
            setCurrentClientAppId(clientAppId)
        }}/>;
    }

    const fetchAllApplications = () => {
        findAllClientApplications()
            .then(val => {
                let rows = val.map(value => {
                    return {
                        clientAppId: value.clientAppId,
                        clientAppName: value.clientAppName,
                        scopes: value.scopes.join(","),
                        authorizedGrantTypes: value.authorizedGrantTypes.join(","),
                        edit: editLinkFor(value["clientAppId"]),
                        delete: deleteClientAppFor(value["clientAppId"]),
                        secretKey: resetSecretKeyFor(value["clientAppId"])
                    }
                })
                setApplications(rows)
            });
    }

    useEffect(() => fetchAllApplications(), []);

    return (
        <AdminTemplate maxWidth="xl" page=": Client Application Admin">
            <ResetClientAppSecretDialog open={openResetClientAppSecretDialog}
                                        onClose={handleCloseResetClientAppSecretDialog}
                                        clientAppId={currentClientAppId}/>
            <ConfirmationDialog maxWidth="md"
                                open={openDeleteClientAppDialog}
                                onExecute={deleteClientApp}
                                onClose={handleCloseDeleteClientAppDialog}
                                message="Are you sure delete the selected Client App"
                                title="Client App deletion"/>

            <Link to={"/client-applications/save"}>
                <FormButton type="button"
                            labelPrefix={<Apps fontSize="large"/>}
                            label={"New Client Application"}/>
            </Link>

            <StickyHeadTable columns={columns} rows={applications}/>

        </AdminTemplate>
    );
}

export default ClientAppListPage