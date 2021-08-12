package xyz.xfqlittlefan.easytest.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.SelectQuestionBankActivity
import xyz.xfqlittlefan.easytest.data.QuestionSet

class QuestionBankAdapter(
    private val list: MutableList<QuestionSet.Set>,
    private val context: Context,
    private val index: ArrayList<Int>,
    private val clickListener: (QuestionSet.Set) -> Unit
) : RecyclerView.Adapter<QuestionBankAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_question_bank, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val itemView = holder.itemView
        val textView = itemView.findViewById<TextView>(R.id.title)
        textView.visibility = View.GONE
        if (item.index == 0) {
            textView.text = item.questionSet.name
            textView.visibility = View.VISIBLE
        }
        itemView.findViewById<TextView>(R.id.textView).text = item.name
        itemView.findViewById<TextView>(R.id.textView2).text = item.description
        if (item.children.isEmpty()) {
            itemView.findViewById<ImageView>(R.id.imageView).apply {
                setImageResource(R.drawable.ic_baseline_book_24)
            }
            itemView.findViewById<View>(R.id.card).setOnClickListener { clickListener(item) }
        } else {
            itemView.findViewById<ImageView>(R.id.imageView).apply {
                setImageResource(R.drawable.ic_baseline_folder_24)
            }
            itemView.findViewById<View>(R.id.card).setOnClickListener {
                val urlList = listOf(item.outerUrl)
                val index = ArrayList(index)
                index.add(item.index)
                Intent(context, SelectQuestionBankActivity::class.java).apply {
                    putStringArrayListExtra("urlList", ArrayList(urlList))
                    putIntegerArrayListExtra("index", index)
                    startActivity(context, this, null)
                }
            }
        }
    }

    fun add(position: Int, list: List<QuestionSet.Set>) {
        this.list.addAll(position, list)
        notifyItemRangeInserted(position, list.size)
    }

    fun reset() {
        val count = list.size
        list.clear()
        notifyItemRangeRemoved(0, count)
    }
}