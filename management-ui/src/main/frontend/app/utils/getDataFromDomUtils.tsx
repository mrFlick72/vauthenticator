export default (elementId: string): string => {
    let result: string = "{}"
    let htmlElement = document.getElementById(elementId);

    if (htmlElement) {
        result = htmlElement.innerHTML || "{}"
    }
    return result
}