#! /bin/bash

rm -rf dist/
cd src/
rm -rf node_modules/
npm ci
npm run-script production-build
