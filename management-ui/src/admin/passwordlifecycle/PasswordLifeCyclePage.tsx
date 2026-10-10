import React from "react";
import {useSearchParams} from "react-router";
import AdminTemplate from "../../components/AdminTemplate";
import Separator from "../../components/Separator";
import AccountRulesCard from "./AccountRulesCard";
import BulkRulesCard from "./BulkRulesCard";

const PasswordLifeCyclePage = () => {
    const [searchParams] = useSearchParams()

    return <AdminTemplate maxWidth="xl" page="Password Lifecycle Management">
        <AccountRulesCard initialAccount={searchParams.get("account") ?? ""}/>
        <Separator/>
        <BulkRulesCard/>
    </AdminTemplate>
}

export default PasswordLifeCyclePage
