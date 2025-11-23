import React, {SyntheticEvent, useEffect, useState} from 'react';
import {deleteKeyFor, findAllKeys, rotateKeyFor, VAuthenticatorKey} from "./KeyRepository";
import StickyHeadTable, {StickyHeadTableColumn} from "../../component/StickyHeadTable";
import AdminTemplate from "../../component/AdminTemplate";
import {Alert, Snackbar} from "@mui/material";
import {Delete} from "@mui/icons-material";
import AutorenewIcon from '@mui/icons-material/Autorenew';
import KeyDialog from "./KeyDialog";
import moment from "moment";

const columns: StickyHeadTableColumn[] = [
    {id: 'masterKey', label: 'Maser Key', minWidth: 170},
    {id: 'kid', label: 'Key Id', minWidth: 170},
    {id: 'delete', label: 'Delete Key', minWidth: 170},
    {id: 'rotate', label: 'Rotate Key', minWidth: 170},
    {id: 'expireIn', label: 'Expire In', minWidth: 170}
];

const KeysManagementPage = () => {
    const pageTitle = "Keys Management"

    const [kid, setKid] = useState("")
    const [kidTtl, setKidTtl] = useState(0)
    const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
    const [openRenewDialog, setOpenRenewDialog] = useState(false)

    const [keys, setKeys] = React.useState([])
    const [openFailure, setOpenFailure] = React.useState(false);
    const [openWarning, setOpenWarning] = React.useState(false);
    const handleClose = (event: SyntheticEvent<Element, Event>) => {
        setOpenFailure(false);
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
            .then(response => {
                if (response.status === 200) {
                    return response.json()
                } else {
                    setKeys([])
                }
            })
            .then(val => {
                let rows = val.map((value: VAuthenticatorKey) => {
                    return {
                        masterKey: value.masterKey,
                        kid: value.kid,
                        delete: getDeleteLinkFor(value.kid),
                        rotate: getRotateLinkFor(value.kid, value.ttl),
                        expireIn: value.ttl == 0 ? "Never" : moment.unix(value.ttl).format("YYYY-MM-DD:HH:mm")

                    }
                })
                setKeys(rows)
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
                               .then(response => {
                                   if (response.status === 204) {
                                       fetchAllKeys()
                                   } else {
                                       setOpenFailure(true);
                                   }
                               })
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
                                   .then(response => {
                                       if (response.status === 204) {
                                           fetchAllKeys()
                                       } else {
                                           setOpenWarning(true);
                                       }
                                   })
                           } else {
                               setOpenWarning(true);
                           }

                       }}
            />

            <Snackbar open={openFailure} autoHideDuration={600}>
                <Alert onClose={handleClose} severity="error" sx={{whiteSpace: 'pre-line'}}>
                    Delete this key is not allowed where <br/>
                    The key is already rotated either there is only one key available
                </Alert>
            </Snackbar>
            <Snackbar open={openWarning} autoHideDuration={600}>
                <Alert onClose={handleClose} severity="warning">
                    Key already rotated
                </Alert>
            </Snackbar>
            <StickyHeadTable columns={columns} rows={keys}/>
        </AdminTemplate>
    )
}

export default KeysManagementPage