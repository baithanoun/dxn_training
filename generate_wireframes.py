"""Generate wireframe PNGs for DXN Training App screens.

Run with: .venv\\Scripts\\python generate_wireframes.py
"""
import os
import arabic_reshaper
from bidi.algorithm import get_display
from PIL import Image, ImageDraw, ImageFont

OUT_DIR = "designs"
os.makedirs(OUT_DIR, exist_ok=True)


def shape(text):
    """Reshape Arabic text and apply bidi so it renders correctly in Pillow."""
    if not text:
        return text
    reshaped = arabic_reshaper.reshape(text)
    return get_display(reshaped)

# Screen size
W, H = 400, 800

# Colors
C_PRIMARY = "#0A7C4E"
C_GOLD = "#D4A017"
C_ACCENT = "#1A365D"
C_SUCCESS = "#22C55E"
C_ERROR = "#EF4444"
C_BG = "#F8FAF9"
C_CARD = "#FFFFFF"
C_TEXT = "#1A202C"
C_MUTED = "#718096"
C_DISABLED = "#CBD5E0"
C_LOCKED = "#A0AEC0"


def hex_to_rgb(hex_color):
    hex_color = hex_color.lstrip("#")
    return tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))


def rgb(hex_color):
    return hex_to_rgb(hex_color)


