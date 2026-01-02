#! /bin/bash

rm -rf dist/
cd src/
rm -rf node_modules/
npm install
npm run-script build
