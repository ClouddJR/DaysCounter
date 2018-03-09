package com.arkadiusz.dayscounter.utils

import android.content.Context
import com.arkadiusz.dayscounter.R
import java.util.*

/**
 * Created by arkadiusz on 02.03.18
 */

object DateUtils {

    fun formatDate(year: Int, month: Int, day: Int): String {
        val formattedMonth = if (month < 10) {
            "0${month + 1}"
        } else {
            "${month + 1}"
        }

        val formattedDay = if (day < 10) {
            "0$day"
        } else {
            "$day"
        }

        return "$year-$formattedMonth-$formattedDay"
    }

    fun formatTime(hour: Int, minute: Int): String {
        val formattedHour = if (hour < 10) {
            "0$hour"
        } else {
            "$hour"
        }

        val formattedMinutes = if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }

        return "$formattedHour:$formattedMinutes"
    }


    fun calculateDate(dateYear: Int, dateMonth: Int, dateDay: Int, areYearsIncluded: Boolean,
                      areMonthsIncluded: Boolean, areWeeksIncluded: Boolean, areDaysIncluded: Boolean, context: Context): String {

        var eventCalendar = generateCalendar(dateYear, dateMonth, dateDay)
        var todayCalendar = generateTodayCalendar()

        var yearsNumber = 0
        var monthsNumber = 0
        var weeksNumber = 0
        var daysNumber = 0

        if (!eventCalendar.before(todayCalendar)) {
            val tempCalendar = eventCalendar
            eventCalendar = todayCalendar
            todayCalendar = tempCalendar
        }

        //calculate years
        if (areYearsIncluded) {
            while (eventCalendar.before(todayCalendar)) {
                eventCalendar.add(Calendar.YEAR, 1)
                yearsNumber++
            }
            if (!eventCalendar.isTheSameInstanceAs(todayCalendar)) {
                eventCalendar.add(Calendar.YEAR, -1)
                yearsNumber--
            }
        }

        //calculate months
        if (areMonthsIncluded) {
            while (eventCalendar.before(todayCalendar)) {
                eventCalendar.add(Calendar.MONTH, 1)
                monthsNumber++
            }
            if (!eventCalendar.isTheSameInstanceAs(todayCalendar)) {
                eventCalendar.add(Calendar.MONTH, -1)
                monthsNumber--
            }
        }

        //calculate weeks
        if (areWeeksIncluded) {
            while (eventCalendar.before(todayCalendar)) {
                eventCalendar.add(Calendar.WEEK_OF_YEAR, 1)
                weeksNumber++
            }
            if (!eventCalendar.isTheSameInstanceAs(todayCalendar)) {
                eventCalendar.add(Calendar.WEEK_OF_YEAR, -1)
                weeksNumber--
            }
        }

        //calculate days
        if (areDaysIncluded) {
            while (eventCalendar.before(todayCalendar)) {
                eventCalendar.add(Calendar.DAY_OF_MONTH, 1)
                daysNumber++
            }
            if (!eventCalendar.isTheSameInstanceAs(todayCalendar)) {
                eventCalendar.add(Calendar.DAY_OF_MONTH, -1)
                daysNumber--
            }
        }

        return generateCounterText(yearsNumber, monthsNumber, weeksNumber, daysNumber,
                areYearsIncluded, areMonthsIncluded, areWeeksIncluded, areDaysIncluded,
                context)
    }

    private fun generateCounterText(yearsNumber: Int, monthsNumber: Int, weeksNumber: Int, daysNumber: Int,
                                    areYearsIncluded: Boolean, areMonthsIncluded: Boolean, areWeeksIncluded: Boolean, areDaysIncluded: Boolean,
                                    context: Context): String {
        var counterText = ""
        if (yearsNumber > 1) {
            counterText += "$yearsNumber " + context.getString(R.string.date_utils_multiple_years) + " "
        } else if (yearsNumber == 1) {
            counterText += "$yearsNumber " + context.getString(R.string.date_utils_single_year) + " "
        } else if (yearsNumber == 0 && areYearsIncluded) {
            counterText += "0 ${context.getString(R.string.date_utils_multiple_years)} "
        }

        if (monthsNumber > 1) {
            counterText += "$monthsNumber " + context.getString(R.string.date_utils_multiple_months) + " "
        } else if (monthsNumber == 1) {
            counterText += "$monthsNumber " + context.getString(R.string.date_utils_single_month) + " "
        } else if (monthsNumber == 0 && areMonthsIncluded) {
            counterText += "0 ${context.getString(R.string.date_utils_multiple_months)} "
        }

        if (weeksNumber > 1) {
            counterText += "$weeksNumber " + context.getString(R.string.date_utils_multiple_weeks) + " "
        } else if (weeksNumber == 1) {
            counterText += "$weeksNumber " + context.getString(R.string.date_utils_single_week) + " "
        } else if (weeksNumber == 0 && areWeeksIncluded) {
            counterText += "0 ${context.getString(R.string.date_utils_multiple_weeks)} "
        }

        if (daysNumber > 1) {
            counterText += "$daysNumber " + context.getString(R.string.date_utils_multiple_days)
        } else if (daysNumber == 1) {
            counterText += "$daysNumber " + context.getString(R.string.date_utils_single_day)
        } else if (daysNumber == 0 && areDaysIncluded) {
            counterText += "0 ${context.getString(R.string.date_utils_multiple_days)} "
        }

        if (yearsNumber == 0 && monthsNumber == 0 && weeksNumber == 0 && daysNumber == 0) {
            counterText = context.getString(R.string.date_utils_today)
        }
        return counterText.trim()
    }

    private fun generateCalendar(dateYear: Int, dateMonth: Int, dateDay: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, dateYear)
        calendar.set(Calendar.MONTH, dateMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, dateDay)
        return calendar
    }

    fun generateTodayCalendar(): Calendar {
        return Calendar.getInstance()
    }

    private fun Calendar.isTheSameInstanceAs(calendar: Calendar): Boolean {
        return (this.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                this.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                this.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
    }


}