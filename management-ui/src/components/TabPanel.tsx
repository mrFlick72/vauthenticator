import React from 'react';
import {Box, Typography} from "@mui/material";

type TabPanelProps = {
    children: React.ReactNode
    value: string
    index: string
}

const TabPanel: React.FC<TabPanelProps> = ({children, value, index}) => {

    return (
        <div
            style={{width: "100%"}}
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}>
            {value === index && (
                <Box sx={{p: 3}}>
                    <Typography>{children}</Typography>
                </Box>
            )}
        </div>
    );
}
export default TabPanel
