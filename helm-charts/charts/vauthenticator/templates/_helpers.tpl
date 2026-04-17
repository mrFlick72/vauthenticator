{{/*
Expand the name of the chart.
*/}}
{{- define "vauthenticator.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "vauthenticator.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
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

{{/*
Selector labels
*/}}
{{- define "vauthenticator.selectorLabels" -}}
{{- toYaml .Values.selectorLabels }}
app.kubernetes.io/name: {{ include "vauthenticator.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
