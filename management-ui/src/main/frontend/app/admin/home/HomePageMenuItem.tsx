import React, {ReactNode} from 'react';
import MenuCard from "../../component/MenuCard";
import LocalPostOfficeIcon from '@mui/icons-material/LocalPostOffice';
import {Apps, AssignmentInd, Key, PeopleAlt} from "@mui/icons-material";

const FONT_SIZE: number = 150

interface HomePageMenuItemWrapperProps {
    content: HomePageMenuItemProps
}

interface HomePageMenuItemProps {
    titleText: string
    titleIcon: ReactNode
    body: string
    link: string
}

const HomePageMenuItem: React.FC<HomePageMenuItemWrapperProps> = ({content}) => {
    return (
        <MenuCard linkTo={content.link}
                  content={
                      <div>
                          <div style={{textAlign: "center"}}>
                              <h1>{content.titleText}</h1>
                              {content.titleIcon}
                          </div>
                          <h3 style={{textAlign: "justify"}}>
                              {content.body}
                          </h3>
                      </div>
                  }/>
    )
}

export const homeMenuContent = {
    clientApplications: {
        titleText: "Client Application Management Section",
        titleIcon: <Apps style={{fontSize: FONT_SIZE}}/>,
        body: "In this section you can manage all client application on VAuthenticator. Your will able to " +
            "   create, delete, set parameters like redirect uri, application roles and so on",
        link: "/client-applications/list"
    },
    roles: {
        titleText: "Roles Management Section",
        titleIcon: <AssignmentInd style={{fontSize: FONT_SIZE}}/>,
        body: "In this section you can manage all Role used in all applications federated with VAuthenticator. Your will able to " +
            "   create, delete or modify descriptions",
        link: "/roles"
    },
    accounts: {
        titleText: "Account Management Section",
        titleIcon: <PeopleAlt style={{fontSize: FONT_SIZE}}/>,
        body: "In this section you can manage Accounts in VAuthenticator from OpenIdConnect prospective. Your will able to " +
            " disable accounts, invalidate and force password reset",
        link: "/accounts"
    },
    keys: {
        titleText: "Key Management Section",
        titleIcon: <Key style={{fontSize: FONT_SIZE}}/>,
        body: "In this section you can manage Keys in VAuthenticator for token signature",
        link: "/keys"
    },
    mails: {
        titleText: "EMail Template Management Section",
        titleIcon: <LocalPostOfficeIcon style={{fontSize: FONT_SIZE}}/>,
        body: "In this section you can manage EMail templates in VAuthenticator",
        link: "/email-templates"
    }
}
export default HomePageMenuItem