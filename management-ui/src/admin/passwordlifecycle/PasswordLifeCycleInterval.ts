export enum IntervalUnit {
    MINUTES = "MINUTES",
    HOURS = "HOURS",
    DAYS = "DAYS"
}

const SECONDS_PER_UNIT: Record<IntervalUnit, number> = {
    [IntervalUnit.MINUTES]: 60,
    [IntervalUnit.HOURS]: 60 * 60,
    [IntervalUnit.DAYS]: 24 * 60 * 60
}

const UNIT_LABELS: Record<IntervalUnit, [string, string]> = {
    [IntervalUnit.MINUTES]: ["minute", "minutes"],
    [IntervalUnit.HOURS]: ["hour", "hours"],
    [IntervalUnit.DAYS]: ["day", "days"]
}

export const intervalUnitOptions = [IntervalUnit.MINUTES, IntervalUnit.HOURS, IntervalUnit.DAYS]
    .map(unit => ({value: unit, label: UNIT_LABELS[unit][1]}))

/**
 * Converts an amount typed by the admin into seconds, or returns null when the amount is not a positive whole number.
 */
export function toIntervalSeconds(amount: string, unit: IntervalUnit): number | null {
    if (!/^[0-9]+$/.test(amount.trim())) {
        return null
    }
    const value = Number(amount.trim())
    return value > 0 ? value * SECONDS_PER_UNIT[unit] : null
}

/**
 * Describes an interval in the largest unit that divides it exactly, for example 7776000 -> "90 days".
 */
export function describeIntervalSeconds(intervalSeconds: number): string {
    const unit = [IntervalUnit.DAYS, IntervalUnit.HOURS, IntervalUnit.MINUTES]
        .find(candidate => intervalSeconds % SECONDS_PER_UNIT[candidate] === 0)
    if (!unit) {
        return `${intervalSeconds} ${intervalSeconds === 1 ? "second" : "seconds"}`
    }
    const amount = intervalSeconds / SECONDS_PER_UNIT[unit]
    return `${amount} ${UNIT_LABELS[unit][amount === 1 ? 0 : 1]}`
}
