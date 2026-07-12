FROM ubuntu:24.04      

# create a group + “system” user, no home directory
RUN useradd -ms /bin/bash application

RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates && \
    update-ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# all following instructions,
# switch to that unprivileged user for the rest of the build
USER application

WORKDIR /opt/app
COPY app /opt/app/app      
ENTRYPOINT ["/opt/app/app"]