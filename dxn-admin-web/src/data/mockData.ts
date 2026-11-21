export const overviewStats = [
  { title: "المستخدمون النشطون اليوم", value: 1248, change: 12.5, changeType: "up" as const, icon: "Users" },
  { title: "معدل الإكمال العام", value: "68.4%", change: 4.2, changeType: "up" as const, icon: "BarChart3" },
  { title: "متوسط طول الـ Streak", value: "5.2 يوم", change: -0.8, changeType: "down" as const, icon: "Flame" },
  { title: "الشهادات الصادرة اليوم", value: 86, change: 23.1, changeType: "up" as const, icon: "Award" },
]

export const activityData = [
  { date: "1 نوفمبر", sessions: 420, activeUsers: 380 },
  { date: "3 نوفمبر", sessions: 480, activeUsers: 410 },
  { date: "5 نوفمبر", sessions: 520, activeUsers: 450 },
  { date: "7 نوفمبر", sessions: 610, activeUsers: 520 },
  { date: "9 نوفمبر", sessions: 590, activeUsers: 500 },
  { date: "11 نوفمبر", sessions: 720, activeUsers: 620 },
  { date: "13 نوفمبر", sessions: 810, activeUsers: 710 },
]

export const failedSkills = [
  { name: "الرد على اعتراض السعر", failureRate: 42 },
  { name: "احتساب النقاط PV/SV", failureRate: 35 },
  { name: "فن الدعوة والتشويق", failureRate: 28 },
  { name: "ترتيب خطوات رتبة الماس", failureRate: 24 },
  { name: "طريقة استخدام الريشي", failureRate: 18 },
]

export const inactiveUsers = [
  { id: 1, name: "أحمد محمد", lastActive: "2026-11-10", xp: 1240, role: "متدرب" },
  { id: 2, name: "سارة علي", lastActive: "2026-11-09", xp: 890, role: "متدرب" },
  { id: 3, name: "خالد العلي", lastActive: "2026-11-08", xp: 2150, role: "قائد" },
  { id: 4, name: "نورة عبدالله", lastActive: "2026-11-07", xp: 560, role: "متدرب" },
]

export const sections = [
  {
    id: 1,
    title: "أساسيات النجاح مع DXN",
    status: "published",
    bagId: 1,
    description: "الشركة وشعارها ومعنى الألوان، فكرة استثمار الاستهلاك، التسجيل، ثلاثية النجاح، والمنتجات الأساسية.",
    certificateTitle: "شهادة أساسيات DXN",
    skills: [
      {
        id: 11,
        title: "الشركة وهويتها",
        status: "published",
        levels: [
          { id: 111, title: "شعار DXN ومعنى الألوان", type: "lesson", xp: 40 },
          { id: 112, title: "تأسيس DXN وفلسفتها", type: "quiz", xp: 40 },
        ],
      },
      {
        id: 12,
        title: "التسجيل وثلاثية النجاح",
        status: "published",
        levels: [
          { id: 121, title: "استخدام المنتجات والتسجيل", type: "lesson", xp: 40 },
        ],
      },
      {
        id: 13,
        title: "المنتجات الأساسية",
        status: "published",
        levels: [
          { id: 131, title: "الريشي والسبايرولينا والمورنزي", type: "lesson", xp: 25 },
          { id: 132, title: "تحدي المستشار الصحي", type: "simulation", xp: 60 },
        ],
      },
    ],
  },
  {
    id: 2,
    title: "استثمار الاستهلاك",
    status: "published",
    bagId: 2,
    description: "كيف تحوّل ما تنفقه شهرياً على المستهلكات إلى مشروع استثماري.",
    certificateTitle: "شهادة استثمار الاستهلاك",
    requiredSectionId: 1,
    skills: [
      {
        id: 21,
        title: "تحويل المصروفات إلى استثمار",
        status: "published",
        levels: [
          { id: 211, title: "الخطوات العملية والأرقام", type: "lesson", xp: 50 },
          { id: 212, title: "محاكي التوفير الشهري", type: "simulation", xp: 60 },
        ],
      },
    ],
  },
  {
    id: 3,
    title: "نظرية خماسية الصحة",
    status: "published",
    bagId: 3,
    description: "الوجبة الواحدة، الحمضية والقلوية، الخمائر والإنزيمات، النظرية الخضراء، والتمارين الرياضية.",
    certificateTitle: "شهادة نظرية خماسية الصحة",
    requiredSectionId: 2,
    skills: [
      {
        id: 31,
        title: "الخماسية الصحية",
        status: "published",
        levels: [
          { id: 311, title: "الوجبة الواحدة والتوازن القلوي", type: "lesson", xp: 50 },
        ],
      },
      {
        id: 32,
        title: "ربط النظرية بالمنتجات",
        status: "published",
        levels: [
          { id: 321, title: "أي منتج يناسب كل حاجة؟", type: "quiz", xp: 60 },
        ],
      },
    ],
  },
  {
    id: 4,
    title: "الخطة التسويقية والرتب",
    status: "published",
    bagId: 4,
    description: "التسجيل ورقم التخفيض، نظام النقاط PV/SV، الرتب النجمية، ومحاكي حساب الأرباح.",
    certificateTitle: "شهادة الخطة التسويقية والرتب",
    requiredSectionId: 3,
    skills: [
      {
        id: 41,
        title: "نظام النقاط PV & SV",
        status: "published",
        levels: [
          { id: 411, title: "احتساب النقاط الشخصية والجماعية", type: "lesson", xp: 60 },
          { id: 412, title: "محاكي حساب الأرباح", type: "simulation", xp: 70 },
        ],
      },
      {
        id: 42,
        title: "سلم الرتب النجمية",
        status: "draft",
        levels: [
          { id: 421, title: "من موزع إلى نجم ماسي", type: "lesson", xp: 30 },
          { id: 422, title: "ترتيب خطوات الوصول للرتبة", type: "quiz", xp: 45 },
        ],
      },
    ],
  },
  {
    id: 5,
    title: "الطريق إلى النجم الماسي",
    status: "draft",
    bagId: 5,
    description: "خطة شهر بشهر للوصول للنجم الماسي خلال 5 أشهر، قائمة المعارف، فن الدعوة، والرد على الاعتراضات.",
    certificateTitle: "شهادة التخرج — النجم الماسي",
    requiredSectionId: 4,
    skills: [
      {
        id: 51,
        title: "خطة العمل الشهرية",
        status: "draft",
        levels: [
          { id: 511, title: "خطة 5 أشهر", type: "lesson", xp: 60 },
        ],
      },
      {
        id: 52,
        title: "مهارات الميدان",
        status: "draft",
        levels: [
          { id: 521, title: "قائمة المعارف وفن الدعوة", type: "lesson", xp: 40 },
          { id: 522, title: "دعوة بدون حرق الفكرة", type: "simulation", xp: 50 },
        ],
      },
      {
        id: 53,
        title: "الرد على الاعتراضات",
        status: "published",
        levels: [{ id: 531, title: "اعتراض السعر والمنتجات", type: "simulation", xp: 80 }],
      },
    ],
  },
]

