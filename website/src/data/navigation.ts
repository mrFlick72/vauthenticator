export const repository = 'https://github.com/mrFlick72/vauthenticator';
export function sitePath(path = '') {
  return `/${path.replace(/^\//, '')}`;
}

export const features = [
  {
    title: 'OAuth2 & OpenID Connect',
    href: sitePath('features/oauth2-oidc/'),
    description: 'Connect applications through standard authentication and authorization protocols.',
    label: '01 · Protocols',
  },
  {
    title: 'Accounts & passwords',
    href: sitePath('features/accounts-passwords/'),
    description: 'Support signup, email verification, password recovery, and account lifecycle rules.',
    label: '02 · Account lifecycle',
  },
  {
    title: 'Multi-factor authentication',
    href: sitePath('features/mfa/'),
    description: 'Add an extra verification step with email, SMS, and OTP, and let account holders manage their devices.',
    label: '03 · Authentication',
  },
  {
    title: 'Management UI',
    href: sitePath('features/management/'),
    description: 'Manage clients, accounts, roles, keys, and email templates from a dedicated interface.',
    label: '04 · Administration',
  },
  {
    title: 'Customization & infrastructure',
    href: sitePath('features/customization/'),
    description: 'Customize token claims and choose persistence, key management, and document storage.',
    label: '05 · Your environment',
  },
];

export const guideLinks = [
  { title: 'Architecture', href: sitePath('architecture/') },
  { title: 'Local installation', href: sitePath('install/local/') },
  { title: 'Kubernetes & Helm', href: sitePath('install/kubernetes/') },
];
