import React from "react";
import {Container, Paper} from "@mui/material";
import {Breakpoint} from "@mui/system";

interface TemplateProps {
    maxWidth: Breakpoint
    children: React.ReactNode;
}

const Template: React.FC<TemplateProps> = ({maxWidth, children}) => {
    return (
        <Container maxWidth={maxWidth}>
            <Paper elevation={3}>
                {children}
            </Paper>
        </Container>
    )
}
export default Template