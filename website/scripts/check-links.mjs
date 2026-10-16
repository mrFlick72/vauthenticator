import { readdir, readFile, stat } from 'node:fs/promises';
import { resolve, join, relative, sep } from 'node:path';

const output = resolve('dist');
const base = '/';
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
  const page = new URL(`${base}${relative(output, file).split(sep).join('/').replace(/index\.html$/, '')}`, 'http://site');
  for (const [, attribute, value] of html.matchAll(/\b(href|src)="([^"]+)"/g)) {
    if (/^(https?:|mailto:|data:|#)/.test(value)) continue;
    count++;
    const resolved = new URL(value, page).pathname;
    if (!resolved.startsWith(base)) {
      errors.push(`${file}: ${attribute}="${value}" resolves outside ${base}`);
      continue;
    }
    const pathname = decodeURIComponent(resolved.slice(base.length));
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
