package xyz.xfqlittlefan.easytest.data

import androidx.annotation.Keep
import java.util.*

@Keep
class QuestionSet {
    lateinit var name: String
    lateinit var id: String
    lateinit var url: String
    lateinit var set: List<Set>

    fun init() {
        if (this::set.isInitialized) {
            set.forEach {
                it.savedQuestionSet = this
                it.init()
            }
        }
    }

    @Keep
    class Set {
        lateinit var savedQuestionSet: QuestionSet
        private lateinit var savedSet: Set
        lateinit var name: String
        lateinit var description: String
        lateinit var url: String
        lateinit var id: String
        var random = false
        lateinit var children: List<Set>
        val isChildrenInitialized get() = this::children.isInitialized

        val questionSet: QuestionSet?
            get() = when {
                this::savedQuestionSet.isInitialized -> savedQuestionSet
                this::savedSet.isInitialized -> savedSet.questionSet
                else -> null
            }
        private val list: List<Set>
            get() = when {
                this::savedSet.isInitialized -> savedSet.children
                this::savedQuestionSet.isInitialized -> savedQuestionSet.set
                else -> listOf()
            }
        val questionSetUrl: String
            get() = questionSet?.url ?: ""
        val index: Int
            get() {
                var index = 114514
                val set = list
                for (i in set.indices) {
                    if (set[i] == this) {
                        index = i
                        break
                    }
                }
                return index
            }

        fun init() {
            if (this::children.isInitialized) {
                children.forEach {
                    if (this::savedQuestionSet.isInitialized) it.savedQuestionSet = savedQuestionSet
                    it.savedSet = this
                    it.init()
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val set = other as Set
            return name == set.name && description == set.description && url == set.url && id == set.id && random == set.random
        }

        override fun hashCode(): Int {
            return Objects.hash(name, description, url, id, random)
        }
    }
}