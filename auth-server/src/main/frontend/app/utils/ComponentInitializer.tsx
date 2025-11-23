import {createRoot} from "react-dom/client";
import React from "react";

export default (component: React.ReactElement) => {
    if (document.getElementById('app')) {
        const container = document.getElementById('app');
        if (container) {
            const root = createRoot(container); // createRoot(container!) if you use TypeScript
            root.render(component);
        }
    }
}