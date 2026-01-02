import React, { useEffect } from "react";
import themeProvider from "../theme/ThemeProvider";
import vauthenticatorStyles from "../theme/styles";
import { AppBar, Container, IconButton, Paper, ThemeProvider, Toolbar, Typography } from "@mui/material";
import { ExitToApp } from "@mui/icons-material";
import MenuIcon from '@mui/icons-material/Menu';
import { Breakpoint } from "@mui/system";
import { isAuthenticated } from "../auth/Authenticator";

interface AdminTemplateProps {
    page: string,
    maxWidth: Breakpoint,
    children: React.ReactNode;
}

const AdminTemplate: React.FC<AdminTemplateProps> = ({ page, maxWidth, children }) => {
    let theme = themeProvider
    const classes = vauthenticatorStyles();
    useEffect(() => {
        isAuthenticated().then()
    }, []); // Or [] if effect doesn't need props or state

    return (
        <ThemeProvider theme={theme}>
            <AppBar position="static">
                <Toolbar variant="dense">
                    <a href="#">
                        <IconButton edge="start"
                            style={classes.menuButton}
                            color="default"
                            aria-label="menu">
                            <MenuIcon style={classes.menuButton}
                                color="inherit" />
                        </IconButton>
                    </a>

                    <Typography variant="h6" style={classes.title}>
                        VAuthenticator Administration {page}
                    </Typography>

                    <a href="/logout">
                        <IconButton edge="start"
                            style={classes.menuButton}
                            color="inherit"
                            aria-label="menu">
                            <ExitToApp /> Logout
                        </IconButton>
                    </a>
                </Toolbar>
            </AppBar>

            <Container maxWidth={maxWidth}>
                <Paper elevation={3}>
                    {children}
                </Paper>
            </Container>
        </ThemeProvider>
    )

}

export default AdminTemplate