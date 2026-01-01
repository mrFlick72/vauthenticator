import React, {useState} from 'react';
import AdminTemplate from "../../components/AdminTemplate";
import {useNavigate} from "react-router";
import FormInputTextField from "../../components/FormInputTextField";
import Separator from "../../components/Separator";
import FormButton from "../../components/FormButton";
import {Card, CardContent, CardHeader} from "@mui/material";


export default () => {
    const pageTitle = "Account Management"
    const [email, setEmail] = useState("")

    const navigate = useNavigate();

    return <AdminTemplate maxWidth="xl" page={pageTitle}>
        <Card>
            <CardHeader title="Account Search" color="textSecondary"/>
            <CardContent>
                <FormInputTextField id="email"
                                    label="Account EMail"
                                    required={true}
                                    disabled={false}
                                    handler={(value) => {
                                        setEmail(value.target.value)
                                    }}
                                    value={email}/>


                <Separator/>
                <FormButton label="Find" onClickHandler={() => navigate(`/accounts/edit/${email}`)}/>
            </CardContent>
        </Card>
    </AdminTemplate>

}