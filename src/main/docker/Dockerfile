FROM openjdk:11

ADD vauthenticator.jar /usr/local/vauthenticator/

VOLUME /var/log/onlyone-portal

WORKDIR /usr/local/vauthenticator/

CMD ["java", "-jar", "vauthenticator.jar"]