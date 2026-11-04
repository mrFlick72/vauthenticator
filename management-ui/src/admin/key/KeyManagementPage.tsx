import React, {SyntheticEvent, useEffect, useState} from 'react';
import {deleteKeyFor, findAllKeys, KEYS, rotateKeyFor, VAuthenticatorKey} from "./KeyRepository";
import StickyHeadTable, {StickyHeadTableColumn} from "../../components/StickyHeadTable";
import AdminTemplate from "../../components/AdminTemplate";
import {Alert, Snackbar} from "@mui/material";
import {Delete} from "@mui/icons-material";
import AutorenewIcon from '@mui/icons-material/Autorenew';
import KeyDialog from "./KeyDialog";
import ErrorToast from "../../components/ErrorToast";
import {failureMessage} from "../../utils/ApiError";

const columns: StickyHeadTableColumn[] = [
    {id: 'masterKey', label: 'Maser Key', minWidth: 170},
    {id: 'kid', label: 'Key Id', minWidth: 170},
    {id: 'delete', label: 'Delete Key', minWidth: 170},
    {id: 'rotate', label: 'Rotate Key', minWidth: 170},
    {id: 'expireIn', label: 'Expire In', minWidth: 170}
];

const padDatePart = (value: number) => value.toString().padStart(2, "0")

const formatUnixTimestamp = (timestamp: number) => {
    const date = new Date(timestamp * 1000)
    return `${date.getFullYear()}-${padDatePart(date.getMonth() + 1)}-${padDatePart(date.getDate())}:${padDatePart(date.getHours())}:${padDatePart(date.getMinutes())}`
}

const KeysManagementPage = () => {
    const pageTitle = "Keys Management"

    const [kid, setKid] = useState("")
    const [kidTtl, setKidTtl] = useState(0)
    const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
    const [openRenewDialog, setOpenRenewDialog] = useState(false)

    const [keys, setKeys] = React.useState<any[]>([])
    const [loadFailure, setLoadFailure] = React.useState<string | null>(null);
    const [writeFailure, setWriteFailure] = React.useState<string | null>(null);
    const [openWarning, setOpenWarning] = React.useState(false);
    const handleClose = (event: SyntheticEvent<Element, Event>) => {
        setOpenWarning(false);
    };

    const getDeleteLinkFor = (kid: string) => {
        return <Delete onClick={() => {
            setKid(kid)
            setOpenDeleteDialog(true);
        }}/>;
    }

    const getRotateLinkFor = (kid: string, ttl: number) => {
        return <AutorenewIcon onClick={() => {
            setKid(kid)
            setKidTtl(ttl)
            setOpenRenewDialog(true);
        }}/>;
    }
    const fetchAllKeys = () => {
        findAllKeys()
            .then(val => {
                let rows = val.map((value: VAuthenticatorKey) => {
                    return {
                        masterKey: value.masterKey,
                        kid: value.kid,
                        delete: getDeleteLinkFor(value.kid),
                        rotate: getRotateLinkFor(value.kid, value.ttl),
                        expireIn: value.ttl == 0 ? "Never" : formatUnixTimestamp(value.ttl)

                    }
                })
                setKeys(rows)
                setLoadFailure(null)
            })
            .catch(error => {
                setKeys([])
                setLoadFailure(failureMessage("load the keys", error, KEYS))
            })
    }

    useEffect(() => {
        fetchAllKeys()
    }, []);

    return (
        <AdminTemplate maxWidth="xl" page={pageTitle}>
            <KeyDialog kid={kid}
                       open={openDeleteDialog}
                       handleClose={() => setOpenDeleteDialog(false)}
                       title={"Delete Key Dialog"}
                       content={`Do you want delete the key: ${kid}`}
                       handler={() => {
                           setOpenDeleteDialog(false);
                           deleteKeyFor(kid)
                               .then(() => fetchAllKeys())
                               .catch(error => setWriteFailure(failureMessage("delete the key", error, KEYS)))
                       }}
            />

            <KeyDialog kid={kid}
                       open={openRenewDialog}
                       handleClose={() => setOpenRenewDialog(false)}
                       content={`Do you want renew the key: ${kid}`}
                       title={"Renew Key Dialog"}
                       handler={() => {
                           const already_rotated = kidTtl
                           setOpenRenewDialog(false);

                           if (!already_rotated) {
                               rotateKeyFor(kid)
                                   .then(() => fetchAllKeys())
                                   .catch(error => setWriteFailure(failureMessage("rotate the key", error, KEYS)))
                           } else {
                               setOpenWarning(true);
                           }

                       }}
            />

            <ErrorToast message={writeFailure} onClose={() => setWriteFailure(null)}/>
            <Snackbar open={openWarning} autoHideDuration={600}>
                <Alert onClose={handleClose} severity="warning">
                    Key already rotated
                </Alert>
            </Snackbar>
            {loadFailure
                ? <Alert severity="error">{loadFailure}</Alert>
                : <StickyHeadTable columns={columns} rows={keys}/>}
        </AdminTemplate>
    )
}

export default KeysManagementPage
