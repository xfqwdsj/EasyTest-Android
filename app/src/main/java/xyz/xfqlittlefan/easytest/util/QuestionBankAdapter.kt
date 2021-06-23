package xyz.xfqlittlefan.easytest.util

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import xyz.xfqlittlefan.easytest.QuestionBank
import xyz.xfqlittlefan.easytest.R
import xyz.xfqlittlefan.easytest.activity.SelectQuestionBankActivity

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
        val item = mList[position]
        val itemView = holder.itemView
        itemView.findViewById<TextView>(R.id.textView).text = item.name
        itemView.findViewById<TextView>(R.id.textView2).text = item.description
        if (item.children.isEmpty()) {
            itemView.findViewById<ImageView>(R.id.imageView).apply {
                setImageResource(R.drawable.ic_baseline_book_24)
            }
            itemView.setOnClickListener { mClickListener(item) }
        } else {
            itemView.findViewById<ImageView>(R.id.imageView).apply {
                setImageResource(R.drawable.ic_baseline_folder_24)
            }
            itemView.setOnClickListener {
                val urlList = PreferenceManager.getDefaultSharedPreferences(mContext).getString("custom_source", "")!!.split("\n").toMutableList()
                urlList.add("https://xfqwdsj.gitee.io/easy-test/question-bank-index.json")
                val index = ArrayList(mIndex)
                index.add(position)
                Intent(mContext, SelectQuestionBankActivity::class.java).apply {
                    putStringArrayListExtra("urlList", ArrayList(urlList))
                    putIntegerArrayListExtra("index", index)
                    startActivity(mContext, this, null)
                }
            }
        }
    }
}