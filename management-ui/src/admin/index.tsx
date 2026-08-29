import React, {lazy, Suspense} from 'react';
import {createRoot} from 'react-dom/client';

import {BrowserRouter, Navigate, Route, Routes} from "react-router";

const HomePage = lazy(() => import("./home/HomePage"));
const RolesManagementPage = lazy(() => import("./roles/RolesManagementPage"));
const AccountManagementPage = lazy(() => import("./account/AccountManagementPage"));
const AccountListPage = lazy(() => import("./account/AccountListPage"));
const KeyManagementPage = lazy(() => import("./key/KeyManagementPage"));
const MailTemplatePage = lazy(() => import("./communication/MailTemplatePage"));
const ClientAppListPage = lazy(() => import("./clientapp/pages/clientAppList/ClientAppListPage"));
const ClientAppManagementPage = lazy(() => import('./clientapp/pages/clientAppManagement/ClientAppManagementPage'));

const VAuthenticatorAdminApp = () =>
    <BrowserRouter basename="/secure/admin">
        <Suspense fallback={<div></div>}>
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

                <Route path="*" element={<Navigate to="/" replace/>}/>
            </Routes>
        </Suspense>
    </BrowserRouter>


if (document.getElementById('app')) {
    const container = document.getElementById('app');
    if (container) {
        const root = createRoot(container); // createRoot(container!) if you use TypeScript
        root.render(<VAuthenticatorAdminApp/>);
    }
}