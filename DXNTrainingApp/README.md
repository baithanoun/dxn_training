# DXN Training App — مشروع Android Studio

تطبيق تدريب تفاعلي لنظام DXN بأسلوب التلعيب (Gamification) وشجرة المهارات.

## المتطلبات

- Android Studio Hedgehog (2023.1.1) أو أحدث
- JDK 17
- Android SDK 34
- Kotlin 1.9+

## فتح المشروع

1. افتح Android Studio.
2. اختر **Open** وحدد مجلد `DXNTrainingApp`.
3. انتظر حتى يقوم Gradle بمزامنة المشروع.
4. شغّل التطبيق على محاكي أو جهاز حقيقي.

## بنية المشروع

```
DXNTrainingApp/
├── app/
│   ├── src/main/java/com/dxn/training/
│   │   ├── MainActivity.kt
│   │   ├── data/
│   │   │   ├── model/          # نماذج البيانات (User, Content, Progress, Team)
│   │   │   └── repository/     # Mock Data + Repositories
│   │   ├── ui/
│   │   │   ├── components/     # مكونات مشتركة (Buttons, Cards, Progress)
│   │   │   ├── navigation/     # AppNavigation + Screen routes
│   │   │   ├── screens/        # جميع الشاشات
│   │   │   ├── theme/          # Colors, Type, Theme
│   │   │   └── viewmodel/      # ViewModels
│   │   └── res/                # الموارد (الأيقونات، الألوان، الخطوط)
│   └── build.gradle.kts
├── designs/                    # رسمات الشاشات الأولية (Wireframes)
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew / gradlew.bat
```

## الشاشات المنجزة

1. Splash
2. Onboarding (3 صفحات)
3. Login / Register
4. Main Screen مع Bottom Navigation
5. Home / Skill Tree
6. Learn (متابعة التعلم)
7. Lesson Cards
8. Quiz (MCQ)
9. Chat Battle Simulator
10. Match Pairs (مطابقة البطاقات)
11. Order Sequence (ترتيب الخطوات)
12. Completion / Result
13. Profile
14. Team Dashboard
15. Admin Overview

## نظام التلعيب

- XP و Streak و Hearts
- قفل/فتح المستويات حسب التقدم
- شريط الثقة (Trust Bar) في محاكي المحادثة
- مكافآت عند اجتياز المستوى

## ملاحظات

- البيانات حالياً محلية (Mock) داخل `MockData.kt`.
- يدعم RTL بشكل كامل.
- التصميم يستخدم Jetpack Compose + Material3.
- يمكن ربطه بـ Backend لاحقاً عبر Repositories.
