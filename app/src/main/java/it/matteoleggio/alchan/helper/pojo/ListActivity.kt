package it.matteoleggio.alchan.helper.pojo

import it.matteoleggio.alchan.data.response.Media
import it.matteoleggio.alchan.data.response.User
import type.ActivityType

class ListActivity(
    id: Int,
    type: ActivityType?,
    replyCount: Int,
    siteUrl: String?,
    isSubscribed: Boolean?,
    likeCount: Int,
    isLiked: Boolean?,
    createdAt: Int,
    replies: ArrayList<ActivityReply>?,
    likes: List<User>?,
    val userId: Int?,
    val status: String?,
    val progress: String?,
    val media: Media?,
    val user: User?
) : ActivityItem(
    id,
    type,
    replyCount,
    siteUrl,
    isSubscribed,
    likeCount,
    isLiked,
    createdAt,
    replies,
    likes
)