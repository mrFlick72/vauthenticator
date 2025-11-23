import React from 'react';
import {createRoot} from 'react-dom/client';

import {HashRouter} from "react-router-dom";
import {Route, Routes} from "react-router";
import HomePage from "./home/HomePage";
import RolesManagementPage from "./roles/RolesManagementPage";
import AccountManagementPage from "./account/AccountManagementPage";
import AccountListPage from "./account/AccountListPage";
import KeyManagementPage from "./key/KeyManagementPage";
import MailTemplatePage from "./templates/MailTemplatePage";
import ClientAppListPage from "./clientapp/pages/clientAppList/ClientAppListPage";
import ClientAppManagementPage from './clientapp/pages/clientAppManagement/ClientAppManagementPage';

const VAuthenticatorAdminApp = () =>
    <HashRouter>
        <Routes>
            <Route path="/" element={<HomePage/>}/>

            <Route path="/client-applications/list"
                   element={<ClientAppListPage/>}/>

            <Route path="/client-applications/save"
                   element={<ClientAppManagementPage/>}/>

            <Route path="/client-applications/edit/:clientAppId"
                   element={<ClientAppManagementPage/>}/>

            <Route path="/roles" element={<RolesManagementPage/>}/>

            <Route path="/accounts" element={<AccountListPage/>}/>
            <Route path="/accounts/edit/:accountEMail" element={<AccountManagementPage/>}/>

            <Route path="/keys" element={<KeyManagementPage/>}/>
            <Route path="/email-templates" element={<MailTemplatePage/>}/>
        </Routes>
    </HashRouter>


if (document.getElementById('app')) {
    const container = document.getElementById('app');
    if (container) {
        const root = createRoot(container); // createRoot(container!) if you use TypeScript
        root.render(<VAuthenticatorAdminApp/>);
    }
}