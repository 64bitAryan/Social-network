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
import com.ryan.socialnetwork.R
import kotlinx.android.synthetic.main.item_post.view.*
import javax.inject.Inject

class PostAdapter @Inject constructor(
    private val glide: RequestManager
): RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val ivPostImage: ImageView = itemView.ivPostImage
        val ivAuthorProfileImage: ImageView = itemView.ivAuthorProfileImage
        val tvPostAuthor: TextView = itemView.tvPostAuthor
        val tvPostText: TextView = itemView.tvPostText
        val tvLikedBy: TextView = itemView.tvLikedBy
        val ibLiked: ImageButton = itemView.ibLike
        val ibComments: ImageButton = itemView.ibComments
        val ibDeletePost: ImageButton = itemView.ibDeletePost
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_post,
                parent,
                false
        ))
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.apply {
            glide.load(post.imageUrl).into(ivPostImage)
            glide.load(post.authorProfilePicture).into(ivAuthorProfileImage)
            tvPostAuthor.text = post.authorUsername
            tvPostText.text = post.text
            val likedCount = post.likedBy.size
            tvLikedBy.text = when {
                likedCount <= 0 -> "No Liked"
                likedCount == 1 ->  "Liked by 1 Person"
                else -> "Liked by $likedCount peoples"
            }
            val uid = FirebaseAuth.getInstance().uid!!
            ibDeletePost.isVisible = uid == post.authorUid
            ibLiked.setImageResource(if(post.isLiked){
                R.drawable.ic_liked
            } else R.drawable.ic_like_white
            )
            tvPostAuthor.setOnClickListener {
                onUserClickListener?.let{ click ->
                    click(post.authorUid)
                }
            }

            ivAuthorProfileImage.setOnClickListener {
                onUserClickListener?.let{ click ->
                    click(post.authorUid)
                }
            }

            tvLikedBy.setOnClickListener {
                onLikedByClickListener?.let { click ->
                    click(post)
                }
            }

            ibLiked.setOnClickListener {
                onLikeClickListener?.let { click ->
                    if(!post.isLiking) click(post, holder.layoutPosition)
                }
            }

            ibComments.setOnClickListener {
                onCommentClickedListener?.let { click ->
                    click(post)
                }
            }

            ibDeletePost.setOnClickListener {
                onDeletePostClickListener?.let{ click ->
                    click(post)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    private val diffCallBack = object: DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }
    }

    private val differ = AsyncListDiffer(this, diffCallBack)

    var posts: List<Post>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    private var onLikeClickListener: ((Post, Int) -> Unit)? = null
    private var onUserClickListener: ((String) -> Unit)? = null
    private var onDeletePostClickListener: ((Post) -> Unit)? = null
    private var onLikedByClickListener: ((Post) -> Unit)? = null
    private var onCommentClickedListener: ((Post) -> Unit)? = null

    fun setOnLikeClickListener(listener: (Post, Int) -> Unit) {
        onLikeClickListener = listener
    }

    fun setOnUserClickListener(listener: (String) -> Unit) {
        onUserClickListener = listener
    }

    fun setOnDeletePostClickListener(listener: (Post) -> Unit) {
        onDeletePostClickListener = listener
    }

    fun setOnLikedByClickListener(listener: (Post) -> Unit) {
        onLikedByClickListener = listener
    }

    fun setOnCommentsClickedListener(listener: (Post) -> Unit) {
        onCommentClickedListener = listener
    }
}