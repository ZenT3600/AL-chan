package it.matteoleggio.alchan.helper.pojo

import it.matteoleggio.alchan.data.response.User
import type.ActivityType

class TextActivity(
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
    var text: String?,
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
