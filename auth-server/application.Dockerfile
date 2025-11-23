FROM alpine:latest AS security_provider
RUN addgroup -S application \
    && adduser -S application -G application

FROM amazoncorretto:21-al2023

COPY --from=security_provider /etc/passwd /etc/passwd
USER application

ADD ../target/vauthenticator.jar /usr/local/vauthenticator/

VOLUME /var/log/onlyone-portal

WORKDIR /usr/local/vauthenticator/

CMD ["java", "-jar", "vauthenticator.jar"]