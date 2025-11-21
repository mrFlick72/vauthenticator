# Local Tenant Installer

VAuthenticator needs of a lot of infrastructure to run: postgres, dynamo, kms, redis and an email server. It can be a barrier to adopt VAuthenticator.
That's way we provide one docker compose with all the needed infrastructure and an actuator endpoint to provision a new tenant, the default credentials are below:

- default admin client application for M2M:
    - username: admin
    - password: secret
- default client application for configure the sso login for the admin ui:
    - username: vauthenticator-management-ui
    - password: secret
- default admin user
    - link: http://local.management.vauthenticator.com:8080/secure/admin/index
    - username: admin@email.com
    - password: secret

In order to have all the needed infrastructure you can avail on the [docker-compose.yml](..%2Fdocker-compose.yml)`, while the endpoint to provision a default local tenant is like below

```shell

curl -X POST http://local.api.vauthenticator.com:9091/actuator/tenant-setup

```

# local host config

add on your local hosts file the following configurations

    ```
    127.0.0.1   local.api.vauthenticator.com
    127.0.0.1   local.management.vauthenticator.com
    ```

### ui and mail template local environment
In order to make simple the ui assets build for local development take in consideration to enable the following spring configuration properties:

```yaml
  document:
    engine: file-system
    fs-base-path: dist
```

in order to be sure to have the asset files in the correct path execute this script:

```shell
cd ../auth-server
rm -rf dist

mkdir -p dist/static-asset/content/asset/
mkdir -p dist/email/templates

cd src/main/frontend
npm install
npm run-script build

cd dist/asset

cp * ../../../../../dist/static-asset/content/asset/

cd ../../../../../communication/default/email 
cp *  ../../../dist/email/templates
```

## Installation in a NON AWS Environment 

Postgres and plain java key management is an available option

The available profile are
- dynamo to let to use dynamo as persistence 
- kms to let to use kms as kay manager a plain java implementation is used otherwise
- database to let to use postgres as persistence