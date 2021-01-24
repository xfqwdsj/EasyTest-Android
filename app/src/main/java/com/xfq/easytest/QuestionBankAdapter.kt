package com.xfq.easytest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.question_bank_item.view.*
import kotlin.Unit

class QuestionBankAdapter(list: List<QuestionBank>, level: Int, clickListener: (QuestionBank) -> Unit) : RecyclerView.Adapter<QuestionBankAdapter.ViewHolder>() {
    private val mList: List<QuestionBank> = list
    private val mLevel: Int = level
    private val mClickListener = clickListener

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(bank: QuestionBank, level: Int, clickListener: (QuestionBank) -> Unit) {
            when (level) {
                0 -> itemView.textView.text = bank.name

            }
            itemView.setOnClickListener { clickListener(bank) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.question_bank_item, parent, false)
        val holder = ViewHolder(view)

        return holder
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position], mLevel, mClickListener)
    }
}