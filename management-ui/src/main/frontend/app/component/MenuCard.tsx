import React from 'react';
import {Link} from "react-router-dom";
import {Card, CardActionArea, CardContent, CardHeader, Grid} from "@mui/material";

const classes = {
    root: {
        maxWidth: 345
    },
    media: {
        height: 140
    }
}

interface MenuCardProps {
    title?: string
    content: any
    linkTo: string
}

const MenuCard: React.FC<MenuCardProps> = ({title, content, linkTo}) => {
    return (
        <Grid item xs={12} sm={6} md={4} lg={4} xl={3}>
            <Link to={linkTo} style={{textDecoration: 'none', textAlign: 'center'}}>
                <Card style={classes.root}>
                    <CardActionArea>
                        <CardHeader
                            style={classes.media}
                            title={title}
                        />
                        <CardContent>
                            {content}
                        </CardContent>
                    </CardActionArea>
                </Card>
            </Link>
        </Grid>
    );
}

export default MenuCard