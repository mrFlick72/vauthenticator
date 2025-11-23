import {createTheme} from "@mui/material";


const theme = createTheme({
    components: {
        MuiGrid: {
            styleOverrides: {
                root: {
                    paddingTop: "15px"
                }
            }
        },
        MuiPaper: {
            styleOverrides: {
                root: {
                    flexGrow: 1,

                    margin: "10px",
                    padding: "10px"
                }
            }
        }
    }
});

export default theme