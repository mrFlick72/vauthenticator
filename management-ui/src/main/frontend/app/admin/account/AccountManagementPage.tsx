import React, {useEffect, useState} from 'react';
import {useNavigate, useParams} from "react-router";
import FormInputTextField from "../../component/FormInputTextField";
import AdminTemplate from "../../component/AdminTemplate";
import Separator from "../../component/Separator";
import CheckboxesGroup from "../../component/CheckboxesGroup";
import {convertToMandatoryAction, findAccountFor, saveAccountFor} from "./AccountRepository";
import FormButton from "../../component/FormButton";
import {findAllRoles} from "../roles/RoleRepository";
import LeftRightComponentRow from "../../component/LeftRightComponentRow";
import {Card, CardContent, CardHeader, Typography} from "@mui/material";
import {PeopleAlt} from "@mui/icons-material";
import FormSelect, {SelectOption} from "../../component/FormSelect";

const AccountManagementPage : React.FC = () => {
    let {accountEMail} = useParams<string>()!;
    const navigate = useNavigate();
    const [email, setEmail] = useState<string>(accountEMail!)
    const [enabled, setEnabled] = useState({enabled: false})
    const [accountLocked, setAccountLocked] = useState({accountLocked: false})
    const [mandatoryAction, setMandatoryAction] = useState<SelectOption>({value: "NO_ACTION", label: "NO_ACTION"})
    const [roles, setRoles] = useState<SelectOption[]>([])
    const [availableRoles, setAvailableRoles] = useState<SelectOption[]>([])

    let pageTitle = "Account Management";

    useEffect(() => {
            async function init() {
                let roles = await findAllRoles()
                let account = await findAccountFor(email!)
                if (account) {
                    setEnabled({enabled: account.enabled})
                    setAccountLocked({accountLocked: account.accountLocked})
                    setAvailableRoles(roles.map(role => {
                        return {value: role.name, label: role.name}
                    }))
                    setRoles(account.authorities.map(authority => {
                        return {value: authority, label: authority}
                    }))

                    setMandatoryAction({value: account.mandatoryAction, label: account.mandatoryAction})
                } else {
                    navigate(-1)
                }
            }

            init();
        }, []
    )

    const save = () => {
        const account = {
            email: email,
            enabled: enabled.enabled,
            accountLocked: accountLocked.accountLocked,
            authorities: roles.map(role => role.value),
            mandatoryAction: convertToMandatoryAction(mandatoryAction.value)
        }

        saveAccountFor(account)
            .then(response => {
                if (response.status === 204) {
                    navigate(-1);
                }
            })
    }

    return (
        <AdminTemplate maxWidth="xl" page={pageTitle}>

            <Typography variant="h3" component="h3">
                <PeopleAlt fontSize="large"/> Account email: {accountEMail}
            </Typography>

            <Card>
                <CardHeader title="Account definition" color="textSecondary"/>
                <CardContent>
                    <FormInputTextField id="email"
                                        label="Account EMail"
                                        required={true}
                                        disabled={true}
                                        handler={(value) => {
                                            setEmail(value.target.value)
                                        }}
                                        value={email}/>

                    <CheckboxesGroup id="enabled"
                                     handler={(value) => {
                                         setEnabled({enabled: value.target.checked})
                                     }}
                                     choicesRegistry={enabled}
                                     legend="Account Enabled"/>

                    <CheckboxesGroup id="accountLocked"
                                     handler={(value) => {
                                         setAccountLocked({accountLocked: value.target.checked})
                                     }}
                                     choicesRegistry={accountLocked}
                                     legend="Account Locked"/>

                    <FormSelect id="mandatoryAction"
                                label="Mandatory Action"
                                multi={false}
                                value={mandatoryAction}
                                onChangeHandler={(event) => {
                                    setMandatoryAction(event)
                                }}
                                options={[
                                    {value: "NO_ACTION", label: "NO_ACTION"},
                                    {value: "RESET_PASSWORD", label: "RESET_PASSWORD"},
                                ]}
                    />

                    <FormSelect id="roles"
                                label="Roles"
                                multi={true}
                                onChangeHandler={(event) => {
                                    setRoles(event)
                                }}
                                options={availableRoles}
                                value={roles}/>
                    <Separator/>

                    <LeftRightComponentRow leftComponentColumnsSize={2}
                                           leftComponents={<FormButton label="Back"
                                                                       onClickHandler={() => navigate(-1)}/>}

                                           rightComponentsColumnSize={2}
                                           rightComponents={<FormButton label="Save" direction="rtl"
                                                                        onClickHandler={save}/>}
                    />

                </CardContent>
            </Card>

        </AdminTemplate>
    );
}

export default AccountManagementPage