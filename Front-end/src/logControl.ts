// Control global de logs para el front-end
export const ON_LOGS_ACTIVE = false;

export function log(...args: unknown[]): void {
  if (ON_LOGS_ACTIVE) {
    console.log(...args);
  }
}

export function warn(...args: unknown[]): void {
  if (ON_LOGS_ACTIVE) {
    console.warn(...args);
  }
}

export function error(...args: unknown[]): void {
  if (ON_LOGS_ACTIVE) {
    console.error(...args);
  }
}

export function info(...args: unknown[]): void {
  if (ON_LOGS_ACTIVE) {
    console.info(...args);
  }
}
