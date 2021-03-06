import React from 'react';
import ReactDOM from 'react-dom';
import {Button, Grid, TextField, withStyles} from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import {ContactMail, Face, GroupAdd, Lock} from "@material-ui/icons";
import Divider from "@material-ui/core/Divider";
import vauthenticatorStyles from "../component/styles";
import Template from "../component/Template";
import Separator from "../component/Separator";

const AccountPage = withStyles(vauthenticatorStyles)((props) => {
    const {classes} = props;
    return (
        <Template maxWidth="sm" classes={classes}>

            <Typography variant="h3" component="h3">
                <GroupAdd fontSize="large"/> Account Registration
            </Typography>

            <Separator />

            <div className={classes.margin}>

                <form action="signup" method="post">
                    <Grid container spacing={8} alignItems="flex-end">
                        <Grid item>
                            <ContactMail fontSize="large"/>
                        </Grid>
                        <Grid item md={true} sm={true} xs={true} lg={true}>
                            <TextField id="email" name="email" type="email" label="E-Mail" fullWidth={true}
                                       variant="outlined"/>
                        </Grid>
                    </Grid>

                    <Grid container spacing={8} alignItems="flex-end">
                        <Grid item>
                            <Lock fontSize="large"/>
                        </Grid>
                        <Grid item md={true} sm={true} xs={true}>
                            <TextField id="password" name="password" type="password" label="Password"
                                       fullWidth={true} variant="outlined"/>
                        </Grid>
                    </Grid>

                    <Grid container spacing={8} alignItems="flex-end">
                        <Grid item>
                            <Face fontSize="large"/>
                        </Grid>
                        <Grid item md={true} sm={true} xs={true}>
                            <TextField id="firstName" name="firstName" type="text" label="First Name"
                                       fullWidth={true} variant="outlined"/>
                        </Grid>
                    </Grid>

                    <Grid container spacing={8} alignItems="flex-end">
                        <Grid item>
                            <Face fontSize="large"/>
                        </Grid>
                        <Grid item md={true} sm={true} xs={true}>
                            <TextField id="lastName" name="lastName" type="text" label="Last Name" fullWidth={true}
                                       variant="outlined"/>
                        </Grid>
                    </Grid>

                    <Grid style={{marginTop: '10px'}}>
                        <Divider/>
                    </Grid>

                    <div dir="rtl">
                        <Grid container alignItems="flex-end" style={{marginTop: '10px'}}>
                            <Grid item md={true} sm={true} xs={true} justify="flex-end">
                                <Button type={"submit"} variant="outlined" color="primary"
                                        style={{textTransform: "none"}}>Register to OnlyOne-Portal</Button>
                            </Grid>
                        </Grid>
                    </div>
                </form>

            </div>

        </Template>
    );
})

if (document.getElementById('app')) {
    ReactDOM.render(<AccountPage/>, document.getElementById('app'));
}