import React, {useEffect, useRef} from "react";
import {basicSetup} from "codemirror";
import {html} from "@codemirror/lang-html";
import {indentWithTab} from "@codemirror/commands";
import {EditorState, Extension} from "@codemirror/state";
import {EditorView, keymap} from "@codemirror/view";

interface TemplateEditorProps {
    label: string,
    language: "html" | "text",
    value: string,
    onChange: (value: string) => void
}

// edits a template as exact text: what the editor shows is what onChange reports, character for character.
// Tab indents; Escape then Tab moves the focus out of the editor
const TemplateEditor: React.FC<TemplateEditorProps> = ({label, language, value, onChange}) => {
    const parent = useRef<HTMLDivElement>(null)
    const view = useRef<EditorView>(null)
    const onChangeRef = useRef(onChange)
    onChangeRef.current = onChange

    const extensions = useRef<Extension>([])

    useEffect(() => {
        extensions.current = [
            basicSetup,
            keymap.of([indentWithTab]),
            language === "html" ? html() : [],
            EditorView.contentAttributes.of({"aria-label": label}),
            EditorView.updateListener.of(update => {
                if (update.docChanged) {
                    onChangeRef.current(update.state.doc.toString())
                }
            })
        ]
        view.current = new EditorView({
            parent: parent.current!,
            state: EditorState.create({doc: value, extensions: extensions.current})
        })
        return () => view.current?.destroy()
    }, [label, language])

    // a value that does not come from typing, such as a loaded template, starts a fresh state:
    // undo must never bring back the content of a previously loaded template
    useEffect(() => {
        const current = view.current
        if (current && current.state.doc.toString() !== value) {
            current.setState(EditorState.create({doc: value, extensions: extensions.current}))
        }
    }, [value])

    return <div ref={parent} style={{minHeight: "200px", border: "1px solid rgba(0, 0, 0, 0.23)"}}/>
}

export default TemplateEditor
