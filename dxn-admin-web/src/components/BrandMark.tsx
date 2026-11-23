import { cn } from "@/lib/utils"

export function BrandMark({
  size = "sm",
  tone = "light",
}: {
  size?: "sm" | "lg"
  tone?: "light" | "dark"
}) {
  const large = size === "lg"

  return (
    <div dir="rtl" className="flex items-center gap-3">
      <img
        src={`${import.meta.env.BASE_URL}brand/logo.png`}
        alt="شعار حكاية تميّز"
        className={cn("shrink-0", large ? "h-24 w-24" : "h-10 w-10")}
      />
      <div className="flex flex-col leading-tight">
        <span
          className={cn(
            "font-bold",
            large ? "text-3xl" : "text-lg",
            tone === "light" ? "text-brand-plum-500" : "text-white",
          )}
        >
          حكاية تميّز
        </span>
        <span
          dir="ltr"
          className={cn(
            "font-semibold tracking-widest",
            large ? "text-sm" : "text-xs",
            tone === "light" ? "text-brand-rose-600" : "text-brand-rose-300",
          )}
        >
          DXN
        </span>
      </div>
    </div>
  )
}
