import React from 'react';
import ReactDOM from 'react-dom';
import {withStyles} from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import {GroupAdd} from "@material-ui/icons";
import Divider from "@material-ui/core/Divider";
import vauthenticatorStyles from "../component/styles";
import Container from "../component/Container";

const AccountPage = withStyles(vauthenticatorStyles)((props) => {
    const {classes} = props;
    return (
        <Container maxWidth="md">
            <Typography variant="h3" component="h3">
                <GroupAdd fontSize="large"/> Account Registration
            </Typography>

            <Divider/>

            <div className={classes.margin}>
                <Typography variant="h3" component="h3">
                    Thank you to have been registered soon you will receive an email
                </Typography>
            </div>
        </Container>
    );
})

if (document.getElementById('app')) {
    ReactDOM.render(<AccountPage/>, document.getElementById('app'));
}