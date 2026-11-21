export function sanitizeText(input: string): string {
  return input
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;")
}

export function hasXss(input: string): boolean {
  const xssPattern = /<script|javascript:|on\w+\s*=|<iframe|<object|<embed|data:text\/html/i
  return xssPattern.test(input)
}
