package com.ohtman.tv

data class Channel(val id: String, val name: String, val url: String, val emoji: String, val desc: String, val color: Long)
data class Section(val title: String, val icon: String, val channels: List<Channel>)
data class Match(val league: String, val home: String, val away: String, val day: String, val time: String, val live: Boolean, val channelId: String?)

// ⚠️ بدّل الروابط التجريبية بروابط قنواتك (m3u8 / mpd)
private const val DEMO1 = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
private const val DEMO2 = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8"

val alArabiya = Channel("alarabiya", "العربية", "https://live.alarabiya.net/alarabiapublish/alarabiya.smil/playlist.m3u8", "📰", "قناة العربية الإخبارية - بث مباشر", 0xFFB71C1C)

val sections = listOf(
    Section("الأخبار", "📰", listOf(alArabiya)),
    Section("الرياضة", "⚽", listOf(
        Channel("sp1", "رياضة 1 (تجريبي)", DEMO1, "⚽", "بث تجريبي", 0xFF1B5E20),
        Channel("sp2", "رياضة 2 (تجريبي)", DEMO2, "🏆", "بث تجريبي", 0xFF0D47A1))),
    Section("الأفلام", "🎬", listOf(
        Channel("mv1", "أفلام (تجريبي)", DEMO2, "🎬", "بث تجريبي", 0xFF4A148C))),
    Section("الأطفال", "🧸", listOf(
        Channel("kd1", "كرتون (تجريبي)", DEMO1, "🧸", "بث تجريبي", 0xFFE65100))),
)

// بيانات تجريبية - عدّلها أو اربطها بـ API لاحقاً
val matches = listOf(
    Match("دوري أبطال أوروبا", "ريال مدريد", "مانشستر سيتي", "اليوم", "22:00", true, "sp1"),
    Match("الدوري السعودي", "الهلال", "النصر", "اليوم", "20:00", false, "sp2"),
    Match("الدوري الإنجليزي", "ليفربول", "أرسنال", "غداً", "19:30", false, "sp1"),
    Match("الدوري الإسباني", "برشلونة", "أتلتيكو مدريد", "غداً", "22:00", false, null),
    Match("الدوري المصري", "الأهلي", "الزمالك", "السبت", "21:00", false, null),
)

data class Social(val label: String, val glyph: String, val color: Long, val url: String)

// ⚠️ بدّل الروابط بحساباتك الحقيقية
val socials = listOf(
    Social("تيليجرام", "✈", 0xFF229ED9, "https://t.me/ohtmanTV"),
    Social("واتساب", "W", 0xFF25D366, "https://wa.me/0000000000"),
    Social("فيسبوك", "f", 0xFF1877F2, "https://facebook.com/ohtmanTV"),
    Social("انستغرام", "IG", 0xFFC13584, "https://instagram.com/ohtmanTV"),
    Social("يوتيوب", "▶", 0xFFE53935, "https://youtube.com/@ohtmanTV"),
    Social("X", "X", 0xFF2A2A33, "https://x.com/ohtmanTV"),
)
