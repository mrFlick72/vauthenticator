#!/bin/sh
set -eu

envsubst '${IDP_BASE_URL} ${CLIENT_APPLICATION_ID} ${REDIRECT_URI} ${AUTHENTICATION_CHECK_INTERVAL} ${API_BASE_URL}' \
    < /etc/nginx/app-config/config.json.template \
    > /usr/share/nginx/html/config.json
