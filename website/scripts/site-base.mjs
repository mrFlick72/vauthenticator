// Normalizes a base path to "/" or "/segment/" form.
export function normalizeBase(value) {
  const trimmed = value.trim().replace(/^\/+|\/+$/g, '');
  return trimmed ? `/${trimmed}/` : '/';
}
