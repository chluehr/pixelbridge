// SPDX-License-Identifier: Apache-2.0

package net.farcaster.pixelbridge.notify

import androidx.annotation.DrawableRes
import net.farcaster.pixelbridge.R
import java.util.Locale

/** Bundled logos: Gadgetbridge sends an app label in `src`, not the phone's icon. */
object AppIcons {
    private val separators = Regex("[\\s\\p{Z}_-]+")

    private fun normalize(source: String): String =
        source.trim().lowercase(Locale.ROOT).replace(separators, "")

    // Match complete labels/packages only: an unknown app containing "telegram" must not
    // impersonate Telegram. Package aliases also support sources that forward package names.
    private data class AppIcon(@param:DrawableRes val small: Int, @param:DrawableRes val color: Int)

    private val icons: Map<String, AppIcon> = buildMap {
        fun app(@DrawableRes small: Int, @DrawableRes color: Int, vararg aliases: String) {
            aliases.forEach { put(normalize(it), AppIcon(small, color)) }
        }
        app(R.drawable.ic_app_youtube, R.drawable.ic_color_youtube, "YouTube", "com.google.android.youtube", "YouTube ReVanced", "app.revanced.android.youtube")
        app(R.drawable.ic_app_telegram, R.drawable.ic_color_telegram, "Telegram", "Telegram X", "Telegram FOSS", "Телеграм", "org.telegram.messenger", "org.telegram.messenger.web", "org.thunderdog.challegram")
        app(R.drawable.ic_app_openai, R.drawable.ic_color_openai, "ChatGPT", "Chat GPT", "OpenAI", "com.openai.chatgpt")
        app(R.drawable.ic_app_reddit, R.drawable.ic_color_reddit, "Reddit", "com.reddit.frontpage")
        app(R.drawable.ic_app_spotify, R.drawable.ic_color_spotify, "Spotify", "com.spotify.music")
        app(R.drawable.ic_app_whatsapp, R.drawable.ic_color_whatsapp, "WhatsApp", "WhatsApp Business", "com.whatsapp", "com.whatsapp.w4b")
        app(R.drawable.ic_app_signal, R.drawable.ic_color_signal, "Signal", "Signal Private Messenger", "Molly", "org.thoughtcrime.securesms", "im.molly.app", "im.molly.app.unifiedpush")
        app(R.drawable.ic_app_discord, R.drawable.ic_color_discord, "Discord", "com.discord")
        app(R.drawable.ic_app_instagram, R.drawable.ic_color_instagram, "Instagram", "Instagram Lite", "com.instagram.android", "com.instagram.lite")
        app(R.drawable.ic_app_facebook, R.drawable.ic_color_facebook, "Facebook", "Facebook Lite", "com.facebook.katana", "com.facebook.lite")
        app(R.drawable.ic_app_messenger, R.drawable.ic_color_messenger, "Messenger", "Facebook Messenger", "com.facebook.orca", "com.facebook.mlite")
        app(R.drawable.ic_app_tiktok, R.drawable.ic_color_tiktok, "TikTok", "TikTok Lite", "com.zhiliaoapp.musically", "com.zhiliaoapp.musically.go", "com.ss.android.ugc.trill")
        app(R.drawable.ic_app_x, R.drawable.ic_color_x, "X", "Twitter", "com.twitter.android")
        app(R.drawable.ic_app_viber, R.drawable.ic_color_viber, "Viber", "Rakuten Viber", "com.viber.voip")
        app(R.drawable.ic_app_vk, R.drawable.ic_color_vk, "VK", "ВКонтакте", "В Контакте", "com.vkontakte.android")
        app(R.drawable.ic_app_gmail, R.drawable.ic_color_gmail, "Gmail", "com.google.android.gm")
        app(R.drawable.ic_app_googlemessages, R.drawable.ic_color_googlemessages, "Google Messages", "Messages", "Сообщения", "com.google.android.apps.messaging")
        app(R.drawable.ic_app_googlecalendar, R.drawable.ic_color_googlecalendar, "Google Calendar", "Calendar", "Google Календарь", "Календарь", "com.google.android.calendar")
        app(R.drawable.ic_app_googlemaps, R.drawable.ic_color_googlemaps, "Google Maps", "Maps", "Google Карты", "Карты", "com.google.android.apps.maps")
        app(R.drawable.ic_app_googlechrome, R.drawable.ic_color_googlechrome, "Google Chrome", "Chrome", "com.android.chrome")
        app(R.drawable.ic_app_googledrive, R.drawable.ic_color_googledrive, "Google Drive", "Drive", "Google Диск", "Диск", "com.google.android.apps.docs")
        app(R.drawable.ic_app_slack, R.drawable.ic_color_slack, "Slack", "com.Slack")
        app(R.drawable.ic_app_zoom, R.drawable.ic_color_zoom, "Zoom", "Zoom Workplace", "us.zoom.videomeetings")
        app(R.drawable.ic_app_protonmail, R.drawable.ic_color_protonmail, "Proton Mail", "ProtonMail", "ch.protonmail.android")
        app(R.drawable.ic_app_thunderbird, R.drawable.ic_color_thunderbird, "Thunderbird", "net.thunderbird.android")
        app(R.drawable.ic_app_twitch, R.drawable.ic_color_twitch, "Twitch", "tv.twitch.android.app")
        app(R.drawable.ic_app_netflix, R.drawable.ic_color_netflix, "Netflix", "com.netflix.mediaclient")
        app(R.drawable.ic_app_github, R.drawable.ic_color_github, "GitHub", "com.github.android")
        app(R.drawable.ic_app_element, R.drawable.ic_color_element, "Element", "Element X", "im.vector.app", "io.element.android.x")
        app(R.drawable.ic_app_mastodon, R.drawable.ic_color_mastodon, "Mastodon", "org.joinmastodon.android")
        app(R.drawable.ic_music_music_note, R.drawable.ic_color_aimp, "AIMP", "com.aimp.player")
        app(R.drawable.ic_music_music_note, R.drawable.ic_color_poweramp, "Poweramp", "com.maxmpz.audioplayer")
        app(R.drawable.ic_app_vlc, R.drawable.ic_color_vlc, "VLC", "VLC media player", "org.videolan.vlc")
        app(R.drawable.ic_music_music_note, R.drawable.ic_color_musicolet, "Musicolet", "in.krosbits.musicolet")
        app(R.drawable.ic_app_youtubemusic, R.drawable.ic_color_youtubemusic, "YouTube Music", "YouTube Музыка", "com.google.android.apps.youtube.music")
        app(R.drawable.ic_music_music_note, R.drawable.ic_color_yandexmusic, "Yandex Music", "Яндекс Музыка", "Яндекс.Музыка", "ru.yandex.music")
        app(R.drawable.ic_app_applemusic, R.drawable.ic_color_applemusic, "Apple Music", "com.apple.android.music")
        app(R.drawable.ic_app_amazonmusic, R.drawable.ic_color_amazonmusic, "Amazon Music", "com.amazon.mp3")
        app(R.drawable.ic_music_music_note, R.drawable.ic_color_deezer, "Deezer", "deezer.android.app")
        app(R.drawable.ic_app_tidal, R.drawable.ic_color_tidal, "TIDAL", "com.aspiro.tidal")
        app(R.drawable.ic_app_soundcloud, R.drawable.ic_color_soundcloud, "SoundCloud", "com.soundcloud.android")
        app(R.drawable.ic_app_pocketcasts, R.drawable.ic_color_pocketcasts, "Pocket Casts", "au.com.shiftyjelly.pocketcasts")
        app(R.drawable.ic_app_antennapod, R.drawable.ic_color_antennapod, "AntennaPod", "de.danoeh.antennapod")
        app(R.drawable.ic_app_foobar2000, R.drawable.ic_color_foobar2000, "foobar2000", "com.foobar2000.foobar2000")
        app(R.drawable.ic_music_music_note, R.drawable.ic_color_samsungmusic, "Samsung Music", "com.sec.android.app.music")
        app(R.drawable.ic_launcher_foreground, R.drawable.ic_color_kdeconnect, "KDE Connect", "KDEConnect", "org.kde.kdeconnect_tp")
        app(R.drawable.ic_launcher_foreground, R.drawable.ic_color_gosuslugi, "Госуслуги", "Gosuslugi", "ru.rostel")
        app(R.drawable.ic_app_steam, R.drawable.ic_color_steam, "Steam", "com.valvesoftware.android.steam.community")
        app(R.drawable.ic_app_steam, R.drawable.ic_color_steamchat, "Steam Chat", "com.valvesoftware.android.steam.friendsui")
        app(R.drawable.ic_launcher_foreground, R.drawable.ic_color_ozon, "Ozon", "Озон", "OZON: товары, одежда, билеты", "ru.ozon.app.android")
        app(R.drawable.ic_launcher_foreground, R.drawable.ic_color_wildberries, "Wildberries", "Вайлдберриз", "WB", "com.wildberries.ru")
        app(R.drawable.ic_launcher_foreground, R.drawable.ic_color_avito, "Авито", "Avito", "Авито – путешествия, работа, услуги, авто", "com.avito.android")
        app(R.drawable.ic_launcher_foreground, R.drawable.ic_color_yandexgo, "Яндекс Go", "Yandex Go", "Яндекс Такси", "Yandex Taxi", "Яндекс Go: Такси, Еда, Маркет, Самокаты, Доставка", "ru.yandex.taxi")
        app(R.drawable.ic_launcher_foreground, R.drawable.ic_color_yandexmail, "Яндекс Почта", "Яндекс.Почта", "Yandex Mail", "Яндекс Почта: умная электронная почта, облако", "ru.yandex.mail")
        app(R.drawable.ic_app_mailru, R.drawable.ic_color_mailru, "Почта Mail", "Почта Mail.ru", "Mail.ru", "Mail: Почта, Облако, Календарь", "ru.mail.mailapp")
        app(R.drawable.ic_launcher_foreground, R.drawable.ic_color_2gis, "2ГИС", "2GIS", "ДубльГИС", "2ГИС: навигатор, транспорт, друзья на карте", "ru.dublgis.dgismobile")
        app(R.drawable.ic_app_kinopoisk, R.drawable.ic_color_kinopoisk, "Кинопоиск", "КиноПоиск HD", "Kinopoisk", "Кинопоиск: кино и сериалы", "ru.kinopoisk")
        app(R.drawable.ic_launcher_foreground, R.drawable.ic_color_rutube, "RUTUBE", "Рутуб", "RUTUBE: видео, шоу, трансляции", "ru.rutube.app")
    }

    @DrawableRes
    fun forSource(source: String?): Int =
        source?.let { icons[normalize(it)]?.small } ?: R.drawable.ic_launcher_foreground

    @DrawableRes
    fun colorForSource(source: String?): Int =
        source?.let { icons[normalize(it)]?.color } ?: R.drawable.ic_launcher_foreground
}
