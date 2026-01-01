import React from 'react';
import {Checkbox, FormControl, FormControlLabel, FormGroup, FormLabel} from "@mui/material";

const classes = {
    root: {
        display: 'flex',
    },
    formControl: {
        margin: "10px"
    }

};

interface CheckboxesGroupProps {
    id: string,
    legend: string,
    choicesRegistry: { [key: string]: boolean },
    handler: (input : any) => void
}

const CheckboxesGroup: React.FC<CheckboxesGroupProps> = ({id, legend, choicesRegistry, handler}) => {
    const choicesRegistryKeys = Object.keys(choicesRegistry)
    return (
        <div style={classes.root}>
            <FormControl id={id} name={id} component="fieldset" style={classes.formControl}>
                <FormLabel component="legend">{legend}</FormLabel>
                <FormGroup>
                    {choicesRegistryKeys.map(choiceKey => {
                            return <FormControlLabel
                                control={<Checkbox checked={choicesRegistry[choiceKey]} onChange={handler}
                                                   name={choiceKey}/>}
                                label={choiceKey}
                            />
                        }
                    )}
                </FormGroup>
            </FormControl>
        </div>
    );
}

export default CheckboxesGroup