class Drawer:
    def __init__(self, width=W, height=H, bg=C_BG):
        self.width = width
        self.height = height
        self.img = Image.new("RGB", (width, height), rgb(bg))
        self.d = ImageDraw.Draw(self.img)
        try:
            self.font_title = ImageFont.truetype("Cairo-Bold.ttf", 24)
            self.font_heading = ImageFont.truetype("Cairo-Bold.ttf", 20)
            self.font_body = ImageFont.truetype("Cairo-Regular.ttf", 16)
            self.font_small = ImageFont.truetype("Cairo-Regular.ttf", 14)
            self.font_tiny = ImageFont.truetype("Cairo-Regular.ttf", 12)
        except Exception:
            try:
                self.font_title = ImageFont.truetype("arial.ttf", 24)
                self.font_heading = ImageFont.truetype("arial.ttf", 20)
                self.font_body = ImageFont.truetype("arial.ttf", 16)
                self.font_small = ImageFont.truetype("arial.ttf", 14)
                self.font_tiny = ImageFont.truetype("arial.ttf", 12)
            except Exception:
                self.font_title = ImageFont.load_default()
                self.font_heading = self.font_title
                self.font_body = self.font_title
                self.font_small = self.font_title
                self.font_tiny = self.font_title

    def rect(self, xy, fill, radius=12, outline=None, width=1):
        self.d.rounded_rectangle(xy, radius=radius, fill=rgb(fill) if fill else None,
                                 outline=rgb(outline) if outline else None, width=width)

    def text(self, xy, text, font="body", fill=C_TEXT, anchor=None):
        f = getattr(self, f"font_{font}")
        self.d.text(xy, shape(text), font=f, fill=rgb(fill), anchor=anchor)

    def text_size(self, text, font="body"):
        f = getattr(self, f"font_{font}")
        bbox = self.d.textbbox((0, 0), shape(text), font=f)
        return bbox[2] - bbox[0], bbox[3] - bbox[1]

    def centered_text(self, y, text, font="body", fill=C_TEXT):
        w, h = self.text_size(text, font)
        self.text(((self.width - w) // 2, y), text, font=font, fill=fill)

    def rtl_text(self, x_right, y, text, font="body", fill=C_TEXT):
        w, h = self.text_size(text, font)
        self.text((x_right - w, y), text, font=font, fill=fill)

    def circle(self, xy, r, fill, outline=None, width=1):
        x, y = xy
        self.d.ellipse([x-r, y-r, x+r, y+r], fill=rgb(fill) if fill else None,
                       outline=rgb(outline) if outline else None, width=width)

    def heart(self, cx, cy, size, fill=C_ERROR):
        """Draw a simple heart shape."""
        s = size
        # two upper lobes
        self.d.ellipse([cx-s//2, cy-s//2, cx, cy+s//4], fill=rgb(fill))
        self.d.ellipse([cx, cy-s//2, cx+s//2, cy+s//4], fill=rgb(fill))
        # bottom triangle
        self.d.polygon([(cx-s//2, cy), (cx+s//2, cy), (cx, cy+s)], fill=rgb(fill))

    def star(self, cx, cy, r, fill=C_GOLD):
        """Draw a 5-point star."""
        import math
        pts = []
        for i in range(10):
            angle = math.pi / 2 + i * math.pi / 5
            rad = r if i % 2 == 0 else r // 2
            pts.append((cx + rad * math.cos(angle), cy - rad * math.sin(angle)))
        self.d.polygon(pts, fill=rgb(fill))

    def line(self, xy, fill=C_MUTED, width=1):
        self.d.line(xy, fill=rgb(fill), width=width)

    def save(self, name):
        path = os.path.join(OUT_DIR, name)
        self.img.save(path)
        print("Saved", path)

    def header(self, title, right_icon=None, show_streak=True):
        # Status bar area
        self.rect([0, 0, self.width, 56], fill=C_CARD, radius=0)
        self.rtl_text(self.width - 16, 18, title, font="body", fill=C_ACCENT)
        # Notification icon
        self.circle((32, 28), 12, fill=C_DISABLED)
        if show_streak:
            self.rect([self.width - 90, 16, self.width - 16, 40], fill="#FEF3C7", radius=12)
            self.rtl_text(self.width - 24, 19, "5 أيام", font="small", fill=C_GOLD)
            self.star(self.width - 78, 28, 8, fill=C_GOLD)

    def bottom_nav(self, active=0):
        y = self.height - 64
        self.rect([0, y, self.width, self.height], fill=C_CARD, radius=0)
        self.line([(0, y), (self.width, y)], fill="#E2E8F0", width=1)
        labels = ["الرئيسية", "تعلم", "الفريق", "ملفي"]
        tab_w = self.width // 4
        for i, label in enumerate(labels):
            x = i * tab_w + tab_w // 2
            color = C_PRIMARY if i == active else C_MUTED
            # simple icon placeholders
            fill_color = color if i == active else None
            if i == 0:
                self.rect([x-8, y+10, x+8, y+22], fill=fill_color, outline=color, radius=2)
            elif i == 1:
                self.rect([x-6, y+10, x+6, y+22], fill=fill_color, outline=color, radius=1)
            elif i == 2:
                self.circle((x-6, y+16), 5, fill=fill_color, outline=color)
                self.circle((x+6, y+16), 5, fill=fill_color, outline=color)
            else:
                self.circle((x, y+16), 8, fill=fill_color, outline=color)
            self.centered_text(y + 32, label, font="tiny", fill=color)


def draw_splash():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_PRIMARY, radius=0)
    # Logo circle
    dw.circle((W//2, H//2 - 40), 70, fill=C_GOLD)
    dw.centered_text(H//2 + 60, "DXN", font="title", fill=C_CARD)
    dw.centered_text(H//2 + 95, "تطبيق التدريب التفاعلي", font="body", fill=C_CARD)
    dw.centered_text(H - 60, "جاري التحميل...", font="small", fill="#A7F3D0")
    dw.save("01_splash.png")


def draw_onboarding_1():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_CARD, radius=0)
    dw.rect([40, 120, W-40, 320], fill="#DCFCE7", radius=20)
    dw.circle((W//2, 200), 40, fill="#00000000", outline=C_PRIMARY, width=4)
    dw.centered_text(190, "DXN", font="body", fill=C_PRIMARY)
    dw.centered_text(360, "تعلم بأسلوب Duolingo", font="heading", fill=C_ACCENT)
    dw.centered_text(400, "كبسولات معرفية قصيرة", font="body", fill=C_MUTED)
    dw.centered_text(430, "ومحفزة يومياً", font="body", fill=C_MUTED)
    dw.rect([40, H-100, W-40, H-60], fill=C_PRIMARY, radius=24)
    dw.centered_text(H-87, "ابدأ", font="body", fill=C_CARD)
    dw.save("02_onboarding_1.png")


def draw_onboarding_2():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_CARD, radius=0)
    dw.rect([40, 120, W-40, 320], fill="#FEF3C7", radius=20)
    dw.star(W//2, 200, 35, fill=C_GOLD)
    dw.centered_text(360, "اكسب XP وأوسمة", font="heading", fill=C_ACCENT)
    dw.centered_text(400, "تابع تقدمك واحتفظ بسلسلة", font="body", fill=C_MUTED)
    dw.centered_text(430, "النجاح اليومي", font="body", fill=C_MUTED)
    dw.rect([40, H-100, W-40, H-60], fill=C_PRIMARY, radius=24)
    dw.centered_text(H-87, "التالي", font="body", fill=C_CARD)
    dw.save("03_onboarding_2.png")


def draw_onboarding_3():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_CARD, radius=0)
    dw.rect([40, 120, W-40, 320], fill="#DBEAFE", radius=20)
    dw.circle((W//2-25, 200), 25, fill=C_ACCENT)
    dw.circle((W//2+25, 200), 25, fill=C_ACCENT)
    dw.circle((W//2, 170), 20, fill=C_ACCENT)
    dw.centered_text(360, "ابنِ فريقك", font="heading", fill=C_ACCENT)
    dw.centered_text(400, "تابع أعضاء فريقك وشجعهم", font="body", fill=C_MUTED)
    dw.centered_text(430, "للوصول للنجاح معاً", font="body", fill=C_MUTED)
    dw.rect([40, H-100, W-40, H-60], fill=C_PRIMARY, radius=24)
    dw.centered_text(H-87, "تسجيل الدخول", font="body", fill=C_CARD)
    dw.save("04_onboarding_3.png")


def draw_login():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_CARD, radius=0)
    dw.centered_text(120, "تسجيل الدخول", font="heading", fill=C_ACCENT)
    dw.rtl_text(W-40, 180, "البريد / رقم الهاتف", font="small", fill=C_MUTED)
    dw.rect([40, 205, W-40, 245], fill=C_BG, radius=12)
    dw.rtl_text(W-55, 215, "example@dxn.com", font="body", fill=C_DISABLED)
    dw.rtl_text(W-40, 270, "كلمة المرور", font="small", fill=C_MUTED)
    dw.rect([40, 295, W-40, 335], fill=C_BG, radius=12)
    dw.rtl_text(W-55, 305, "••••••••", font="body", fill=C_DISABLED)
    dw.rect([40, 380, W-40, 420], fill=C_PRIMARY, radius=24)
    dw.centered_text(395, "دخول", font="body", fill=C_CARD)
    dw.centered_text(460, "ليس لديك حساب؟ سجّل", font="small", fill=C_PRIMARY)
    dw.centered_text(520, "أو سجل عبر Google / Apple", font="small", fill=C_MUTED)
    dw.save("05_login.png")


def draw_register():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_CARD, radius=0)
    dw.centered_text(100, "إنشاء حساب DXN", font="heading", fill=C_ACCENT)
    fields = [("الاسم الكامل", "أحمد محمد"), ("البريد/الهاتف", "+966 5x xxx xxxx"),
              ("رقم عضوية DXN", "DXN-123456"), ("كود القائد", "اختياري")]
    y = 160
    for label, placeholder in fields:
        dw.rtl_text(W-40, y, label, font="small", fill=C_MUTED)
        dw.rect([40, y+25, W-40, y+65], fill=C_BG, radius=12)
        dw.rtl_text(W-55, y+35, placeholder, font="body", fill=C_DISABLED)
        y += 80
    dw.rect([40, y+20, W-40, y+60], fill=C_PRIMARY, radius=24)
    dw.centered_text(y+35, "إنشاء الحساب", font="body", fill=C_CARD)
    dw.save("06_register.png")


def draw_home_skill_tree():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_BG, radius=0)
    dw.header("مرحباً أحمد")
    # Progress summary
    dw.rect([16, 70, W-16, 150], fill=C_CARD, radius=16)
    dw.rtl_text(W-32, 88, "تقدمك اليومي", font="body", fill=C_ACCENT)
    dw.rect([32, 120, W-32, 136], fill="#E2E8F0", radius=8)
    dw.rect([32, 120, W//2, 136], fill=C_PRIMARY, radius=8)
    dw.rtl_text(W-32, 145, "5 أيام متتالية  |  XP 1,240", font="small", fill=C_MUTED)
    # Skill tree title
    dw.rtl_text(W-24, 170, "شجرة المهارات", font="heading", fill=C_ACCENT)
    # Nodes
    nodes = [
        ("مفهوم البيع المباشر", C_SUCCESS, True),
        ("المنتجات الأساسية", C_PRIMARY, False),
        ("نظام النقاط", C_LOCKED, True),
        ("سلم الرتب", C_LOCKED, True),
        ("قائمة المعارف", C_LOCKED, True),
        ("فن الدعوة", C_LOCKED, True),
        ("الرد على الاعتراضات", C_LOCKED, True),
    ]
    y = 210
    for title, color, locked in nodes:
        dw.rect([24, y, W-24, y+52], fill=C_CARD, radius=12)
        dw.circle((50, y+26), 16, fill=color if not locked else C_DISABLED)
        if locked:
            dw.rect((46, y+22, 54, y+30), fill=C_CARD, radius=2)
            dw.circle((52, y+22), 2, fill=C_CARD)
        elif color == C_SUCCESS:
            dw.text((46, y+18), "V", font="small", fill=C_CARD)
        else:
            dw.text((46, y+18), ">", font="small", fill=C_CARD)
        dw.rtl_text(W-48, y+16, title, font="body", fill=C_TEXT if not locked else C_LOCKED)
        y += 64
    dw.rect([W-80, H-140, W-24, H-88], fill=C_GOLD, radius=28)
    dw.text((W-66, H-124), ">", font="heading", fill=C_CARD)
    dw.bottom_nav(active=0)
    dw.save("07_home_skill_tree.png")


def draw_lesson_card():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_BG, radius=0)
    dw.rect([0, 0, W, 56], fill=C_CARD, radius=0)
    dw.rtl_text(W-16, 18, "مفهوم البيع المباشر", font="body", fill=C_ACCENT)
    dw.text((20, 18), "X", font="body", fill=C_MUTED)
    # Progress bar
    dw.rect([40, 72, W-40, 86], fill="#E2E8F0", radius=7)
    dw.rect([40, 72, W//2+40, 86], fill=C_PRIMARY, radius=7)
    dw.rtl_text(W-40, 96, "بطاقة 2 من 3", font="small", fill=C_MUTED)
    # Card
    dw.rect([24, 130, W-24, 520], fill=C_CARD, radius=20)
    dw.rect([48, 160, W-48, 280], fill="#DCFCE7", radius=16)
    dw.circle((W//2, 220), 35, fill="#00000000", outline=C_PRIMARY, width=4)
    dw.centered_text(210, "DXN", font="body", fill=C_PRIMARY)
    dw.rtl_text(W-48, 310, "البيع المباشر", font="heading", fill=C_ACCENT)
    dw.rtl_text(W-48, 345, "هو بيع المنتجات مباشرة", font="body", fill=C_TEXT)
    dw.rtl_text(W-48, 370, "للمستهلك دون وسائط", font="body", fill=C_TEXT)
    dw.rtl_text(W-48, 395, "تقليدية.", font="body", fill=C_TEXT)
    dw.rtl_text(W-48, 440, "DXN تأسست عام 1993 وتستخدم", font="small", fill=C_MUTED)
    dw.rtl_text(W-48, 465, "نظام شبكة تسويقية ناجح.", font="small", fill=C_MUTED)
    # Next button
    dw.rect([40, 560, W-40, 600], fill=C_PRIMARY, radius=24)
    dw.centered_text(575, "التالي", font="body", fill=C_CARD)
    dw.save("08_lesson_card.png")


def draw_quiz_mcq():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_BG, radius=0)
    dw.rect([0, 0, W, 56], fill=C_CARD, radius=0)
    dw.rtl_text(W-16, 18, "اختبار سريع", font="body", fill=C_ACCENT)
    dw.text((20, 18), "X", font="body", fill=C_MUTED)
    # Hearts
    for i in range(3):
        dw.heart(W-28 - i*28, 78, 10, fill=C_ERROR)
    # Question
    dw.rtl_text(W-24, 110, "متى تأسست شركة DXN؟", font="heading", fill=C_ACCENT)
    options = ["1990", "1993", "2000", "2010"]
    y = 170
    for opt in options:
        dw.rect([24, y, W-24, y+56], fill=C_CARD, radius=16)
        dw.rtl_text(W-48, y+18, opt, font="body", fill=C_TEXT)
        dw.circle((50, y+28), 14, fill=C_BG, outline=C_DISABLED, width=2)
        y += 72
    # Check button disabled
    dw.rect([40, y+20, W-40, y+60], fill=C_DISABLED, radius=24)
    dw.centered_text(y+35, "تحقق", font="body", fill=C_CARD)
    dw.save("09_quiz_mcq.png")


def draw_chat_battle():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_BG, radius=0)
    dw.rect([0, 0, W, 110], fill=C_CARD, radius=0)
    dw.circle((50, 45), 24, fill=C_DISABLED)
    dw.rtl_text(W-80, 28, "أحمد — صديق مهتم", font="body", fill=C_ACCENT)
    dw.rtl_text(W-80, 52, "متردد", font="small", fill=C_MUTED)
    # Trust bar
    dw.rtl_text(W-24, 78, "الثقة", font="tiny", fill=C_MUTED)
    dw.rect([24, 96, W-80, 108], fill="#E2E8F0", radius=6)
    dw.rect([24, 96, W//2, 108], fill=C_GOLD, radius=6)
    dw.rtl_text(W-24, 92, "50%", font="tiny", fill=C_ACCENT)
    # Hearts
    for i in range(3):
        dw.heart(28 + i*22, 86, 8, fill=C_ERROR)
    # Chat bubbles
    dw.rect([48, 130, W-24, 190], fill=C_CARD, radius=16)
    dw.rtl_text(W-40, 145, "المنتج غالي شوي...", font="body", fill=C_TEXT)
    dw.rect([24, 210, W-48, 280], fill=C_PRIMARY, radius=16)
    dw.rtl_text(W-72, 225, "أتفهم. بس خليني أقولك", font="body", fill=C_CARD)
    dw.rtl_text(W-72, 250, "شيء اكتشفته", font="body", fill=C_CARD)
    # Options
    dw.rtl_text(W-24, 310, "اختر ردك التالي:", font="body", fill=C_ACCENT)
    options = [
        "(Feel-Felt-Found) أتفهم شعورك...",
        "لا مش غالي، أنت غلطان",
        "اشتري بسرعة قبل ينتهي العرض"
    ]
    y = 350
    colors = [C_CARD, C_CARD, C_CARD]
    for opt, color in zip(options, colors):
        dw.rect([24, y, W-24, y+60], fill=color, radius=14)
        dw.rtl_text(W-40, y+20, opt, font="small", fill=C_TEXT)
        y += 76
    dw.save("10_chat_battle.png")


def draw_match_pairs():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_BG, radius=0)
    dw.rect([0, 0, W, 56], fill=C_CARD, radius=0)
    dw.rtl_text(W-16, 18, "مطابقة البطاقات", font="body", fill=C_ACCENT)
    dw.rtl_text(W-16, 70, "3  |  00:45", font="small", fill=C_MUTED)
    for i in range(3):
        dw.heart(W-48 - i*16, 78, 6, fill=C_ERROR)
    dw.rtl_text(W-24, 110, "اربط كل منتج بفائدته:", font="heading", fill=C_ACCENT)
    # Left column items
    left_items = ["الريشي", "السبايرولينا", "المورنزي"]
    right_items = ["طرد سموم", "بروتين نباتي", "مضادات أكسدة"]
    y = 160
    for li, ri in zip(left_items, right_items):
        dw.rect([24, y, W//2-12, y+50], fill=C_CARD, radius=12)
        dw.rtl_text(W//2-24, y+15, li, font="body", fill=C_TEXT)
        dw.rect([W//2+12, y, W-24, y+50], fill=C_CARD, radius=12)
        dw.rtl_text(W-36, y+15, ri, font="body", fill=C_TEXT)
        y += 66
    dw.save("11_match_pairs.png")


def draw_order_sequence():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_BG, radius=0)
    dw.rect([0, 0, W, 56], fill=C_CARD, radius=0)
    dw.rtl_text(W-16, 18, "ترتيب الخطوات", font="body", fill=C_ACCENT)
    for i in range(3):
        dw.heart(W-28 - i*20, 78, 8, fill=C_ERROR)
    dw.rtl_text(W-24, 110, "رتب خطوات عرض الفكرة:", font="heading", fill=C_ACCENT)
    steps = ["الدعوة والتشويق", "عرض المنتج", "شرح الخطة", "طلب القرار"]
    y = 160
    for i, step in enumerate(steps, 1):
        dw.rect([24, y, W-24, y+56], fill=C_CARD, radius=14)
        dw.circle((50, y+28), 18, fill=C_PRIMARY)
        dw.text((43, y+20), str(i), font="body", fill=C_CARD)
        dw.rtl_text(W-40, y+18, step, font="body", fill=C_TEXT)
        y += 70
    dw.rect([40, y+20, W-40, y+60], fill=C_PRIMARY, radius=24)
    dw.centered_text(y+35, "تحقق", font="body", fill=C_CARD)
    dw.save("12_order_sequence.png")


def draw_completion_result():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_BG, radius=0)
    dw.rect([0, 0, W, 56], fill=C_CARD, radius=0)
    dw.rtl_text(W-16, 18, "اكتمل المستوى!", font="body", fill=C_ACCENT)
    # Celebration area
    dw.rect([24, 80, W-24, 340], fill=C_CARD, radius=20)
    dw.star(W//2, 130, 40, fill=C_GOLD)
    dw.centered_text(190, "ممتاز!", font="heading", fill=C_ACCENT)
    dw.centered_text(230, "+120 XP", font="heading", fill=C_GOLD)
    dw.centered_text(270, "إتقان من أول محاولة ×2", font="small", fill=C_MUTED)
    # Skill bar filled
    dw.rect([48, 300, W-48, 318], fill="#E2E8F0", radius=9)
    dw.rect([48, 300, W-48, 318], fill=C_GOLD, radius=9)
    # Buttons
    dw.rect([40, 380, W-40, 420], fill=C_PRIMARY, radius=24)
    dw.centered_text(395, "المستوى التالي", font="body", fill=C_CARD)
    dw.rect([40, 440, W-40, 480], fill=C_CARD, radius=24)
    dw.centered_text(455, "مراجعة البطاقات", font="body", fill=C_PRIMARY)
    dw.rect([40, 500, W-40, 540], fill="#DBEAFE", radius=24)
    dw.centered_text(515, "مشاركة الإنجاز", font="body", fill=C_ACCENT)
    dw.save("13_completion_result.png")


def draw_profile():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_BG, radius=0)
    dw.header("ملفي")
    dw.rect([16, 70, W-16, 220], fill=C_CARD, radius=16)
    dw.circle((W//2, 130), 45, fill=C_DISABLED)
    dw.centered_text(190, "أحمد محمد", font="heading", fill=C_ACCENT)
    dw.centered_text(220, "DXN-123456", font="small", fill=C_MUTED)
    # Stats row
    stats = [("5", "Streak"), ("1,240", "XP"), ("12", "شهادة")]
    x = 40
    icons = [lambda dw, cx, cy: dw.star(cx, cy, 8, fill=C_GOLD),
             lambda dw, cx, cy: dw.circle((cx, cy), 8, fill=C_GOLD),
             lambda dw, cx, cy: dw.rect([cx-6, cy-8, cx+6, cy+8], fill=C_GOLD, radius=2)]
    for (val, label), icon_fn in zip(stats, icons):
        dw.rect([x, 240, x+100, 290], fill=C_BG, radius=12)
        icon_fn(dw, x+50, 248)
        dw.centered_text_at(x+50, 252, val, font="small", fill=C_CARD)
        dw.centered_text_at(x+50, 275, label, font="tiny", fill=C_MUTED)
        x += 115
    # Menu items
    items = ["حقيبة المتدرب", "الأوسمة", "الشهادات", "الإعدادات", "تسجيل الخروج"]
    y = 320
    for item in items:
        dw.rect([16, y, W-16, y+50], fill=C_CARD, radius=12)
        dw.rtl_text(W-40, y+15, item, font="body", fill=C_TEXT)
        dw.text((20, y+15), "<", font="body", fill=C_MUTED)
        y += 60
    dw.bottom_nav(active=3)
    dw.save("14_profile.png")


def draw_team_dashboard():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_BG, radius=0)
    dw.header("فريقي")
    # Team summary
    dw.rect([16, 70, W-16, 150], fill=C_CARD, radius=16)
    dw.rtl_text(W-32, 88, "ملخص الفريق", font="body", fill=C_ACCENT)
    dw.rtl_text(W-32, 120, "الأعضاء: 24  |  معدل الإكمال: 68%", font="small", fill=C_MUTED)
    dw.rect([32, 135, W-32, 140], fill="#E2E8F0", radius=3)
    dw.rect([32, 135, W//2+60, 140], fill=C_PRIMARY, radius=3)
    # Filter
    dw.rect([16, 170, W//2-8, 200], fill=C_PRIMARY, radius=12)
    dw.centered_text_in(W//2-8, 16, 170, "نشط", font="small", fill=C_CARD)
    dw.rect([W//2+8, 170, W-16, 200], fill=C_CARD, radius=12)
    dw.centered_text_in(W//2-8, W//2+8, 170, "متوقف >3 أيام", font="small", fill=C_MUTED)
    # Members list
    members = [("خالد", "85%", C_SUCCESS), ("سارة", "62%", C_PRIMARY),
               ("محمد", "30%", C_ERROR), ("نور", "0%", C_LOCKED)]
    y = 220
    for name, pct, color in members:
        dw.rect([16, y, W-16, y+70], fill=C_CARD, radius=12)
        dw.circle((44, y+35), 18, fill=C_DISABLED)
        dw.rtl_text(W-70, y+12, name, font="body", fill=C_TEXT)
        dw.rtl_text(W-70, y+38, f"إنجاز {pct}", font="small", fill=C_MUTED)
        dw.rect([70, y+48, 110, y+56], fill="#E2E8F0", radius=4)
        dw.rect([70, y+48, 70+int(40*int(pct[:-1])/100), y+56], fill=color, radius=4)
        y += 82
    dw.rect([40, y+10, W-40, y+50], fill=C_GOLD, radius=24)
    dw.centered_text(y+25, "إرسال تشجيع", font="body", fill=C_CARD)
    dw.bottom_nav(active=2)
    dw.save("15_team_dashboard.png")


def draw_admin_content():
    dw = Drawer()
    dw.rect([0, 0, W, H], fill=C_BG, radius=0)
    dw.rect([0, 0, W, 56], fill=C_CARD, radius=0)
    dw.rtl_text(W-16, 18, "لوحة الإدارة", font="body", fill=C_ACCENT)
    dw.rect([16, 70, W-16, 140], fill=C_PRIMARY, radius=16)
    dw.rtl_text(W-32, 88, "المستخدمون النشطون", font="body", fill=C_CARD)
    dw.rtl_text(W-32, 118, "1,240  ↑ 12%", font="heading", fill=C_CARD)
    dw.rect([16, 155, W//2-8, 230], fill=C_CARD, radius=16)
    dw.centered_text_in(W//2-8, 16, 170, "معدل الإكمال", font="small", fill=C_MUTED)
    dw.centered_text_in(W//2-8, 16, 195, "68%", font="heading", fill=C_ACCENT)
    dw.rect([W//2+8, 155, W-16, 230], fill=C_CARD, radius=16)
    dw.centered_text_in(W//2-8, W//2+8, 170, "متوسط Streak", font="small", fill=C_MUTED)
    dw.centered_text_in(W//2-8, W//2+8, 195, "4.2", font="heading", fill=C_ACCENT)
    dw.rtl_text(W-24, 250, "أكثر المهارات رسوباً", font="body", fill=C_ACCENT)
    skills = ["نظام النقاط", "الرد على الاعتراضات", "سلم الرتب"]
    y = 280
    for skill in skills:
        dw.rect([16, y, W-16, y+50], fill=C_CARD, radius=12)
        dw.rtl_text(W-40, y+15, skill, font="body", fill=C_TEXT)
        dw.rtl_text(40, y+15, "35% رسوب", font="small", fill=C_ERROR)
        y += 60
    dw.save("16_admin_overview.png")


# Helper methods for Drawer used above
def centered_text_at(self, cx, y, text, font="body", fill=C_TEXT):
    w, h = self.text_size(text, font)
    self.text((cx - w//2, y), text, font=font, fill=fill)


def centered_text_in(self, width, x, y, text, font="body", fill=C_TEXT):
    w, h = self.text_size(text, font)
    self.text((x + (width - w)//2, y), text, font=font, fill=fill)


Drawer.centered_text_at = centered_text_at
Drawer.centered_text_in = centered_text_in


def main():
    draw_splash()
    draw_onboarding_1()
    draw_onboarding_2()
    draw_onboarding_3()
    draw_login()
    draw_register()
    draw_home_skill_tree()
    draw_lesson_card()
    draw_quiz_mcq()
    draw_chat_battle()
    draw_match_pairs()
    draw_order_sequence()
    draw_completion_result()
    draw_profile()
    draw_team_dashboard()
    draw_admin_content()
    print("All wireframes generated in", OUT_DIR)


if __name__ == "__main__":
    main()
