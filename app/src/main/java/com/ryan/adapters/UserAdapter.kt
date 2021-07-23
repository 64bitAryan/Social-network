package com.ryan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.firebase.auth.FirebaseAuth
import com.ryan.data.entities.Post
import com.ryan.data.entities.User
import com.ryan.socialnetwork.R
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.android.synthetic.main.item_user.view.*
import javax.inject.Inject

class UserAdapter @Inject constructor(
    private val glide: RequestManager
): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
       val ivProfilPicture: ImageView = itemView.ivProfileImage
        val tvUsername: TextView = itemView.tvUsername
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_user,
                parent,
                false
        ))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.apply {
            glide.load(user.profilePictureUrl).into(ivProfilPicture)

            tvUsername.text = user.userName
            itemView.setOnClickListener {
                onUserClickListener?.let { click ->
                    click(user)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    private val diffCallBack = object: DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }
    }

    private val differ = AsyncListDiffer(this, diffCallBack)

    var users: List<User>
        get() = differ.currentList
        set(value) = differ.submitList(value)


    private var onUserClickListener: ((User) -> Unit)? = null

    fun setOnUserClickListener(listener: (User) -> Unit) {
        onUserClickListener = listener
    }
}