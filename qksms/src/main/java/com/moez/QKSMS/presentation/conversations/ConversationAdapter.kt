package com.moez.QKSMS.presentation.conversations

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.ThemeManager
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.presentation.base.QkViewHolder
import com.moez.QKSMS.presentation.messages.MessageListActivity
import io.realm.OrderedRealmCollection
import io.realm.RealmList
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import javax.inject.Inject

class ConversationAdapter(data: OrderedRealmCollection<Message>?) : RealmRecyclerViewAdapter<Message, QkViewHolder>(data, true) {

    @Inject lateinit var context: Context
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var dateFormatter: DateFormatter
    @Inject lateinit var themeManager: ThemeManager

    init {
        AppComponentManager.appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): QkViewHolder {
        val layoutRes = when (viewType) {
            0 -> R.layout.conversation_list_item
            else -> R.layout.conversation_list_item_unread
        }

        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(layoutRes, parent, false)

        if (viewType == 1) {
            view.date.setTextColor(themeManager.color)
        }

        return QkViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val message = getItem(position)!!
        val conversation = messageRepo.getConversation(message.threadId)
        val view = viewHolder.itemView

        RxView.clicks(view)
                .map { Intent(context, MessageListActivity::class.java) }
                .doOnNext { intent -> intent.putExtra("threadId", message.threadId) }
                .subscribe { intent -> context.startActivity(intent) }

        view.avatars.contacts = conversation?.contacts ?: RealmList()
        view.title.text = conversation?.getTitle()
        view.date.text = dateFormatter.getConversationTimestamp(message.date)
        view.snippet.text = message.body
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)!!.read) 0 else 1
    }
}