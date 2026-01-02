import React, {ReactNode} from 'react';
import {Grid} from "@mui/material";


interface MenuCardContainerProps {
    children: ReactNode
}

const MenuCardContainer: React.FC<MenuCardContainerProps> = ({children}) => {
    return <Grid container spacing={2}>
        {children}
    </Grid>
}
export default MenuCardContainer