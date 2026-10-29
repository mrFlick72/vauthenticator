// unique per call: tests must not share data, within or across runs
export function uniqueId() {
    return `e2e-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}
