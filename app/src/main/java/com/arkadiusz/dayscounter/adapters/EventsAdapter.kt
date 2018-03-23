package com.arkadiusz.dayscounter.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.database.Event
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import com.arkadiusz.dayscounter.utils.DateUtils.calculateDate
import com.arkadiusz.dayscounter.utils.DateUtils.generateCalendar
import com.arkadiusz.dayscounter.utils.DateUtils.generateTodayCalendar
import com.arkadiusz.dayscounter.utils.DateUtils.getElementsFromDate
import com.arkadiusz.dayscounter.utils.FontUtils.getFontFor
import com.bumptech.glide.Glide
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.single_event_layout.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor

/**
 * Created by arkadiusz on 17.03.18
 */

class EventsAdapter(context: Context, private val eventsList: OrderedRealmCollection<Event>) :
        RealmRecyclerViewAdapter<Event, EventsAdapter.ViewHolder>(context, eventsList, true) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_event_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(eventsList[position])
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private val databaseRepository = DatabaseRepository()

        fun bind(event: Event) {
            displayCounterText(event)
            displayTitle(event)
            displayImage(event)
            changeFonts(event)
            dimPicture(event)
            repeatIfNecessary(event)
        }

        private fun displayCounterText(event: Event) {
            val counterText = calculateDate(event.date,
                    event.formatYearsSelected,
                    event.formatMonthsSelected,
                    event.formatWeeksSelected,
                    event.formatDaysSelected, context)
            view.daysNumber.text = counterText
        }

        private fun displayTitle(event: Event) {
            view.eventName.text = event.name
        }

        private fun displayImage(event: Event) {
            when {
                event.imageID == 0 -> Glide.with(context).load(event.image).into(view.eventImage)
                event.imageID != 0 -> Glide.with(context).load(event.imageID).into(view.eventImage)
            }
        }

        private fun changeFonts(event: Event) {
            view.eventName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, event.titleFontSize.toFloat())
            view.daysNumber.setTextSize(TypedValue.COMPLEX_UNIT_DIP, event.counterFontSize.toFloat())
            view.eventName.typeface = getFontFor(event.fontType, context)
            view.daysNumber.typeface = getFontFor(event.fontType, context)
            view.eventName.textColor = event.fontColor
            view.daysNumber.textColor = event.fontColor
            view.line.backgroundColor = event.fontColor
        }

        private fun dimPicture(event: Event) {
            view.eventImage.setColorFilter(Color.argb(255 / 17 * event.pictureDim, 0, 0, 0))
        }

        private fun repeatIfNecessary(event: Event) {
            when (event.type) {
                "future" -> {
                    if (eventDateIsFromThePast(event)) {
                        if (eventIsNotRepeated(event)) {
                            databaseRepository.moveEventToPast(event)
                        } else {
                            databaseRepository.repeatEvent(event)
                        }
                    }
                }
                "past" -> {
                    if (eventDateIsFromTheFuture(event)) {
                        databaseRepository.moveEventToFuture(event)
                    }
                }
            }
        }

        private fun eventDateIsFromThePast(event: Event): Boolean {
            val todayCalendar = generateTodayCalendar()

            val eventTriple = getElementsFromDate(event.date)
            val year = eventTriple.first
            val month = eventTriple.second
            val day = eventTriple.third

            val eventCalendar = generateCalendar(year, month, day)

            return eventCalendar.before(todayCalendar)
        }

        private fun eventIsNotRepeated(event: Event): Boolean {
            return event.repeat == "0"
        }

        private fun eventDateIsFromTheFuture(event: Event): Boolean {
            val todayCalendar = generateTodayCalendar()

            val eventTriple = getElementsFromDate(event.date)
            val year = eventTriple.first
            val month = eventTriple.second
            val day = eventTriple.third

            val eventCalendar = generateCalendar(year, month, day)

            return todayCalendar.before(eventCalendar)
        }
    }
}