import React from 'react';
import PropTypes from 'prop-types';
import {Box, Typography} from "@mui/material";

type TabPanelProps = {
    children: any
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
                <Box p={3}>
                    <Typography>{children}</Typography>
                </Box>
            )}
        </div>
    );
}
export default TabPanel
