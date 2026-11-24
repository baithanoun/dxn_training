package com.dxn.training.data.repository

import com.dxn.training.data.model.*

object MockData {

    val sections = listOf(
        Section(
            id = "s1",
            title = "أساسيات النجاح مع DXN",
            orderIndex = 1,
            bagId = 1,
            description = "الشركة وشعارها ومعنى الألوان، فكرة استثمار الاستهلاك، التسجيل، ثلاثية النجاح، والمنتجات الأساسية.",
            certificateTitle = "شهادة أساسيات DXN",
            skills = listOf(
                Skill(
                    id = "s1k1",
                    sectionId = "s1",
                    title = "الشركة وهويتها",
                    orderIndex = 1,
                    levels = listOf(
                        Level(
                            id = "l1",
                            skillId = "s1k1",
                            title = "شعار DXN ومعنى الألوان",
                            orderIndex = 1,
                            xpReward = 40,
                            passThreshold = 80,
                            type = "quiz",
                            cards = listOf(
                                LessonCard(
                                    id = "c1",
                                    title = "شعار DXN ومعنى الألوان",
                                    body = "الأحمر يمثل النار والشمس — الإصرار والطموح. الأخضر يمثل الأشجار والجانوديرما — النمو والتوسع. الأزرق الداكن يمثل الماء والأساس.",
                                    tip = "تأسست DXN عام 1993 على يد الدكتور ليم سيو سين."
                                ),
                                LessonCard(
                                    id = "c2",
                                    title = "فلسفة DXN",
                                    body = "DXN تقوم على مبدأ استثمار الاستهلاك: حوّل ما تنفقه شهرياً إلى فرصة استثمارية.",
                                    tip = "الجودة والاستدامة هما أساس المنتجات."
                                )
                            ),
                            rounds = listOf(
                                GameRound(
                                    id = "r1",
                                    prompt = "متى تأسست شركة DXN ومن مؤسسها؟",
                                    initialTrust = 50,
                                    options = listOf(
                                        RoundOption("o1", "1990 — د. أحمد", false, trustImpact = -10, feedback = "ليست الإجابة الصحيحة."),
                                        RoundOption("o2", "1993 — د. ليم سيو سين", true, trustImpact = +25, feedback = "إجابة صحيحة!"),
                                        RoundOption("o3", "2000 — د. محمد", false, trustImpact = -10, feedback = "ليست الإجابة الصحيحة.")
                                    )
                                )
                            )
                        )
                    )
                ),
                Skill(
                    id = "s1k2",
                    sectionId = "s1",
                    title = "التسجيل وثلاثية النجاح",
                    orderIndex = 2,
                    levels = listOf(
                        Level(
                            id = "l2",
                            skillId = "s1k2",
                            title = "استخدام المنتجات والتسجيل",
                            orderIndex = 1,
                            xpReward = 40,
                            passThreshold = 80,
                            type = "chat_battle",
                            cards = listOf(
                                LessonCard(
                                    id = "c3",
                                    title = "ثلاثية النجاح",
                                    body = "استخدم المنتجات شخصياً، شارك الفكرة، وابنِ فريقك. هذه هي السلسلة الذهبية.",
                                    tip = "المصداقية تأتي من التجربة الشخصية."
                                )
                            ),
                            rounds = listOf(
                                GameRound(
                                    id = "r2",
                                    prompt = "ما عناصر ثلاثية النجاح في DXN؟",
                                    initialTrust = 50,
                                    options = listOf(
                                        RoundOption("o5", "استخدام، مشاركة، بناء فريق", true, trustImpact = +25, feedback = "صحيح!"),
                                        RoundOption("o6", "شراء، بيع، ربح فقط", false, trustImpact = -10, feedback = "غير صحيح، النجاح يحتاج بناء علاقات.")
                                    )
                                )
                            )
                        )
                    )
                ),
                Skill(
                    id = "s1k3",
                    sectionId = "s1",
                    title = "المنتجات الأساسية",
                    orderIndex = 3,
                    levels = listOf(
                        Level(
                            id = "l3",
                            skillId = "s1k3",
                            title = "الريشي والسبايرولينا والمورنزي",
                            orderIndex = 1,
                            xpReward = 60,
                            passThreshold = 80,
                            type = "match_pairs",
                            cards = listOf(
                                LessonCard(
                                    id = "c4",
                                    title = "فطر الريشي",
                                    body = "الريشي (جانوديرما) يدعم الخلايا ويساعد على طرد السموم.",
                                    tip = "يُعرف باسم فطر الخلود في الطب التقليدي."
                                ),
                                LessonCard(
                                    id = "c5",
                                    title = "السبايرولينا والمورنزي",
                                    body = "السبايرولينا غذاء كامل غني بالبروتين. عصير المورنزي غني بمضادات الأكسدة.",
                                    tip = "الطريقة الصحيحة للاستخدام تضاعف الفائدة."
                                )
                            ),
                            rounds = listOf(
                                GameRound(
                                    id = "r3",
                                    prompt = "اربط المنتج بالفائدة:",
                                    initialTrust = 50,
                                    options = listOf(
                                        RoundOption("o7", "الريشي — طرد السموم", true, trustImpact = +25, feedback = "صحيح!"),
                                        RoundOption("o8", "المورنزي — بروتين نباتي", false, trustImpact = -10, feedback = "خطأ، المورنزي غني بمضادات الأكسدة.")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ),
        Section(
            id = "s2",
            title = "استثمار الاستهلاك",
            orderIndex = 2,
            bagId = 2,
            description = "كيف تحوّل ما تنفقه شهرياً على المستهلكات إلى مشروع استثماري.",
            certificateTitle = "شهادة استثمار الاستهلاك",
            requiredSectionId = "s1",
            skills = listOf(
                Skill(
                    id = "s2k1",
                    sectionId = "s2",
                    title = "تحويل المصروفات إلى استثمار",
                    orderIndex = 1,
                    levels = listOf(
                        Level(
                            id = "l4",
                            skillId = "s2k1",
                            title = "الخطوات العملية والأرقام",
                            orderIndex = 1,
                            xpReward = 50,
                            passThreshold = 80,
                            type = "order_sequence",
                            cards = listOf(
                                LessonCard(
                                    id = "c6",
                                    title = "استثمار الاستهلاك",
                                    body = "بدلاً من شراء منتجات متعددة من السوق، استبدلها بمنتجات DXN عالية الجودة واحصل على فرصة عمل.",
                                    tip = "احسب توفيرك الشهري وتحويله إلى دخل."
                                )
                            ),
                            rounds = listOf(
                                GameRound(
                                    id = "r4",
                                    prompt = "ما المقصود باستثمار الاستهلاك؟",
                                    initialTrust = 50,
                                    options = listOf(
                                        RoundOption("o9", "شراء منتجات DXN بدلاً من المنتجات العادية والاستفادة من العمل", true, trustImpact = +25, feedback = "صحيح!"),
                                        RoundOption("o10", "شراء أي منتج بسعر أقل", false, trustImpact = -10, feedback = "غير صحيح.")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ),
        Section(
            id = "s3",
            title = "نظرية خماسية الصحة",
            orderIndex = 3,
            bagId = 3,
            description = "الوجبة الواحدة، الحمضية والقلوية، الخمائر والإنزيمات، النظرية الخضراء، والتمارين الرياضية.",
            certificateTitle = "شهادة نظرية خماسية الصحة",
            requiredSectionId = "s2",
            skills = listOf(
                Skill(
                    id = "s3k1",
                    sectionId = "s3",
                    title = "الخماسية الصحية",
                    orderIndex = 1,
                    levels = listOf(
                        Level(
                            id = "l5",
                            skillId = "s3k1",
                            title = "الوجبة الواحدة والتوازن القلوي",
                            orderIndex = 1,
                            xpReward = 50,
                            passThreshold = 80,
                            type = "chat_battle",
                            cards = listOf(
                                LessonCard(
                                    id = "c7",
                                    title = "الوجبة الواحدة",
                                    body = "تناول وجبة غنية ومتوازنة بدلاً من وجبات متفرقة غير صحية.",
                                    tip = "المشروبات الصحية تساعد على الشبع والطاقة."
                                ),
                                LessonCard(
                                    id = "c8",
                                    title = "التوازن الحمضي القلوي",
                                    body = "الجسم يحتاج توازناً بين الأحماض والقلويات. المنتجات الخضراء تساعد على القلوية.",
                                    tip = "الخمائر والإنزيمات تدعم الهضم."
                                )
                            ),
                            rounds = listOf(
                                GameRound(
                                    id = "r5",
                                    prompt = "ما أحد أركان الخماسية الصحية؟",
                                    initialTrust = 50,
                                    options = listOf(
                                        RoundOption("o11", "الوجبة الواحدة", true, trustImpact = +25, feedback = "صحيح!"),
                                        RoundOption("o12", "تناول الوجبات السريعة", false, trustImpact = -10, feedback = "غير صحيح.")
                                    )
                                )
                            )
                        )
                    )
                ),
                Skill(
                    id = "s3k2",
                    sectionId = "s3",
                    title = "ربط النظرية بالمنتجات",
                    orderIndex = 2,
                    levels = listOf(
                        Level(
                            id = "l6",
                            skillId = "s3k2",
                            title = "أي منتج يناسب كل حاجة؟",
                            orderIndex = 1,
                            xpReward = 60,
                            passThreshold = 80,
                            type = "match_pairs",
                            cards = listOf(
                                LessonCard(
                                    id = "c9",
                                    title = "المنتجات والصحة",
                                    body = "الريشي للدعم الخلوي، السبايرولينا للتغذية، المورنزي لمضادات الأكسدة، والتمارين للنشاط.",
                                    tip = "استخدم المنتج المناسب للاستشارة."
                                )
                            ),
                            rounds = listOf(
                                GameRound(
                                    id = "r6",
                                    prompt = "أي منتج يناسب من يبحث عن طاقة يومية متوازنة؟",
                                    initialTrust = 50,
                                    options = listOf(
                                        RoundOption("o13", "السبايرولينا", true, trustImpact = +25, feedback = "صحيح!"),
                                        RoundOption("o14", "صابون DXN", false, trustImpact = -10, feedback = "غير صحيح.")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ),
        Section(
            id = "s4",
            title = "الخطة التسويقية والرتب",
            orderIndex = 4,
            bagId = 4,
            description = "التسجيل ورقم التخفيض، نظام النقاط PV/SV، الرتب النجمية، ومحاكي حساب الأرباح.",
            certificateTitle = "شهادة الخطة التسويقية والرتب",
            requiredSectionId = "s3",
            skills = listOf(
                Skill(
                    id = "s4k1",
                    sectionId = "s4",
                    title = "نظام النقاط PV & SV",
                    orderIndex = 1,
                    levels = listOf(
                        Level(
                            id = "l7",
                            skillId = "s4k1",
                            title = "احتساب النقاط الشخصية والجماعية",
                            orderIndex = 1,
                            xpReward = 60,
                            passThreshold = 80,
                            type = "order_sequence",
                            cards = listOf(
                                LessonCard(
                                    id = "c10",
                                    title = "PV و SV",
                                    body = "PV (Point Value) قيمة النقاط الشخصية. SV (Sales Value) قيمة المبيعات. كل منتج له نقاط محددة.",
                                    tip = "100 PV شهرياً شرط التأهل الأساسي."
                                )
                            ),
                            rounds = listOf(
                                GameRound(
                                    id = "r7",
                                    prompt = "كم PV مطلوب شهرياً للتأهل؟",
                                    initialTrust = 50,
                                    options = listOf(
                                        RoundOption("o15", "100 PV", true, trustImpact = +25, feedback = "صحيح!"),
                                        RoundOption("o16", "50 PV", false, trustImpact = -10, feedback = "غير صحيح.")
                                    )
                                )
                            )
                        )
                    )
                ),
                Skill(
                    id = "s4k2",
                    sectionId = "s4",
                    title = "سلم الرتب النجمية",
                    orderIndex = 2,
                    levels = listOf(
                        Level(
                            id = "l8",
                            skillId = "s4k2",
                            title = "من موزع إلى نجم ماسي",
                            orderIndex = 1,
                            xpReward = 70,
                            passThreshold = 80,
                            type = "chat_battle",
                            cards = listOf(
                                LessonCard(
                                    id = "c11",
                                    title = "الرتب النجمية",
                                    body = "الرتب: موزع → وكيل نجم → نجم ياقوتي/روبي → نجم ماسي. كل رتبة لها شروط وتأهيل.",
                                    tip = "الترقي يأتي بالتأهل الشخصي وبناء المجموعة."
                                )
                            ),
                            rounds = listOf(
                                GameRound(
                                    id = "r8",
                                    prompt = "ما ترتيب الرتب الصحيح؟",
                                    initialTrust = 50,
                                    options = listOf(
                                        RoundOption("o17", "موزع → وكيل نجم → ياقوتي → ماسي", true, trustImpact = +25, feedback = "صحيح!"),
                                        RoundOption("o18", "موزع → ماسي → ياقوتي → وكيل نجم", false, trustImpact = -10, feedback = "غير صحيح.")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ),
        Section(
            id = "s5",
            title = "الطريق إلى النجم الماسي",
            orderIndex = 5,
            bagId = 5,
            description = "خطة شهر بشهر للوصول للنجم الماسي خلال 5 أشهر، قائمة المعارف، فن الدعوة، والرد على الاعتراضات.",
            certificateTitle = "شهادة التخرج — النجم الماسي",
            requiredSectionId = "s4",
            skills = listOf(
                Skill(
                    id = "s5k1",
                    sectionId = "s5",
                    title = "خطة العمل الشهرية",
                    orderIndex = 1,
                    levels = listOf(
                        Level(
                            id = "l9",
                            skillId = "s5k1",
                            title = "خطة 5 أشهر",
                            orderIndex = 1,
                            xpReward = 60,
                            passThreshold = 80,
                            type = "match_pairs",
                            cards = listOf(
                                LessonCard(
                                    id = "c12",
                                    title = "خطة 5 أشهر",
                                    body = "الشهر 1: التعلم والتسجيل. الشهر 2-3: بناء قائمة المعارف. الشهر 4-5: التكرار والترقي.",
                                    tip = "الاستمرارية والمتابعة هما السر."
                                )
                            ),
                            rounds = listOf(
                                GameRound(
                                    id = "r9",
                                    prompt = "كم شهراً تقريباً للوصول للنجم الماسي بالخطة العملية؟",
                                    initialTrust = 50,
                                    options = listOf(
                                        RoundOption("o19", "5 أشهر", true, trustImpact = +25, feedback = "صحيح!"),
                                        RoundOption("o20", "شهر واحد", false, trustImpact = -10, feedback = "غير صحيح.")
                                    )
                                )
                            )
                        )
                    )
                ),
                Skill(
                    id = "s5k2",
                    sectionId = "s5",
                    title = "مهارات الميدان",
                    orderIndex = 2,
                    levels = listOf(
                        Level(
                            id = "l10",
                            skillId = "s5k2",
                            title = "قائمة المعارف وفن الدعوة",
                            orderIndex = 1,
                            xpReward = 70,
                            passThreshold = 80,
                            type = "order_sequence",
                            cards = listOf(
                                LessonCard(
                                    id = "c13",
                                    title = "قائمة المعارف",
                                    body = "اكتب قائمة بـ 100 شخص على الأقل وصنّفهم: مهتم بالصحة، مهتم بالدخل، محتاج للتغيير.",
                                    tip = "لا تحكم مسبقاً على اهتمامات الناس."
                                ),
                                LessonCard(
                                    id = "c14",
                                    title = "فن الدعوة بدون حرق الفكرة",
                                    body = "ابدأ بالفائدة، لا بالتفاصيل. استخدم الفضول لتحديد موعد العرض.",
                                    tip = "لا تشرح كل شيء في المحادثة الأولى."
                                )
                            ),
                            rounds = listOf(
                                GameRound(
                                    id = "r10",
                                    prompt = "كيف تدعو صديقاً للاطلاع على الفكرة دون حرقها؟",
                                    initialTrust = 50,
                                    options = listOf(
                                        RoundOption("o21", "عندي مشروع صحي ممتاز، هل عندك 20 دقيقة غداً؟", true, trustImpact = +25, feedback = "رد احترافي."),
                                        RoundOption("o22", "بيع DXN سهل والربح كبير، انضم الآن", false, trustImpact = -20, feedback = "حرق للفكرة، يخفض الثقة.")
                                    )
                                )
                            )
                        )
                    )
                ),
                Skill(
                    id = "s5k3",
                    sectionId = "s5",
                    title = "الرد على الاعتراضات",
                    orderIndex = 3,
                    levels = listOf(
                        Level(
                            id = "l11",
                            skillId = "s5k3",
                            title = "اعتراض السعر والمنتجات",
                            orderIndex = 1,
                            xpReward = 80,
                            passThreshold = 80,
                            type = "quiz",
                            cards = listOf(
                                LessonCard(
                                    id = "c15",
                                    title = "قاعدة الرد الذهبية",
                                    body = "لا تجادل العميل. الاعتراض هو طلب لمزيد من المعلومات بصيغة دفاعية.",
                                    tip = "استخدم تقنية Feel - Felt - Found."
                                ),
                                LessonCard(
                                    id = "c16",
                                    title = "التركيز على القيمة لا السعر",
                                    body = "المنتج العضوي يدوم ويعطي فائدة صحية، ويمكن الحصول عليه مجاناً عبر النقاط.",
                                    tip = "حوّل الاعتراض إلى فائدة."
                                )
                            ),
                            rounds = listOf(
                                GameRound(
                                    id = "r11",
                                    prompt = "العميل: المنتج غالي شوي...",
                                    initialTrust = 50,
                                    options = listOf(
                                        RoundOption("o23", "(Feel-Felt-Found) أتفهم شعورك...", true, trustImpact = +25, feedback = "رد احترافي يرفع الثقة."),
                                        RoundOption("o24", "لا مش غالي، أنت غلطان", false, trustImpact = -20, feedback = "رد هجومي يخفض الثقة."),
                                        RoundOption("o25", "اشتري بسرعة قبل ينتهي العرض", false, trustImpact = -15, feedback = "ضغط غير مناسب."),
                                        RoundOption("o26", "دعني أرسل لك دراسة مقارنة", false, isNeutral = true, trustImpact = 0, feedback = "رد محايد، لا يؤثر كثيراً.")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    )

    val userProgress = mutableMapOf(
        "l1" to UserProgress("u1", "l1", LevelStatus.COMPLETED, 100, 1, 100),
        "l2" to UserProgress("u1", "l2", LevelStatus.UNLOCKED, 0, 0, 0),
        "l3" to UserProgress("u1", "l3", LevelStatus.LOCKED, 0, 0, 0),
        "l4" to UserProgress("u1", "l4", LevelStatus.LOCKED, 0, 0, 0),
        "l5" to UserProgress("u1", "l5", LevelStatus.LOCKED, 0, 0, 0),
        "l6" to UserProgress("u1", "l6", LevelStatus.LOCKED, 0, 0, 0),
        "l7" to UserProgress("u1", "l7", LevelStatus.LOCKED, 0, 0, 0),
        "l8" to UserProgress("u1", "l8", LevelStatus.LOCKED, 0, 0, 0),
        "l9" to UserProgress("u1", "l9", LevelStatus.LOCKED, 0, 0, 0),
        "l10" to UserProgress("u1", "l10", LevelStatus.LOCKED, 0, 0, 0),
        "l11" to UserProgress("u1", "l11", LevelStatus.LOCKED, 0, 0, 0)
    )

    val sectionProgress = mutableMapOf(
        "s1" to SectionProgress("u1", "s1", SectionStatus.UNLOCKED),
        "s2" to SectionProgress("u1", "s2", SectionStatus.LOCKED),
        "s3" to SectionProgress("u1", "s3", SectionStatus.LOCKED),
        "s4" to SectionProgress("u1", "s4", SectionStatus.LOCKED),
        "s5" to SectionProgress("u1", "s5", SectionStatus.LOCKED)
    )
}
