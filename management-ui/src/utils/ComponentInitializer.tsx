import {createRoot} from 'react-dom/client';
import {ReactElement} from "react";

export default (component: ReactElement) => {
    let htmlElement = document.getElementById('app');
    if (htmlElement) {
        const root = createRoot(htmlElement); // createRoot(container!) if you use TypeScript
        root.render(component);
    }
}
