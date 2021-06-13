package com.xfq.easytest.util

import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.xfq.easytest.QuestionBank
import com.xfq.easytest.R
import com.xfq.easytest.activity.SelectQuestionBankActivity
import rikka.core.res.resolveColor

class QuestionBankAdapter(
    private val mList: MutableList<QuestionBank>,
    private val mContext: Context,
    private val mIndex: ArrayList<Int>,
    private val mClickListener: (QuestionBank) -> Unit
) : RecyclerView.Adapter<QuestionBankAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.layout_question_bank_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        fun collapseCount(bank: QuestionBank): Int {
            var count = 0
            for (item in bank.children) {
                count++
                if (item.status == 2) {
                    count += collapseCount(item)
                }
                item.status = 0
            }
            return count
        }

        val item = mList[position]
        val itemView = holder.itemView
        itemView.findViewById<TextView>(R.id.textView).text = item.name
        itemView.findViewById<TextView>(R.id.textView2).text = item.description
        /*
        if (item.status == 0) item.status = 1
        when (item.status) {
            1 -> itemView.findViewById<ImageView>(R.id.imageView).animate().setDuration(0)
                .rotation(0F).start()
            2 -> itemView.findViewById<ImageView>(R.id.imageView).animate().setDuration(0)
                .rotation(90F).start()
        }
         */
        val typedValue = TypedValue()
        mContext.theme.resolveAttribute(R.attr.colorControlNormal, typedValue, true)
        val colorControl = mContext.theme.resolveColor(R.attr.colorOnBackground)
        if (item.children.isEmpty()) {
            itemView.findViewById<ImageView>(R.id.imageView).apply {
                setImageResource(R.drawable.ic_baseline_book_24)
                setColorFilter(colorControl)
            }
            itemView.setOnClickListener { mClickListener(item) }
        } else {
            itemView.findViewById<ImageView>(R.id.imageView).apply {
                setImageResource(R.drawable.ic_baseline_folder_24)
                setColorFilter(colorControl)
            }
            itemView.setOnClickListener {
                val urlList = PreferenceManager.getDefaultSharedPreferences(mContext)
                    .getString("custom_source", "")!!.split("\n").toMutableList()
                urlList.add("https://xfqwdsj.gitee.io/easy-test/question-bank-index.json")
                val index = ArrayList(mIndex)
                index.add(position)
                Intent(mContext, SelectQuestionBankActivity::class.java).apply {
                    putStringArrayListExtra("urlList", ArrayList(urlList))
                    putIntegerArrayListExtra("index", index)
                    startActivity(mContext, this, null)
                }
                /*
                val viewPosition =
                    (itemView.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
                if (item.status == 1) {
                    item.status = 2
                    itemView.findViewById<ImageView>(R.id.imageView).animate().setDuration(300)
                        .rotation(90F).start()
                    for (i in mList[viewPosition].children.indices) {
                        mList.add(i + viewPosition + 1, mList[viewPosition].children[i])
                        notifyItemInserted(i + viewPosition + 1)
                    }
                } else if (item.status == 2) {
                    item.status = 1
                    itemView.findViewById<ImageView>(R.id.imageView).animate().setDuration(300)
                        .rotation(0F).start()
                    for (i in 1..collapseCount(item)) {
                        mList.removeAt(viewPosition + 1)
                        notifyItemRemoved(viewPosition + 1)
                    }
                }
                 */
            }
        }
    }
}