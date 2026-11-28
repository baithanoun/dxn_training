<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>انضم للفريق — {{ e($teamName) }}</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, sans-serif; background: #faf7f2; color: #2b1d3a; margin: 0; padding: 24px; line-height: 1.8; }
        .wrap { max-width: 760px; margin: 0 auto; background: #fff; padding: 32px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,.06); }
        h1 { color: #5b2c6f; margin-top: 0; }
        .team { color: #5b2c6f; font-weight: 700; font-size: 18px; }
        .tagline { color: #6b5b75; margin-top: 4px; }
        .invite { background: #f3ecf7; border: 1px solid #d8c4e3; padding: 16px; border-radius: 10px; margin: 20px 0; }
        .leader-id { display: inline-block; direction: ltr; background: #fff; border: 1px solid #d8c4e3; padding: 6px 12px; border-radius: 8px; font-weight: 700; color: #5b2c6f; margin-top: 8px; }
        .download { display: inline-block; background: #5b2c6f; color: #fff; text-decoration: none; padding: 12px 20px; border-radius: 10px; font-weight: 700; margin-top: 8px; }
        .download:hover { background: #4a235a; }
        .note { color: #4a3d52; margin-top: 20px; }
    </style>
</head>
<body>
<div class="wrap">
    <div class="team">{{ e($teamName) }}</div>
    @if(filled($teamTagline))
        <div class="tagline">{{ e($teamTagline) }}</div>
    @endif

    <h1>انضم إلى الفريق</h1>

    <div class="invite">
        <div>انضم عبر القائد</div>
        <div class="leader-id">{{ e($dxnId) }}</div>
    </div>

    @if(filled($downloadUrl))
        <a class="download" href="{{ e($downloadUrl) }}">تحميل التطبيق</a>
    @else
        <span class="download" role="link" aria-disabled="true">تحميل التطبيق</span>
    @endif

    <p class="note">أدخل رقم القائد هذا عند التسجيل.</p>
</div>
</body>
</html>
