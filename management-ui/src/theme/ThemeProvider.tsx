import {createTheme} from "@mui/material";

const themeProvider = createTheme({
    palette: {
        primary: {
            main: '#252624',
            contrastText: '#fff',
        }
    },
    components: {
        MuiGrid: {
            styleOverrides: {
                root: {
                    marginTop: "10px",
                    paddingTop: "10px",
                    paddingLeft: "10px",
                    paddingRight: "10px"
                }
            }
        }
    }
});

export default themeProvider