export const users = [
  { id: 1, name: "أحمد محمد", dxnId: "DXN-1001", email: "ahmed@example.com", role: "متدرب", leader: "خالد العلي", xp: 1240, lastActive: "2026-11-14", status: "نشط" },
  { id: 2, name: "سارة علي", dxnId: "DXN-1002", email: "sara@example.com", role: "متدرب", leader: "خالد العلي", xp: 890, lastActive: "2026-11-09", status: "متوقف" },
  { id: 3, name: "خالد العلي", dxnId: "DXN-1003", email: "khaled@example.com", role: "قائد", leader: "—", xp: 2150, lastActive: "2026-11-08", status: "متوقف" },
  { id: 4, name: "نورة عبدالله", dxnId: "DXN-1004", email: "noura@example.com", role: "متدرب", leader: "خالد العلي", xp: 560, lastActive: "2026-11-07", status: "متوقف" },
  { id: 5, name: "محمد العمري", dxnId: "DXN-1005", email: "mohammed@example.com", role: "مدير", leader: "—", xp: 4320, lastActive: "2026-11-15", status: "نشط" },
  { id: 6, name: "فاطمة الزهراء", dxnId: "DXN-1006", email: "fatima@example.com", role: "متدرب", leader: "خالد العلي", xp: 1450, lastActive: "2026-11-14", status: "نشط" },
]

export const teams = [
  { id: 1, leader: "خالد العلي", members: 12, completion: 62, activeToday: 8 },
  { id: 2, leader: "أحمد سالم", members: 8, completion: 78, activeToday: 6 },
  { id: 3, leader: "نور الدين", members: 15, completion: 45, activeToday: 5 },
]

export const funnelData = [
  { name: "فتح المستوى", value: 1000 },
  { name: "بدء الدرس", value: 820 },
  { name: "إكمال الدرس", value: 640 },
  { name: "اجتياز الاختبار", value: 480 },
  { name: "المستوى التالي", value: 360 },
]

export const analyticsSummary = [
  { title: "Completion Rate", value: "68.4%", change: 4.2 },
  { title: "Average Time", value: "4:32 دقيقة", change: -2.1 },
  { title: "First-Attempt Success", value: "54.2%", change: 7.5 },
  { title: "Drop-off Rate", value: "31.6%", change: -3.4 },
]

export const failedQuestions = [
  { id: 1, question: "كم نقطة PV مطلوبة شهرياً؟", attempts: 340, errorRate: 45, avgTime: "12 ث" },
  { id: 2, question: "ما الفرق بين SV و PV؟", attempts: 280, errorRate: 38, avgTime: "18 ث" },
  { id: 3, question: "ترتيب خطوات رتبة الياقوتي", attempts: 410, errorRate: 36, avgTime: "24 ث" },
]

export const retentionData = [
  { day: "اليوم 1", rate: 78 },
  { day: "اليوم 7", rate: 54 },
  { day: "اليوم 30", rate: 32 },
]

export const certificates = [
  { id: "cert-s1", sectionId: 1, title: "شهادة أساسيات DXN", issuedCount: 342, template: "default", isActive: true },
  { id: "cert-s2", sectionId: 2, title: "شهادة استثمار الاستهلاك", issuedCount: 198, template: "default", isActive: true },
  { id: "cert-s3", sectionId: 3, title: "شهادة نظرية خماسية الصحة", issuedCount: 156, template: "default", isActive: true },
  { id: "cert-s4", sectionId: 4, title: "شهادة الخطة التسويقية والرتب", issuedCount: 89, template: "default", isActive: true },
  { id: "cert-s5", sectionId: 5, title: "شهادة التخرج — النجم الماسي", issuedCount: 12, template: "default", isActive: true },
]
