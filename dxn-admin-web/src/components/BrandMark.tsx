import { cn } from "@/lib/utils"

export function BrandMark({
  size = "sm",
  tone = "light",
}: {
  size?: "sm" | "lg"
  tone?: "light" | "dark"
}) {
  const large = size === "lg"

  const nameColor = tone === "light" ? "text-brand-plum-500" : "text-white"
  const dxnColor = tone === "light" ? "text-brand-rose-600" : "text-brand-rose-300"

  if (large) {
    return (
      <div dir="rtl" className="flex flex-col items-center gap-3 text-center">
        <img
          src={`${import.meta.env.BASE_URL}brand/logo.png`}
          alt="شعار حكاية تميّز"
          className="h-32 w-32 shrink-0"
        />
        <div className="flex flex-col items-center leading-tight">
          <span className={cn("font-bold text-3xl", nameColor)}>حكاية تميّز</span>
          <span dir="ltr" className={cn("font-semibold tracking-[0.3em] text-sm", dxnColor)}>
            DXN
          </span>
        </div>
      </div>
    )
  }

  return (
    <div dir="rtl" className="flex items-center gap-3">
      <img
        src={`${import.meta.env.BASE_URL}brand/logo.png`}
        alt="شعار حكاية تميّز"
        className="h-10 w-10 shrink-0"
      />
      <div className="flex flex-col leading-tight">
        <span className={cn("font-bold text-lg", nameColor)}>حكاية تميّز</span>
        <span dir="ltr" className={cn("font-semibold tracking-widest text-xs", dxnColor)}>
          DXN
        </span>
      </div>
    </div>
  )
}
