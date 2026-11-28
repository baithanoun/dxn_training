# // shortcut: logo assets upscaled from a 770px JPEG, replace with vector when the owner has one
"""Regenerate the Hikaya Tamayoz PNG logo assets from the owner's JPEG.

One-off asset helper, not a production build step. Run from the repository root:

    .venv\\Scripts\\python.exe -I docs\\brand\\build_logo_assets.py

The rose ring pixels define the circle. Everything outside it is made
transparent, which drops the ivory corners and the faint gray shadow just
outside the ring. The ivory *inside* the circle is deliberately left untouched
(no thresholding), so the Arabic lettering keeps clean JPEG antialiasing.

The same run also writes the Android launcher icons from logo-512.png: a
transparent 108dp adaptive foreground, and 48dp legacy square/round icons on
#F8F8F4, at all five standard densities.
"""

from pathlib import Path

from PIL import Image, ImageDraw

HERE = Path(__file__).resolve().parent
SOURCE = HERE / "hikaya-tamayoz-logo.jpg"
LOGO = HERE / "logo-512.png"
ANDROID_RES = HERE.parent.parent / "DXNTrainingApp/app/src/main/res"

MARGIN_RATIO = 0.02  # transparent padding kept around the detected circle
MASK_SUPERSAMPLE = 4  # smooth alpha edge after LANCZOS reduction
ICON_CANVAS = 512
ICON_SAFE_ZONE = 66 / 108  # Android adaptive-icon foreground safe zone
ADAPTIVE_DP = 108
LEGACY_DP = 48
DENSITIES = (("mdpi", 1), ("hdpi", 1.5), ("xhdpi", 2), ("xxhdpi", 3), ("xxxhdpi", 4))
IVORY = (248, 248, 244, 255)  # #F8F8F4, same as @color/brand_ivory


def rose_bounds(image: Image.Image) -> tuple[int, int, int, int]:
    """Return (x0, y0, x1, y1) of pixels belonging to the rose ring."""
    pixels = image.load()
    xs: list[int] = []
    ys: list[int] = []
    for y in range(image.height):
        for x in range(image.width):
            r, g, b = pixels[x, y]
            if r > 170 and g < 130 and b > 100:
                xs.append(x)
                ys.append(y)
    if not xs:
        raise RuntimeError(f"no rose ring pixels found in {SOURCE}")
    return min(xs), min(ys), max(xs), max(ys)


def build_circle(image: Image.Image) -> tuple[Image.Image, Image.Image]:
    """Return (padded circular logo, tight masked circle), both RGBA."""
    x0, y0, x1, y1 = rose_bounds(image)
    diameter = max(x1 - x0, y1 - y0) + 1
    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2

    margin = max(1, round(diameter * MARGIN_RATIO))
    canvas_size = diameter + 2 * margin  # 2% transparent margin on each side

    base = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    base.paste(
        image.convert("RGBA"),
        (round(canvas_size / 2 - cx), round(canvas_size / 2 - cy)),
    )

    # Draw the mask oversized, then shrink it for a smooth (not jagged) rim.
    scale = MASK_SUPERSAMPLE
    big_mask = Image.new("L", (canvas_size * scale, canvas_size * scale), 0)
    pad = (canvas_size - diameter) / 2
    edge = round((pad + diameter) * scale) - 1
    ImageDraw.Draw(big_mask).ellipse(
        (round(pad * scale), round(pad * scale), edge, edge),
        fill=255,
    )
    base.putalpha(big_mask.resize((canvas_size, canvas_size), Image.Resampling.LANCZOS))

    tight = (round(pad), round(pad), round(pad + diameter), round(pad + diameter))
    return base, base.crop(tight)


def centered_mark(size: int) -> Image.Image:
    """Return the tight logo artwork scaled to 66/108 of an icon canvas."""
    with Image.open(LOGO) as logo:
        bounds = logo.getchannel("A").getbbox()
        if bounds is None:
            raise RuntimeError(f"no visible logo in {LOGO}")
        artwork = logo.crop(bounds).convert("RGBA")
    mark_px = round(size * ICON_SAFE_ZONE)
    return artwork.resize((mark_px, mark_px), Image.Resampling.LANCZOS)


def place_mark(canvas: Image.Image, mark: Image.Image) -> None:
    offset = (canvas.width - mark.width) // 2
    canvas.alpha_composite(mark, (offset, offset))


def write_foreground(size: int, destination: Path) -> None:
    """Transparent adaptive-icon foreground: only the mark is visible."""
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    place_mark(canvas, centered_mark(size))
    canvas.save(destination)


def write_legacy(size: int, round_icon: bool, destination: Path) -> None:
    """Pre-26 icon: the mark on ivory, optionally clipped to a circle."""
    canvas = Image.new("RGBA", (size, size), IVORY)
    place_mark(canvas, centered_mark(size))
    if round_icon:
        scale = MASK_SUPERSAMPLE
        mask = Image.new("L", (size * scale, size * scale), 0)
        ImageDraw.Draw(mask).ellipse(
            (0, 0, size * scale - 1, size * scale - 1), fill=255
        )
        canvas.putalpha(mask.resize((size, size), Image.Resampling.LANCZOS))
    canvas.save(destination)


def main() -> None:
    with Image.open(SOURCE) as source:
        padded, circle = build_circle(source)

    padded.resize((512, 512), Image.Resampling.LANCZOS).save(HERE / "logo-512.png")
    padded.resize((1024, 1024), Image.Resampling.LANCZOS).save(HERE / "logo-1024.png")

    mark_size = round(ICON_CANVAS * ICON_SAFE_ZONE)  # 66/108 of 512 -> 313 px
    foreground = circle.resize((mark_size, mark_size), Image.Resampling.LANCZOS)
    offset = (ICON_CANVAS - mark_size) // 2
    mark = Image.new("RGBA", (ICON_CANVAS, ICON_CANVAS), (0, 0, 0, 0))
    mark.paste(foreground, (offset, offset))
    mark.save(HERE / "logo-mark-512.png")

    for density, scale in DENSITIES:
        mipmap = ANDROID_RES / f"mipmap-{density}"
        write_foreground(round(ADAPTIVE_DP * scale), mipmap / "ic_launcher_foreground.png")
        legacy_px = round(LEGACY_DP * scale)
        write_legacy(legacy_px, False, mipmap / "ic_launcher.png")
        write_legacy(legacy_px, True, mipmap / "ic_launcher_round.png")


if __name__ == "__main__":
    main()
