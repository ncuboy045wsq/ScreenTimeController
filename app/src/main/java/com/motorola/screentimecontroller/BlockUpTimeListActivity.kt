package com.motorola.screentimecontroller

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.activity_blockup_time_list.*
import motorola.core_services.misc.MotoExtendManager
import motorola.core_services.screentimecontroller.bean.ScreenBlockUpTime
import java.text.SimpleDateFormat
import java.util.*

class BlockUpTimeListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blockup_time_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val list = MotoExtendManager.getInstance(this).screenBlockUpTimesList
        val result = mutableListOf<ScreenBlockUpTime>()
        if (list == null) {
            recyclerView.adapter = AppListAdapter(this, result)
        } else {
            list.forEach { result.add(ScreenBlockUpTime(it))}
            recyclerView.adapter = AppListAdapter(this, result)
        }
    }
}

private class AppListAdapter(val context: Context, val list: List<ScreenBlockUpTime>) : BaseQuickAdapter<ScreenBlockUpTime, BaseViewHolder>(R.layout.item_blockup_time, list) {
    override fun getItemViewType(position: Int): Int {
        return position
    }

    private fun getDisplayTime(time: Long): String {
        val hour = time / (60 * 60 * 1000)
        val minute = (time - hour * (60 * 60 * 1000)) / (60 * 1000)
        return "$hour : $minute"
    }

    override fun convert(helper: BaseViewHolder, item: ScreenBlockUpTime) {
        helper.getView<TextView>(R.id.textView).text = "from ${getDisplayTime(item.startTime)} to ${getDisplayTime(item.endTime)}"
    }
}