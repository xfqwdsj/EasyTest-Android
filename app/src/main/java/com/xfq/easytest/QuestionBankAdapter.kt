package com.xfq.easytest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuestionBankAdapter(list: MutableList<QuestionBank>, clickListener: (QuestionBank, Boolean) -> Unit) : RecyclerView.Adapter<QuestionBankAdapter.ViewHolder>() {
    private val mList: MutableList<QuestionBank> = list
    private val mClickListener = clickListener

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val item = mList[position]
            itemView.findViewById<TextView>(R.id.textView).text = item.name
            itemView.findViewById<TextView>(R.id.textView2).text = item.description
            if (item.status == 0) {
                itemView.findViewById<ImageView>(R.id.imageView).animate().setDuration(0).rotation(0F).start()
            } else if (item.status == 1) {
                itemView.findViewById<ImageView>(R.id.imageView).animate().setDuration(0).rotation(90F).start()
            }
            if (item.children.isEmpty()) {
                itemView.findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.ic_baseline_school_24)
                itemView.setOnClickListener { mClickListener(item, true) }
            } else {
                itemView.findViewById<ImageView>(R.id.imageView).setImageResource(R.drawable.ic_baseline_arrow_right_24)
                itemView.setOnClickListener {
                    if (item.status == 0) {
                        item.status = 1
                        itemView.findViewById<ImageView>(R.id.imageView).animate().setDuration(300).rotation(90F).start()
                        for (i in mList[position].children.indices) {
                            mList.add(i + position + 1, mList[position].children[i])
                            notifyItemInserted(i + position + 1)
                        }
                    } else if (item.status == 1) {
                        item.status = 0
                        itemView.findViewById<ImageView>(R.id.imageView).animate().setDuration(300).rotation(0F).start()
                        for (i in 1..collapseCount(item)) {
                            mList.removeAt(position + 1)
                            notifyItemRemoved(position + 1)
                        }
                    }
                    mClickListener(item, false)
                }
            }
        }

        private fun collapseCount(bank: QuestionBank): Int {
            var count = 0
            for (item in bank.children) {
                count ++
                if (item.status == 1) {
                    count += collapseCount(item)
                }
                item.status = 0
            }
            return count
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.layout_question_bank_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }
}