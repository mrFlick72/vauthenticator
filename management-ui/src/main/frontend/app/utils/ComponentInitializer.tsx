import {createRoot} from 'react-dom/client';

export default (component: JSX.Element) => {
    let htmlElement = document.getElementById('app');
    if (htmlElement) {
        const root = createRoot(htmlElement); // createRoot(container!) if you use TypeScript
        root.render(component);
    }
}