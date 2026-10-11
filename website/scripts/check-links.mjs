import { readdir, readFile, stat } from 'node:fs/promises';
import { resolve, join } from 'node:path';

const output = resolve('dist');
const base = '/vauthenticator/';
const files = [];
async function walk(directory) {
  for (const item of await readdir(directory, { withFileTypes: true })) {
    const path = join(directory, item.name);
    if (item.isDirectory()) await walk(path);
    else if (path.endsWith('.html')) files.push(path);
  }
}
await walk(output);
const errors = [];
let count = 0;
for (const file of files) {
  const html = await readFile(file, 'utf8');
  for (const [, attribute, value] of html.matchAll(/\b(href|src)="([^"]+)"/g)) {
    if (/^(https?:|mailto:|data:|#)/.test(value)) continue;
    count++;
    if (!value.startsWith(base)) {
      errors.push(`${file}: ${attribute}="${value}" does not use ${base}`);
      continue;
    }
    const pathname = decodeURIComponent(value.split(/[?#]/)[0].slice(base.length));
    const target = join(output, pathname);
    try {
      const entry = await stat(target);
      if (entry.isDirectory()) await stat(join(target, 'index.html'));
    } catch {
      errors.push(`${file}: missing target for ${value}`);
    }
  }
}
if (errors.length) {
  console.error(errors.join('\n'));
  process.exitCode = 1;
} else {
  console.log(`Checked ${count} internal links/assets across ${files.length} pages under ${base}.`);
}
