package com.crosspaste.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

fun getDateUtils(): DateUtils {
    return DateUtils
}

object DateUtils {

    val TIME_ZONE: TimeZone = TimeZone.currentSystemDefault()

    @OptIn(FormatStringsInDatetimeFormats::class)
    val YMD_FORMAT = LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd") }

    @OptIn(FormatStringsInDatetimeFormats::class)
    val YMDHMS_FORMAT = LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm:ss") }

    fun getOffsetDay(
        currentTime: Instant = nowInstant(),
        days: Int,
    ): Long {
        val offsetDay = currentTime.plus(days.days)
        return offsetDay.toEpochMilliseconds()
    }

    fun getDateDesc(date: LocalDateTime): String? {
        val now = now()

        if (date.date == now.date) {
            val hoursDiff = now.hour - date.hour
            val minutesDiff = now.minute - date.minute
            val secondsDiff = now.second - date.second

            if (hoursDiff < 1 && minutesDiff < 1 && secondsDiff < 60) {
                return "just_now"
            }
            return "today"
        }

        val yesterday =
            nowInstant()
                .minus(1.days)
                .toLocalDateTime(TimeZone.currentSystemDefault())

        if (date.date == yesterday.date) {
            return "yesterday"
        }

        return null
    }

    fun getYMD(date: LocalDateTime = now()): String {
        return YMD_FORMAT.format(date)
    }

    fun getYMDHMS(date: LocalDateTime = now()): String {
        return YMDHMS_FORMAT.format(date)
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    fun getDateDesc(
        date: LocalDateTime,
        options: DateTimeFormatOptions,
        locale: String,
    ): String {
        val dateTimeFormat =
            LocalDateTime.Format {
                byUnicodePattern(
                    "${options.dateStyle.toPattern(locale)} ${options.timeStyle.toPattern(locale)}",
                )
            }
        return dateTimeFormat.format(date)
    }

    fun now(): LocalDateTime {
        val currentInstant: Instant = nowInstant()
        return currentInstant.toLocalDateTime(TIME_ZONE)
    }

    fun nowEpochMilliseconds(): Long {
        return nowInstant().toEpochMilliseconds()
    }

    fun nowInstant(): Instant {
        return Clock.System.now()
    }

    fun epochMillisecondsToLocalDateTime(epochMilliseconds: Long): LocalDateTime {
        val instant = Instant.fromEpochMilliseconds(epochMilliseconds)
        return instant.toLocalDateTime(TIME_ZONE)
    }
}
