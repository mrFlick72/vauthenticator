# VAuthenticator website

An incremental, static Astro website for people considering VAuthenticator. This project is independent of the authorization server and management UI.

## Run locally

Use Node.js 22.12 or newer. From this directory:

```sh
npm ci
npm run dev
```

Open http://localhost:4321/.

## Build and preview

```sh
npm run build
npm run check:links
npm run preview
```

The build generates static files in `dist/`, and the link check verifies that internal URLs resolve from the site root.

## Hosting

The site is deployed to S3 + CloudFront by a separate pipeline outside this repository. Whatever deploys it must upload the contents of `dist/` to the bucket root.

CloudFront requirements:

- S3 REST origin (not the website endpoint) with Origin Access Control, and a bucket policy granting `s3:GetObject` to `cloudfront.amazonaws.com` for this distribution.
- Default root object `index.html`.
- A viewer-request CloudFront Function, so directory URLs such as `/features/mfa/` serve their `index.html`:

  ```js
  function handler(event) {
    var request = event.request;
    if (request.uri.endsWith('/')) request.uri += 'index.html';
    else if (!request.uri.includes('.')) request.uri += '/index.html';
    return request;
  }
  ```

- Optionally, map 403/404 errors to `/404.html`.

## Content

- `src/pages/index.astro`: product homepage.
- `src/pages/features/`: Markdown feature summaries.
- `src/pages/install/`: installation overviews linking to project setup and configuration documentation.
- `src/layouts/`: shared website and documentation layouts.
- `src/styles/global.css`: responsive styling.

Installation pages describe the setup path and link to detailed instructions for the authorization server, management UI, and Helm charts.
