import React from "react";
import {Grid} from "@mui/material";

interface LeftRightComponentRowProps {
    leftComponents: React.ReactNode,
    leftComponentColumnsSize: number,
    rightComponents: React.ReactNode,
    rightComponentsColumnSize: number
}

const LeftRightComponentRow: React.FC<LeftRightComponentRowProps> = ({
                                                                         leftComponents,
                                                                         leftComponentColumnsSize,
                                                                         rightComponents,
                                                                         rightComponentsColumnSize
                                                                     }) => {

    return <Grid container columns={12}>
        <Grid size={leftComponentColumnsSize}>
            {leftComponents}
        </Grid>

        <Grid size={12 - leftComponentColumnsSize - rightComponentsColumnSize}>
        </Grid>

        <Grid size={rightComponentsColumnSize}>
            {rightComponents}
        </Grid>
    </Grid>

}

export default LeftRightComponentRow
