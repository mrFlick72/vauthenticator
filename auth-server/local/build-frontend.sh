# Fills the local file-system document storage (dist) with the Default Pages and the Default Templates, so a
# page change can be seen without running the tenant setup again. Maven builds the pages (frontend-maven-plugin
# with a pinned Node); the tenant setup does the same seeding on a new tenant.
set -e

cd ..
rm -rf dist

mkdir -p dist/static-asset/content/asset/
mkdir -p dist/email/templates
mkdir -p dist/sms/templates

../mvnw -q generate-resources

cp target/classes/pages/default/*_bundle.js dist/static-asset/content/asset/
cp src/main/resources/communication/default/email/* dist/email/templates
cp src/main/resources/communication/default/sms/* dist/sms/templates
