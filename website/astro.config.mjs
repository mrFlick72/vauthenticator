import { defineConfig } from 'astro/config';
import { normalizeBase } from './scripts/site-base.mjs';

// Defaults target https://vauthenticator.com; override SITE_URL/SITE_BASE for other hosts.
const site = process.env.SITE_URL ?? 'https://vauthenticator.com';
const base = normalizeBase(process.env.SITE_BASE ?? '/');

export default defineConfig({
  site,
  base,
  output: 'static',
  trailingSlash: 'always',
});
