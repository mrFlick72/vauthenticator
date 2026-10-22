cd ..
rm -rf dist

mkdir -p dist/static-asset/content/asset/
mkdir -p dist/email/templates
mkdir -p dist/sms/templates

cd src/main/frontend
npm install
npm run-script build

cd dist/asset

cp * ../../../../../dist/static-asset/content/asset/

cd ../../../../../communication/default/email 
cp *  ../../../dist/email/templates

cd ../sms
cp *  ../../../dist/sms/templates
