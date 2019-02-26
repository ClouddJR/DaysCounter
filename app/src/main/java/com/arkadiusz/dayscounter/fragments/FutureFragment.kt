package com.arkadiusz.dayscounter.fragments

import PreferenceUtils.defaultPrefs
import PreferenceUtils.get
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.activities.DetailActivity
import com.arkadiusz.dayscounter.activities.EditActivity
import com.arkadiusz.dayscounter.adapters.EventsAdapter
import com.arkadiusz.dayscounter.adapters.RecyclerItemClickListener
import com.arkadiusz.dayscounter.model.Event
import com.arkadiusz.dayscounter.viewmodels.EventsViewModel
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity

/**
 * Created by arkadiusz on 23.03.18
 */

class FutureFragment : Fragment() {

    private lateinit var viewModel: EventsViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventsList: RealmResults<Event>
    private lateinit var eventContextOptions: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        setUpData()
        setUpContextOptions()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.future_fragment, container, false)
        initRecyclerView(view)
        setUpRecyclerView()
        hideFABOnScroll()
        return view
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(EventsViewModel::class.java)

        val sharedPref = defaultPrefs(context!!)
        val sortType = sharedPref["sort_type"] ?: "date_order"

        viewModel.init(sortType, context)
    }

    private fun setUpData() {
        eventsList = viewModel.getFutureEvents()
    }

    private fun setUpContextOptions() {
        eventContextOptions = listOf(getString(R.string.fragment_main_dialog_option_edit),
                getString(R.string.fragment_main_dialog_option_delete))
    }

    private fun initRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.addOnItemTouchListener(object : RecyclerItemClickListener(context!!, recyclerView, object : OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                val id = eventsList[position]!!.id
                context?.startActivity<DetailActivity>("event_id" to id)
            }

            override fun onItemLongClick(view: View?, position: Int) {
                vibration()
                displayEventOptions(eventsList[position]!!)
            }

        }) {})
    }

    private fun setUpRecyclerView() {
        val adapter = EventsAdapter(context!!, eventsList)
        recyclerView.adapter = adapter
        recyclerView.scheduleLayoutAnimation()
        recyclerView.invalidate()
    }

    private fun vibration() {
        val vibrator = context?.getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(25)
        }
    }

    private fun displayEventOptions(event: Event) {
        context?.let { ctx ->
            ctx.selector(getString(R.string.fragment_main_dialog_title), eventContextOptions) { _, i ->
                when (i) {
                    0 -> ctx.startActivity<EditActivity>("eventId" to event.id)
                    1 -> {
                        ctx.alert(getString(R.string.fragment_delete_dialog_question)) {
                            positiveButton(android.R.string.yes) {
                                viewModel.deleteEventAndRelatedReminder(context!!, event)
                            }
                            negativeButton(android.R.string.no) {}
                        }.show()
                    }
                }
            }
        }
    }

    private fun hideFABOnScroll() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    activity?.fab?.hide()
                } else if (dy < 0) {
                    activity?.fab?.show()
                }
            }
        })
    }
}