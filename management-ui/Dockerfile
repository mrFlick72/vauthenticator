FROM nginx:1.27-alpine
COPY docker/default.conf.template /etc/nginx/templates/default.conf.template
COPY docker/config.json.template /etc/nginx/app-config/config.json.template
COPY docker/40-generate-app-config.sh /docker-entrypoint.d/40-generate-app-config.sh
RUN chmod +x /docker-entrypoint.d/40-generate-app-config.sh
COPY dist /usr/share/nginx/html
EXPOSE 80
