FROM amazoncorretto:21-al2023

ADD target/vauthenticator-management-ui.jar /usr/local/vauthenticator-management-ui/

VOLUME /var/log/onlyone-portal

WORKDIR /usr/local/vauthenticator-management-ui/

CMD ["java", "-jar", "vauthenticator-management-ui.jar"]