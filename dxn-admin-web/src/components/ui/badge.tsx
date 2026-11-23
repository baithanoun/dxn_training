import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "@/lib/utils"

const badgeVariants = cva(
  "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
  {
    variants: {
      variant: {
        default:
          "border-transparent bg-brand-plum-500 text-white hover:bg-brand-plum-500/80",
        secondary:
          "border-transparent bg-brand-rose-600 text-white hover:bg-brand-rose-600/80",
        destructive:
          "border-transparent bg-semantic-error text-white hover:bg-semantic-error/80",
        outline: "text-foreground",
        success:
          "border-transparent bg-game-leaf text-white hover:bg-game-leaf/80",
        warning:
          "border-transparent bg-semantic-warning text-white hover:bg-semantic-warning/80",
        muted:
          "border-transparent bg-gray-100 text-gray-700 hover:bg-gray-200",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
)

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return (
    <div className={cn(badgeVariants({ variant }), className)} {...props} />
  )
}

export { Badge, badgeVariants }
