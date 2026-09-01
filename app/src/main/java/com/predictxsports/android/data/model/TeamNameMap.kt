package com.predictxsports.android.data.model

/**
 * 中英文隊名對照表 — 與 iOS TeamNameMap 100% 對齊
 *
 * iOS 原始碼：TeamNameMap.swift
 */
object TeamNameMap {
    private val mapping: Map<String, String> = mapOf(
        // ===== NBA (30 隊完整) =====
        "Atlanta Hawks" to "亞特蘭大老鷹",
        "Boston Celtics" to "波士頓塞爾提克",
        "Brooklyn Nets" to "布魯克林籃網",
        "Charlotte Hornets" to "夏洛特黃蜂",
        "Chicago Bulls" to "芝加哥公牛",
        "Cleveland Cavaliers" to "克里夫蘭騎士",
        "Dallas Mavericks" to "達拉斯獨行俠",
        "Denver Nuggets" to "丹佛金塊",
        "Detroit Pistons" to "底特律活塞",
        "Golden State Warriors" to "金州勇士",
        "Houston Rockets" to "休士頓火箭",
        "Indiana Pacers" to "印第安納溜馬",
        "Los Angeles Clippers" to "洛杉磯快艇",
        "Los Angeles Lakers" to "洛杉磯湖人",
        "Memphis Grizzlies" to "曼菲斯灰熊",
        "Miami Heat" to "邁阿密熱火",
        "Milwaukee Bucks" to "密爾瓦基公鹿",
        "Minnesota Timberwolves" to "明尼蘇達灰狼",
        "New Orleans Pelicans" to "紐奧良鵜鶘",
        "New York Knicks" to "紐約尼克",
        "Oklahoma City Thunder" to "奧克拉荷馬雷霆",
        "Orlando Magic" to "奧蘭多魔術",
        "Philadelphia 76ers" to "費城七六人",
        "Phoenix Suns" to "鳳凰城太陽",
        "Portland Trail Blazers" to "波特蘭拓荒者",
        "Sacramento Kings" to "沙加緬度國王",
        "San Antonio Spurs" to "聖安東尼奧馬刺",
        "Toronto Raptors" to "多倫多暴龍",
        "Utah Jazz" to "猶他爵士",
        "Washington Wizards" to "華盛頓巫師",

        // ===== MLB (30 隊) =====
        "New York Yankees" to "紐約洋基",
        "Boston Red Sox" to "波士頓紅襪",
        "Los Angeles Dodgers" to "洛杉磯道奇",
        "Chicago Cubs" to "芝加哥小熊",
        "Houston Astros" to "休士頓太空人",
        "Arizona Diamondbacks" to "亞利桑那響尾蛇",
        "Milwaukee Brewers" to "密爾瓦基釀酒人",
        "Colorado Rockies" to "科羅拉多落磯",
        "Kansas City Royals" to "堪薩斯皇家",
        "Minnesota Twins" to "明尼蘇達雙城",
        "Cincinnati Reds" to "辛辛那提紅人",
        "St. Louis Cardinals" to "聖路易紅雀",
        "Washington Nationals" to "華盛頓國民",
        "Athletics" to "奧克蘭運動家",
        "Cleveland Guardians" to "克里夫蘭守護者",
        "Texas Rangers" to "德州遊騎兵",
        "Los Angeles Angels" to "洛杉磯天使",
        "New York Mets" to "紐約大都會",
        "San Diego Padres" to "聖地牙哥教士",
        "Tampa Bay Rays" to "坦帕灣光芒",
        "Miami Marlins" to "邁阿密馬林魚",
        "Chicago White Sox" to "芝加哥白襪",
        "Philadelphia Phillies" to "費城費城人",
        "Seattle Mariners" to "西雅圖水手",
        "Detroit Tigers" to "底特律老虎",
        "Baltimore Orioles" to "巴爾的摩金鶯",
        "Toronto Blue Jays" to "多倫多藍鳥",
        "San Francisco Giants" to "舊金山巨人",
        "Pittsburgh Pirates" to "匹茲堡海盜",
        "Atlanta Braves" to "亞特蘭大勇士",

        // ===== NPB 日本職棒 =====
        // 中央聯盟
        "Yomiuri Giants" to "讀賣巨人",
        "Hanshin Tigers" to "阪神虎",
        "Chunichi Dragons" to "中日龍",
        "Yokohama DeNA BayStars" to "橫濱DeNA海灣之星",
        "Hiroshima Toyo Carp" to "廣島東洋鯉魚",
        "Tokyo Yakult Swallows" to "東京養樂多燕子",
        // 太平洋聯盟
        "Fukuoka SoftBank Hawks" to "福岡軟銀鷹",
        "Orix Buffaloes" to "歐力士猛牛",
        "ORIX Buffaloes" to "歐力士猛牛",
        "Chiba Lotte Marines" to "千葉羅德海洋",
        "Saitama Seibu Lions" to "埼玉西武獅",
        "Tohoku Rakuten Golden Eagles" to "東北樂天金鷲",
        "Hokkaido Nippon-Ham Fighters" to "北海道日本火腿鬥士",

        // ===== CPBL 中華職棒 =====
        "Uni-President 7-ELEVEn Lions" to "統一7-ELEVEn獅",
        "CTBC Brothers" to "中信兄弟",
        "Fubon Guardians" to "富邦悍將",
        "Rakuten Monkeys" to "樂天桃猿",
        "Wei Chuan Dragons" to "味全龍",
        "TSG Hawks" to "台鋼雄鷹",

        // ===== WNBA (15 隊) =====
        "Atlanta Dream" to "亞特蘭大夢想",
        "Chicago Sky" to "芝加哥天空",
        "Connecticut Sun" to "康乃狄克太陽",
        "Dallas Wings" to "達拉斯飛翼",
        "Golden State Valkyries" to "金洲女武神",
        "Indiana Fever" to "印第安納狂熱",
        "Las Vegas Aces" to "拉斯維加斯王牌",
        "Los Angeles Sparks" to "洛杉磯火花",
        "Minnesota Lynx" to "明尼蘇達山貓",
        "New York Liberty" to "紐約自由人",
        "Phoenix Mercury" to "鳳凰城水星",
        "Portland Fire" to "波特蘭火焰",
        "Seattle Storm" to "西雅圖風暴",
        "Toronto Tempo" to "多倫多節奏",
        "Washington Mystics" to "華盛頓神秘人"
    )

    fun getChineseName(englishName: String): String {
        return mapping[englishName] ?: englishName
    }
}