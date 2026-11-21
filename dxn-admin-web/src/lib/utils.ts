import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatNumber(num: number): string {
  return new Intl.NumberFormat("ar-SA").format(num)
}

export function formatPercent(num: number): string {
  return new Intl.NumberFormat("ar-SA", { style: "percent", maximumFractionDigits: 1 }).format(num / 100)
}
