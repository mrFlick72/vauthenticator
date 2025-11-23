# vauthenticator-management-ui


### ui local environment
In order to make it simple, the ui assets build for local development take in consideration to enable the following spring configuration properties:

```yaml
  document:
    engine: file-system
    fs-base-path: dist
```

in order to be sure to have the asset files in the correct path execute this script:

```shell
rm -rf dist
rm -rf src/main/frontend/node_modules
rm -rf src/main/frontend/package-lock.json
rm -rf src/main/frontend/dist

mkdir -p dist/static-asset/content/asset/

cd src/main/frontend
npm install --legacy-peer-deps 
npm run-script build

cd dist/asset

cp * ../../../../../dist/static-asset/content/asset/

```
