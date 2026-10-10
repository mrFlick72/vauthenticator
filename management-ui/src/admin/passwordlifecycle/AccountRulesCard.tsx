import React, {useEffect, useState} from "react";
import {Alert, Card, CardContent, CardHeader, Tooltip} from "@mui/material";
import {Delete} from "@mui/icons-material";
import FormInputTextField from "../../components/FormInputTextField";
import FormButton from "../../components/FormButton";
import FormSelect, {SelectOption} from "../../components/FormSelect";
import Separator from "../../components/Separator";
import StickyHeadTable, {StickyHeadTableColumn} from "../../components/StickyHeadTable";
import IntervalInput from "./IntervalInput";
import {describeIntervalSeconds, IntervalUnit, intervalUnitOptions, toIntervalSeconds} from "./PasswordLifeCycleInterval";
import {
    errorMessageFor,
    findRulesFor,
    PasswordLifeCycleAction,
    passwordLifeCycleActionOptions,
    PasswordLifeCycleRule,
    removeRuleFor,
    saveRuleFor
} from "./PasswordLifeCycleRepository";

const columns: StickyHeadTableColumn[] = [
    {id: 'action', label: 'Action', minWidth: 170},
    {id: 'interval', label: 'Evaluation Interval', minWidth: 120},
    {id: 'creationDate', label: 'Registered', minWidth: 170},
    {id: 'lastEvaluationDate', label: 'Last Evaluation', minWidth: 170},
    {id: 'nextEvaluationDate', label: 'Next Evaluation', minWidth: 170},
    {id: 'remove', label: 'Remove', minWidth: 80}
];

const defaultUnit = intervalUnitOptions.find(option => option.value === IntervalUnit.DAYS)!

type Feedback = { severity: "success" | "error", message: string }

interface AccountRulesCardProps {
    initialAccount: string
}

const AccountRulesCard: React.FC<AccountRulesCardProps> = ({initialAccount}) => {
    const [accountInput, setAccountInput] = useState(initialAccount)
    const [loadedAccount, setLoadedAccount] = useState("")
    const [rules, setRules] = useState<PasswordLifeCycleRule[]>([])
    const [action, setAction] = useState<SelectOption>(passwordLifeCycleActionOptions[0])
    const [amount, setAmount] = useState("")
    const [unit, setUnit] = useState<SelectOption>(defaultUnit)
    const [feedback, setFeedback] = useState<Feedback | null>(null)

    const loadRulesFor = async (account: string) => {
        if (!account.trim()) {
            setFeedback({severity: "error", message: "Type the account email first"})
            return
        }
        const response = await findRulesFor(account.trim())
        if (response.status === 200) {
            setRules(await response.json())
            setLoadedAccount(account.trim())
            setFeedback(null)
        } else {
            setRules([])
            setLoadedAccount("")
            setFeedback({severity: "error", message: await errorMessageFor(response)})
        }
    }

    useEffect(() => {
        if (initialAccount) {
            loadRulesFor(initialAccount)
        }
    }, [initialAccount]);

    const saveRule = async () => {
        const intervalSeconds = toIntervalSeconds(amount, unit.value as IntervalUnit)
        if (intervalSeconds === null) {
            setFeedback({severity: "error", message: "The interval must be a positive whole number"})
            return
        }
        const response = await saveRuleFor(loadedAccount, action.value as PasswordLifeCycleAction, intervalSeconds)
        if (response.status === 204) {
            await loadRulesFor(loadedAccount)
            setFeedback({severity: "success", message: `${action.value} saved for ${loadedAccount}`})
        } else {
            setFeedback({severity: "error", message: await errorMessageFor(response)})
        }
    }

    const removeRule = async (ruleAction: PasswordLifeCycleAction) => {
        const response = await removeRuleFor(loadedAccount, ruleAction)
        if (response.status === 204) {
            await loadRulesFor(loadedAccount)
            setFeedback({severity: "success", message: `${ruleAction} removed for ${loadedAccount}`})
        } else {
            setFeedback({severity: "error", message: await errorMessageFor(response)})
        }
    }

    const rows = rules.map(rule => ({
        code: rule.action,
        action: rule.action,
        interval: <Tooltip title={`${rule.intervalSeconds} seconds`}>
            <span>{describeIntervalSeconds(rule.intervalSeconds)}</span>
        </Tooltip>,
        creationDate: rule.creationDate,
        lastEvaluationDate: rule.lastEvaluationDate ?? "Never",
        nextEvaluationDate: rule.nextEvaluationDate,
        remove: <Delete style={{cursor: "pointer"}} onClick={() => removeRule(rule.action)}/>
    }))

    return <Card>
        <CardHeader title="Account rules" color="textSecondary"/>
        <CardContent>
            <FormInputTextField id="account"
                                label="Account EMail"
                                required={true}
                                value={accountInput}
                                handler={(event) => setAccountInput(event.target.value)}/>
            <FormButton label="Load rules" onClickHandler={() => loadRulesFor(accountInput)}/>

            {feedback && <><Separator/><Alert severity={feedback.severity}>{feedback.message}</Alert></>}

            {loadedAccount && <>
                <Separator/>
                <h3>Rules of {loadedAccount}</h3>
                {rules.length === 0
                    ? <Alert severity="info">This account has no password lifecycle rules</Alert>
                    : <StickyHeadTable columns={columns} rows={rows}/>}

                <Separator/>
                <h3>Add or replace a rule</h3>
                <FormSelect id="account-rule-action"
                            label="Action"
                            multi={false}
                            options={passwordLifeCycleActionOptions}
                            value={action}
                            onChangeHandler={setAction}/>
                <IntervalInput id="account-rule-interval"
                               amount={amount}
                               unit={unit}
                               onAmountChange={setAmount}
                               onUnitChange={setUnit}/>
                <FormButton label="Save rule" onClickHandler={saveRule}/>
            </>}
        </CardContent>
    </Card>
}

export default AccountRulesCard
