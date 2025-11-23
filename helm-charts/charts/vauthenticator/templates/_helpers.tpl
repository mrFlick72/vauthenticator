{{/*
Expand the name of the chart.
*/}}
{{- define "vauthenticator.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "vauthenticator.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "vauthenticator-management-ui.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}-management-ui
{{- end }}


{{- define "vauthenticator-assets.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}-assets
{{- end }}

{{- define "vauthenticator-management-ui-assets.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}-management-ui-assets
{{- end }}


{{/*
Common labels
*/}}
{{- define "vauthenticator.labels" -}}
helm.sh/chart: {{ include "vauthenticator.chart" . }}
{{ include "vauthenticator.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "vauthenticator-management-ui.labels" -}}
helm.sh/chart: {{ include "vauthenticator.chart" . }}
{{ include "vauthenticator-management-ui.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "vauthenticator-assets.labels" -}}
helm.sh/chart: {{ include "vauthenticator.chart" . }}
{{ include "vauthenticator-assets.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "vauthenticator-management-ui-assets.labels" -}}
helm.sh/chart: {{ include "vauthenticator.chart" . }}
{{ include "vauthenticator-management-ui-assets.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "vauthenticator.selectorLabels" -}}
{{- toYaml .Values.application.selectorLabels }}
app.kubernetes.io/name: {{ include "vauthenticator.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}


{{- define "vauthenticator-management-ui.selectorLabels" -}}
{{- toYaml .Values.managementUi.selectorLabels }}
app.kubernetes.io/name: {{ include "vauthenticator.name" . }}-management-ui
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "vauthenticator-assets.selectorLabels" -}}
{{- toYaml .Values.applicationAssets.selectorLabels }}
app.kubernetes.io/name: {{ include "vauthenticator.name" . }}-assets
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "vauthenticator-management-ui-assets.selectorLabels" -}}
{{- toYaml .Values.managementUiAssets.selectorLabels }}
app.kubernetes.io/name: {{ include "vauthenticator.name" . }}-management-ui-assets
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
