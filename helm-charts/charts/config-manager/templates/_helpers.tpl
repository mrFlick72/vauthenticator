{{/*
Expand the name of the chart.
*/}}
{{- define "config-manager.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "config-manager.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "config-manager.labels" -}}
helm.sh/chart: {{ include "config-manager.chart" . }}
{{ include "config-manager.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "config-manager.selectorLabels" -}}
{{- toYaml .Values.selectorLabels }}
app.kubernetes.io/name: {{ include "config-manager.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
