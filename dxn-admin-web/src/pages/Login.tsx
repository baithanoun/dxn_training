import { useState, type FormEvent } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import {
  BatteryFull,
  BookOpen,
  CheckSquare,
  Flame,
  Gem,
  Heart,
  Lock,
  Mail,
  MessageSquare,
  Play,
  Signal,
  Trophy,
  User,
  Wifi,
} from "lucide-react";
import { useAuth } from "@/lib/auth";

const LOGO_URL = `${import.meta.env.BASE_URL}brand/logo.png`;
// shortcut: download link/version come from build env, move to app settings when the app is on Play
const APP_DOWNLOAD_URL: string = import.meta.env.VITE_APP_DOWNLOAD_URL || "";
const APP_VERSION: string = import.meta.env.VITE_APP_VERSION || "1.0.0";

const inputClass =
  "w-full rounded-xl border border-brand-plum-100 bg-white py-3 pl-4 pr-11 text-sm text-brand-text outline-none transition placeholder:text-brand-muted focus:border-brand-plum-500 focus:ring-2 focus:ring-brand-plum-500/20";

function PhoneMockup() {
  return (
    <div
      dir="rtl"
      aria-hidden="true"
      className="relative h-[540px] w-[270px] rounded-[2.75rem] border-[6px] border-black bg-black p-1.5 shadow-2xl"
    >
      <div className="absolute inset-x-0 top-0 z-20 mx-auto h-6 w-28 rounded-b-2xl bg-black" />

      <div className="relative flex h-full w-full flex-col overflow-hidden rounded-[2.3rem] bg-brand-ivory text-right">
        {/* Status bar */}
        <div className="flex h-9 items-center justify-between bg-white px-6 pt-2 text-[10px] font-bold text-brand-text">
          <span>9:41</span>
          <div className="flex items-center gap-1">
            <Signal size={12} />
            <Wifi size={12} />
            <BatteryFull size={14} />
          </div>
        </div>

        {/* Gamification bar */}
        <div className="flex items-center justify-between bg-white px-4 py-2 text-xs font-bold">
          <span className="flex items-center gap-1 text-game-xp-text">
            <Gem size={15} className="text-game-xp" fill="currentColor" />
            1250
          </span>
          <span className="flex items-center gap-1 text-game-heart">
            <Heart size={15} fill="currentColor" />4
          </span>
          <span className="flex items-center gap-1 text-game-streak">
            <Flame size={15} fill="currentColor" />5
          </span>
        </div>

        <div className="flex-1 overflow-hidden px-4 py-3">
          {/* Welcome */}
          <div className="flex items-center gap-2">
            <img src={LOGO_URL} alt="" className="h-9 w-9 shrink-0 rounded-full" />
            <div>
              <p className="text-[12px] font-bold leading-tight text-brand-plum-500">
                مرحباً بك عضو فريق حكاية تميّز
              </p>
              <p className="text-[9px] text-brand-muted">هل أنت جاهز للانطلاق في رحلة تميّز جديدة؟</p>
            </div>
          </div>

          {/* Lesson card */}
          <div className="mt-3 rounded-2xl bg-white p-3 shadow-sm">
            <div className="flex items-center justify-between gap-2">
              <div className="min-w-0">
                <span className="rounded-full bg-brand-rose-100 px-2 py-0.5 text-[9px] font-bold text-brand-rose-700">
                  المستوى الأول
                </span>
                <h3 className="mt-1 text-xs font-bold text-brand-text">معرفة المنتج</h3>
              </div>
              <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-brand-plum-500 text-white shadow-md">
                <Play size={16} className="mr-0.5" fill="currentColor" />
              </span>
            </div>
            <div dir="rtl" className="mt-3 h-2 w-full overflow-hidden rounded-full bg-brand-plum-50">
              <div className="h-full w-2/3 rounded-full bg-brand-plum-500" />
            </div>
            <p className="mt-1.5 text-[9px] text-brand-muted">أكملت 12 من 18 درساً</p>
          </div>

          {/* Scenarios */}
          <h4 className="mb-2 mt-4 text-[11px] font-bold text-brand-text">سيناريوهات التدريب</h4>
          <div className="grid grid-cols-2 gap-2">
            {[
              { label: "معركة المحادثة", Icon: MessageSquare },
              { label: "اختبار سريع", Icon: CheckSquare },
            ].map(({ label, Icon }) => (
              <div key={label} className="flex flex-col items-center rounded-xl bg-white p-2.5 shadow-sm">
                <span className="mb-1.5 rounded-full bg-brand-rose-100 p-2 text-brand-plum-500">
                  <Icon size={16} />
                </span>
                <span className="text-[10px] font-bold text-brand-text">{label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Bottom nav */}
        <div className="flex items-center justify-around border-t border-brand-plum-50 bg-white py-2 text-[9px]">
          <span className="flex flex-col items-center gap-0.5 font-bold text-brand-plum-500">
            <BookOpen size={16} />
            التعلّم
          </span>
          <span className="flex flex-col items-center gap-0.5 text-brand-muted">
            <Trophy size={16} />
            الترتيب
          </span>
          <span className="flex flex-col items-center gap-0.5 text-brand-muted">
            <User size={16} />
            حسابي
          </span>
        </div>
      </div>
    </div>
  );
}

function DownloadCard() {
  return (
    <div className="flex w-full max-w-sm items-center gap-4 rounded-2xl border border-white/10 bg-black/35 p-4 shadow-xl backdrop-blur-md">
      <div className="shrink-0 rounded-xl bg-white p-1.5">
        <img src={LOGO_URL} alt="" className="h-[76px] w-[76px]" />

      </div>
      <div className="min-w-0 flex-1 text-right">
        <p className="text-sm font-bold text-white">
          حمّل التطبيق <span dir="ltr" className="text-brand-rose-300">(v{APP_VERSION})</span>
        </p>
        <p className="mt-1 text-xs text-white/70">
          {APP_DOWNLOAD_URL ? "حمّل أحدث نسخة من التطبيق" : "التطبيق متاح قريباً"}
        </p>
        {APP_DOWNLOAD_URL && (
          <a
            href={APP_DOWNLOAD_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="mt-3 inline-flex items-center gap-1.5 rounded-lg bg-black px-3 py-1.5 text-xs font-medium text-white transition hover:bg-black/70 focus:outline-none focus:ring-2 focus:ring-brand-rose-300"
          >
            <Play size={12} aria-hidden="true" />
            Google Play
          </a>
        )}
      </div>
    </div>
  );
}

export default function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await login(email.trim(), password);
      navigate("/", { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "تعذّر تسجيل الدخول");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div dir="rtl" className="flex min-h-screen flex-col bg-brand-ivory md:flex-row">
      {/* Sign-in (right side in RTL) — plain ivory, no watermark */}
      <section className="flex w-full items-center justify-center p-6 md:w-1/2">
        <div className="w-full max-w-md rounded-3xl bg-white p-8 text-center shadow-[0_12px_40px_rgba(105,22,61,0.10)]">
          <img
            src={LOGO_URL}
            alt="شعار حكاية تميّز"
            className="mx-auto mb-5 h-28 w-28 rounded-full shadow-sm"
          />
          <h1 className="text-2xl font-bold text-brand-plum-500">مرحباً بك عضو فريق حكاية تميّز</h1>
          <p className="mt-2 text-sm text-brand-muted">هل أنت جاهز للانطلاق في رحلة تميّز جديدة؟</p>

          <form onSubmit={submit} className="mt-8 space-y-4 text-right">
            <div className="relative">
              <Mail
                size={18}
                aria-hidden="true"
                className="pointer-events-none absolute right-3.5 top-1/2 -translate-y-1/2 text-brand-muted"
              />
              <input
                id="email"
                name="email"
                type="text"
                autoComplete="username"
                aria-label="البريد الإلكتروني أو الهاتف"
                placeholder="البريد الإلكتروني أو الهاتف"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                className={inputClass}
                required
              />
            </div>

            <div className="relative">
              <Lock
                size={18}
                aria-hidden="true"
                className="pointer-events-none absolute right-3.5 top-1/2 -translate-y-1/2 text-brand-muted"
              />
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                aria-label="كلمة المرور"
                placeholder="كلمة المرور"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                className={inputClass}
                required
              />
            </div>

            {error && (
              <p role="alert" className="text-sm text-semantic-error">
                {error}
              </p>
            )}

            <button
              type="submit"
              disabled={submitting}
              className="w-full rounded-xl bg-brand-plum-500 px-4 py-3 font-bold text-white shadow-md transition hover:bg-brand-plum-600 focus:outline-none focus:ring-2 focus:ring-brand-plum-500/40 focus:ring-offset-2 disabled:opacity-60"
            >
              {submitting ? "جارٍ تسجيل الدخول…" : "تسجيل الدخول"}
            </button>
          </form>
        </div>
      </section>

      {/* App promotion (left side in RTL) — brand gradient fills the whole panel, logo watermark behind the phone */}
      <section className="relative flex w-full flex-col items-center justify-center gap-6 overflow-hidden bg-gradient-to-br from-brand-plum-700 via-brand-plum-500 to-brand-rose-600 px-6 py-10 md:w-1/2">
        <div
          aria-hidden="true"
          className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_30%_25%,rgba(224,178,198,0.35),transparent_55%)]"
        />
        <img
          src={LOGO_URL}
          alt=""
          aria-hidden="true"
          className="pointer-events-none absolute left-1/2 top-1/2 w-[130%] max-w-none -translate-x-1/2 -translate-y-1/2 select-none opacity-[0.08] mix-blend-screen md:w-[115%]"
        />

        <div className="relative z-10 flex flex-col items-center gap-6">
          <PhoneMockup />
          <DownloadCard />
        </div>
      </section>
    </div>
  );
}
