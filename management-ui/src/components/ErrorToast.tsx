import React from "react";
import {Alert, Snackbar} from "@mui/material";

interface ErrorToastProps {
    message: string | null
    onClose: () => void
}

/**
 * The toast a failed write shows; a failed load shows an inline Alert instead.
 */
const ErrorToast: React.FC<ErrorToastProps> = ({message, onClose}) =>
    <Snackbar open={message !== null}
              autoHideDuration={6000}
              onClose={(_, reason) => reason !== "clickaway" && onClose()}>
        <Alert onClose={onClose} severity="error" sx={{whiteSpace: 'pre-line'}}>
            {message}
        </Alert>
    </Snackbar>

export default ErrorToast
