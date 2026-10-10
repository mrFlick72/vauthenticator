import React, {useState} from "react";
import {Alert, Card, CardContent, CardHeader} from "@mui/material";
import FormInputTextField from "../../components/FormInputTextField";
import FormButton from "../../components/FormButton";
import FormSelect, {SelectOption} from "../../components/FormSelect";
import Separator from "../../components/Separator";
import LeftRightComponentRow from "../../components/LeftRightComponentRow";
import ConfirmationDialog from "../../components/ConfirmationDialog";
import IntervalInput from "./IntervalInput";
import {describeIntervalSeconds, IntervalUnit, intervalUnitOptions, toIntervalSeconds} from "./PasswordLifeCycleInterval";
import {
    errorMessageFor,
    PasswordLifeCycleAction,
    passwordLifeCycleActionOptions,
    PasswordLifeCycleBulkResult,
    removeRuleForAccountPattern,
    saveRuleForAccountPattern
} from "./PasswordLifeCycleRepository";

const defaultUnit = intervalUnitOptions.find(option => option.value === IntervalUnit.DAYS)!

type Feedback = { severity: "success" | "error", message: string }

type PendingOperation =
    { kind: "apply", pattern: string, action: PasswordLifeCycleAction, intervalSeconds: number }
    | { kind: "remove", pattern: string, action: PasswordLifeCycleAction }

const describePattern = (pattern: string) =>
    pattern === "*" ? "every account" : `all accounts matching "${pattern}"`

const confirmationMessageFor = (operation: PendingOperation) =>
    operation.kind === "apply"
        ? `Register ${operation.action} every ${describeIntervalSeconds(operation.intervalSeconds)} for ` +
        `${describePattern(operation.pattern)}? Accounts that already have this rule will have it replaced.`
        : `Remove the ${operation.action} rule from ${describePattern(operation.pattern)}?`

const BulkRulesCard: React.FC = () => {
    const [pattern, setPattern] = useState("")
    const [action, setAction] = useState<SelectOption>(passwordLifeCycleActionOptions[0])
    const [amount, setAmount] = useState("")
    const [unit, setUnit] = useState<SelectOption>(defaultUnit)
    const [pendingOperation, setPendingOperation] = useState<PendingOperation | null>(null)
    const [feedback, setFeedback] = useState<Feedback | null>(null)

    const requestApply = () => {
        if (!pattern.trim()) {
            setFeedback({severity: "error", message: "Type an account pattern first, for example *@gmail.com"})
            return
        }
        const intervalSeconds = toIntervalSeconds(amount, unit.value as IntervalUnit)
        if (intervalSeconds === null) {
            setFeedback({severity: "error", message: "The interval must be a positive whole number"})
            return
        }
        setPendingOperation({
            kind: "apply",
            pattern: pattern.trim(),
            action: action.value as PasswordLifeCycleAction,
            intervalSeconds
        })
    }

    const requestRemove = () => {
        if (!pattern.trim()) {
            setFeedback({severity: "error", message: "Type an account pattern first, for example *@gmail.com"})
            return
        }
        setPendingOperation({kind: "remove", pattern: pattern.trim(), action: action.value as PasswordLifeCycleAction})
    }

    const execute = async (operation: PendingOperation) => {
        setPendingOperation(null)
        const response = operation.kind === "apply"
            ? await saveRuleForAccountPattern(operation.pattern, operation.action, operation.intervalSeconds)
            : await removeRuleForAccountPattern(operation.pattern, operation.action)

        if (response.status === 200) {
            const result: PasswordLifeCycleBulkResult = await response.json()
            setFeedback({
                severity: "success",
                message: operation.kind === "apply"
                    ? `${operation.action} registered for ${result.matchedAccounts} accounts`
                    : `${operation.action} removed from ${result.matchedAccounts} accounts`
            })
        } else {
            setFeedback({severity: "error", message: await errorMessageFor(response)})
        }
    }

    return <Card>
        <CardHeader title="Bulk rules by account pattern" color="textSecondary"/>
        <CardContent>
            {pendingOperation && <ConfirmationDialog open={true}
                                                     maxWidth="md"
                                                     title="Confirm bulk operation"
                                                     message={confirmationMessageFor(pendingOperation)}
                                                     onExecute={() => execute(pendingOperation)}
                                                     onClose={() => setPendingOperation(null)}/>}

            <FormInputTextField id="account-pattern"
                                label="Account pattern (* matches anything, e.g. *@gmail.com; * alone is every account)"
                                required={true}
                                value={pattern}
                                handler={(event) => setPattern(event.target.value)}/>
            <FormSelect id="bulk-rule-action"
                        label="Action"
                        multi={false}
                        options={passwordLifeCycleActionOptions}
                        value={action}
                        onChangeHandler={setAction}/>
            <IntervalInput id="bulk-rule-interval"
                           amount={amount}
                           unit={unit}
                           onAmountChange={setAmount}
                           onUnitChange={setUnit}/>

            <Separator/>
            <LeftRightComponentRow leftComponentColumnsSize={2}
                                   leftComponents={<FormButton label="Remove" onClickHandler={requestRemove}/>}
                                   rightComponentsColumnSize={2}
                                   rightComponents={<FormButton label="Apply" direction="rtl"
                                                                onClickHandler={requestApply}/>}/>

            {feedback && <><Separator/><Alert severity={feedback.severity}>{feedback.message}</Alert></>}
        </CardContent>
    </Card>
}

export default BulkRulesCard
