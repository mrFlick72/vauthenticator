# vauthenticator-management-ui



### ui local environment

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

to access to the application you can use the following link: **https://management.vauthenticator.com/secure/admin/index**