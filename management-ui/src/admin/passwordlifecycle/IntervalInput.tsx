import React from "react";
import {Grid} from "@mui/material";
import FormInputTextField from "../../components/FormInputTextField";
import FormSelect, {SelectOption} from "../../components/FormSelect";
import {intervalUnitOptions} from "./PasswordLifeCycleInterval";

interface IntervalInputProps {
    id: string
    amount: string
    unit: SelectOption
    onAmountChange: (amount: string) => void
    onUnitChange: (unit: SelectOption) => void
}

const IntervalInput: React.FC<IntervalInputProps> = ({id, amount, unit, onAmountChange, onUnitChange}) =>
    <Grid container spacing={2} sx={{alignItems: "flex-end"}}>
        <Grid size={8}>
            <FormInputTextField id={`${id}-amount`}
                                label="Evaluation interval"
                                type="number"
                                required={true}
                                value={amount}
                                handler={(event) => onAmountChange(event.target.value)}/>
        </Grid>
        <Grid size={4}>
            <FormSelect id={`${id}-unit`}
                        label="Unit"
                        multi={false}
                        options={intervalUnitOptions}
                        value={unit}
                        onChangeHandler={onUnitChange}/>
        </Grid>
    </Grid>

export default IntervalInput
