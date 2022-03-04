package it.matteoleggio.alchan.helper.pojo

import it.matteoleggio.alchan.data.response.User

class ActivityReply(
    val id: Int,
    val userId: Int?,
    val activityId: Int?,
    var text: String?,
    var likeCount: Int,
    var isLiked: Boolean?,
    val createdAt: Int,
    val user: User?,
    var likes: List<User>?
)