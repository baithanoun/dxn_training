import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "@/lib/utils"

const badgeVariants = cva(
  "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
  {
    variants: {
      variant: {
        default:
          "border-transparent bg-dxn-primary text-white hover:bg-dxn-primary/80",
        secondary:
          "border-transparent bg-dxn-secondary text-white hover:bg-dxn-secondary/80",
        destructive:
          "border-transparent bg-dxn-error text-white hover:bg-dxn-error/80",
        outline: "text-foreground",
        success:
          "border-transparent bg-dxn-success text-white hover:bg-dxn-success/80",
        warning:
          "border-transparent bg-dxn-warning text-white hover:bg-dxn-warning/80",
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
