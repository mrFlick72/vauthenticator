import React, {useEffect} from 'react';
import {Box, CircularProgress, Paper, Stack, ThemeProvider, Typography} from "@mui/material";
import {authenticate} from "./Authenticator";
import ComponentInitializer from '../utils/ComponentInitializer';
import themeProvider from "../theme/ThemeProvider";

const Callback = () => {
    const params = new URLSearchParams(window.location.search)

    useEffect(() => {
        let code = params.get("code")!!
        authenticate(code)
            .then(_ => {
                window.location.href = window.sessionStorage.getItem("returnTo")!!
            })
    }, [])

    return (
        <ThemeProvider theme={themeProvider}>
            <Box
                sx={{
                    minHeight: "100vh",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    px: 3,
                    background: "linear-gradient(135deg, #f7f8f5 0%, #e9ece5 100%)"
                }}
            >
                <Paper
                    elevation={0}
                    sx={{
                        width: "100%",
                        maxWidth: 420,
                        borderRadius: 6,
                        px: 5,
                        py: 6,
                        textAlign: "center",
                        backgroundColor: "rgba(255, 255, 255, 0.92)",
                        border: "1px solid rgba(37, 38, 36, 0.08)",
                        boxShadow: "0 24px 60px rgba(37, 38, 36, 0.12)",
                        backdropFilter: "blur(12px)"
                    }}
                >
                    <Stack spacing={2.5} alignItems="center">
                        <Box sx={{position: "relative", display: "inline-flex"}}>
                            <CircularProgress
                                variant="determinate"
                                value={100}
                                size={72}
                                thickness={2}
                                sx={{color: "rgba(37, 38, 36, 0.10)"}}
                            />
                            <CircularProgress
                                size={72}
                                thickness={4}
                                sx={{
                                    color: "primary.main",
                                    position: "absolute",
                                    left: 0
                                }}
                            />
                        </Box>

                        <Typography variant="h5" sx={{fontWeight: 700, letterSpacing: "-0.02em"}}>
                            Signing you in
                        </Typography>

                        <Typography variant="body1" color="text.secondary" sx={{maxWidth: 260}}>
                            Completing the secure sign-in flow and preparing your session.
                        </Typography>
                    </Stack>
                </Paper>
            </Box>
        </ThemeProvider>
    )
}


ComponentInitializer(<Callback/>,)
