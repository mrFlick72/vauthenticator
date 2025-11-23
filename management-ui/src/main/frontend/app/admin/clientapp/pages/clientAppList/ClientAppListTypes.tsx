import React from "react";

export type ClientAppListPageTableRow = {
    clientAppName: string
    clientAppId: string
    scopes: string
    authorizedGrantTypes: string
    edit: React.JSX.Element
    delete: React.JSX.Element
    secretKey: React.JSX.Element
}
