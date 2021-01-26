package com.xfq.easytest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import io.noties.markwon.Markwon

class TestPagerAdapter(list: MutableList<Question>, private val context: Context) : RecyclerView.Adapter<TestPagerAdapter.ViewHolder>() {
    private val mList: MutableList<Question> = list

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.layout_test, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = mList[position]
        val markwon = Markwon.create(context)
        if (question is Question.FillBankQuestion) {
            holder.itemView.findViewById<ScrollView>(R.id.fillBankQuestionLayout).visibility = View.VISIBLE
            if (question.question.replace("&%{", "").replace("&}", "").count { it == '{' } == question.question.replace("&%{", "").replace("&}", "").count { it == '}' } && question.question.replace("&%{", "").replace("&}", "").count { it == '{' } != 0) {
                val textArray = question.question.split("}")
                var id = 0
                for (text in textArray) {
                    if (text.last() == '&') {
                        val textView = MaterialTextView(context)
                        markwon.setMarkdown(textView, text.substring(0, text.length - 1).replace("&%{", "%{") + "}")
                        holder.itemView.findViewById<FlexboxLayout>(R.id.fillBankQuestionLayout).addView(textView)
                    } else {
                        val textView = MaterialTextView(context)
                        markwon.setMarkdown(textView, text.substring(0, text.indexOf("%{")))
                        val editText = TextInputEditText(context)
                        editText.hint = text.substring(text.indexOf("%{"))
                        editText.id = id
                        editText.minEms = 5
                        holder.itemView.findViewById<FlexboxLayout>(R.id.fillBankQuestionLayout).addView(textView)
                        holder.itemView.findViewById<FlexboxLayout>(R.id.fillBankQuestionLayout).addView(editText)
                        id++
                    }
                }
            }
        } else if (question is Question.SingleChooseQuestion) {
            holder.itemView.findViewById<ScrollView>(R.id.singleChooseQuestionLayout).visibility = View.VISIBLE
            markwon.setMarkdown(holder.itemView.findViewById(R.id.singleQuestion), question.question)
            for (i in question.options.indices) {
                val button = RadioButton(context)
                markwon.setMarkdown(button, question.options[i].option)
                button.id = i
                holder.itemView.findViewById<RadioGroup>(R.id.singleGroup).addView(button)
            }
        } else if (question is Question.MultipleChooseQuestion) {
            holder.itemView.findViewById<ScrollView>(R.id.multipleChooseQuestionLayout).visibility = View.VISIBLE
            markwon.setMarkdown(holder.itemView.findViewById(R.id.multipleQuestion), question.question)
            for (j in question.options.indices) {
                val button = CheckBox(context)
                markwon.setMarkdown(button, question.options[j].option)
                button.id = j
                holder.itemView.findViewById<RadioGroup>(R.id.multipleGroup).addView(button)
            }
        } else if (question is Question.GeneralQuestion) {
            holder.itemView.findViewById<ScrollView>(R.id.generalQuestionLayout).visibility = View.VISIBLE
            markwon.setMarkdown(holder.itemView.findViewById(R.id.generalQuestion), question.question)
        }
    }

    override fun getItemCount(): Int = mList.size
}