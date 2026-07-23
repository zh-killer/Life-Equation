#!/usr/bin/env python3
"""Generate PNG icon files for WeChat mini program ui-icon component.

Approach:
- Emoji icons (home, shield, heart, etc.) are rendered using the system
  "Segoe UI Emoji" color font with embedded_color=True. They keep their
  native emoji colors and ignore the color prop.
- Symbol icons (chevron, close, plus, etc.) are rendered with a regular font
  in the requested color.
"""
import os
from PIL import Image, ImageDraw, ImageFont

OUT_DIR = r"C:\Users\ZH\IdeaProjects\life_equation_mini\src\static\icons"
CANVAS = 128

# Try to load fonts
EMOJI_FONT = r"C:\Windows\Fonts\seguiemj.ttf"
SYMBOL_FONT = r"C:\Windows\Fonts\seguisym.ttf"
FALLBACK_FONT = r"C:\Windows\Fonts\segoeui.ttf"


def get_font(size, emoji=False):
    if emoji:
        for fp in [EMOJI_FONT, FALLBACK_FONT]:
            if os.path.exists(fp):
                return ImageFont.truetype(fp, size)
    else:
        for fp in [SYMBOL_FONT, FALLBACK_FONT]:
            if os.path.exists(fp):
                return ImageFont.truetype(fp, size)
    return ImageFont.load_default()


# Map icon name to character (emoji or symbol)
# Emoji icons will keep their native colors.
EMOJI_ICONS = {
    "home":         "🏠",
    "hospital":     "🏥",
    "stethoscope":  "🩺",
    "virus":        "🦠",
    "pill":         "💊",
    "pill2":        "💊",
    "store":        "🏪",
    "robot":        "🤖",
    "heart":        "❤️",
    "file":         "📄",
    "message":      "💬",
    "news":         "📰",
    "calendar":     "📅",
    "folder":       "📁",
    "user":         "👤",
    "settings":     "⚙️",
    "search":       "🔍",
    "pin":          "📍",
    "location":     "📍",
    "phone":        "📞",
    "clock":        "🕐",
    "edit":         "✏️",
    "lock":         "🔒",
    "logout":       "🚪",
    "refresh":      "🔄",
    "shield":       "🛡️",
    "clipboard":    "📋",
    "activity":     "📈",
    "send":         "✉️",
    "map":          "🗺️",
    "star":         "⭐",
    "eye":          "👁️",
    "book":         "📖",
    "brain":        "🧠",
    "pulse":        "💓",
    "flask":        "⚗️",
}

# Symbol icons and their characters (rendered in the requested color)
SYMBOL_CHARS = {
    "chevronRight": "›",
    "chevronLeft":  "‹",
    "back":         "‹",
    "check":        "✓",
    "checkCircle":  "✓",
    "close":        "✕",
    "plus":         "+",
    "dot":          "•",
    "arrowUp":      "↑",
    "arrowDown":    "↓",
}

# Unique colors used by symbol icons in the project
# Also includes common defaults for fallbacks.
SYMBOL_COLORS = {
    "ffffff": "#FFFFFF",
    "c7c7cc": "#C7C7CC",
    "2dd4bf": "#2DD4BF",
    "7fdbff": "#7FDBFF",
    "ff9500": "#FF9500",
    "34c759": "#34C759",
    "ff3b30": "#FF3B30",
    "af52de": "#AF52DE",
    "8e8e93": "#8E8E93",
    "bbbbbb": "#BBBBBB",
}


def normalize_color(color):
    """Return a clean 6-digit hex color."""
    if not color:
        return "#333333"
    c = color.strip().lower()
    if c.startswith("rgba"):
        # rgba(255,255,255,0.8) -> white
        parts = c.strip("rgba() ").split(",")
        if len(parts) >= 3:
            r, g, b = int(parts[0]), int(parts[1]), int(parts[2])
            return f"#{r:02x}{g:02x}{b:02x}"
        return "#FFFFFF"
    if c.startswith("rgb"):
        parts = c.strip("rgb() ").split(",")
        if len(parts) >= 3:
            r, g, b = int(parts[0]), int(parts[1]), int(parts[2])
            return f"#{r:02x}{g:02x}{b:02x}"
    if c.startswith("#"):
        c = c[1:]
        if len(c) == 3:
            c = "".join([ch * 2 for ch in c])
        if len(c) == 4:
            c = "".join([ch * 2 for ch in c[:3]])
        return f"#{c[:6]}"
    return c


def draw_centered_text(img, char, font, fill=None, embedded_color=False):
    draw = ImageDraw.Draw(img)
    # Measure using textbbox
    bbox = draw.textbbox((0, 0), char, font=font)
    w = bbox[2] - bbox[0]
    h = bbox[3] - bbox[1]
    x = (CANVAS - w) // 2 - bbox[0]
    y = (CANVAS - h) // 2 - bbox[1]
    if fill:
        draw.text((x, y), char, fill=fill, font=font)
    else:
        draw.text((x, y), char, font=font, embedded_color=embedded_color)


def render_emoji(name, char):
    """Render a colored emoji to PNG."""
    img = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    font_size = int(CANVAS * 0.8)
    font = get_font(font_size, emoji=True)
    draw_centered_text(img, char, font, embedded_color=True)
    path = os.path.join(OUT_DIR, f"{name}.png")
    img.save(path, "PNG")
    return path


def render_symbol(name, char, color):
    """Render a monochrome symbol to PNG in the specified color."""
    img = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    font_size = int(CANVAS * 0.7)
    font = get_font(font_size, emoji=False)
    draw_centered_text(img, char, font, fill=color)
    path = os.path.join(OUT_DIR, f"{name}_{color.lstrip('#').lower()}.png")
    img.save(path, "PNG")
    return path


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    print(f"Output: {OUT_DIR}")

    print("\n=== Emoji icons (native color) ===")
    for name, char in EMOJI_ICONS.items():
        try:
            render_emoji(name, char)
        except Exception as e:
            print(f"  FAIL {name}: {e}")
    print(f"  Generated {len(EMOJI_ICONS)} emoji icons")

    print("\n=== Symbol icons (colored) ===")
    for name, char in SYMBOL_CHARS.items():
        for color_name, color_hex in SYMBOL_COLORS.items():
            try:
                render_symbol(name, char, color_hex)
            except Exception as e:
                print(f"  FAIL {name}_{color_name}: {e}")
        print(f"  {name}: OK ({len(SYMBOL_COLORS)} colors)")

    files = sorted(f for f in os.listdir(OUT_DIR) if f.endswith(".png"))
    print(f"\n=== Total: {len(files)} PNG files ===")


if __name__ == "__main__":
    main()
