package it.matteoleggio.alchan.data.datasource

import ActivityDetailQuery
import ActivityQuery
import DeleteActivityMutation
import DeleteActivityReplyMutation
import SaveActivityReplyMutation
import SaveMessageActivityMutation
import SaveTextActivityMutation
import ToggleActivitySubsriptionMutation
import ToggleLikeMutation
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.Rx2Apollo
import it.matteoleggio.alchan.data.network.ApolloHandler
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import type.ActivityType
import type.LikeableType

class SocialDataSourceImpl(private val apolloHandler: ApolloHandler) : SocialDataSource {

    override fun getFriendsActivity(
        typeIn: List<ActivityType>?,
        userId: Int?,
        userIdNotIn: List<Int>?
    ): Observable<Response<ActivityQuery.Data>> {
        val query = ActivityQuery(
            page = Input.fromNullable(1),
            perPage = Input.fromNullable(10),
            type_in = Input.optional(typeIn),
            userId = Input.optional(userId),
            userId_not_in = Input.optional(userIdNotIn),
            isFollowing = Input.optional(if (userId != null) null else true)
        )
        val queryCall = apolloHandler.apolloClient.query(query)
        return Rx2Apollo.from(queryCall)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getActivityDetail(id: Int): Observable<Response<ActivityDetailQuery.Data>> {
        val query = ActivityDetailQuery(id = Input.fromNullable(id))
        val queryCall = apolloHandler.apolloClient.query(query)
        return Rx2Apollo.from(queryCall)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun toggleLike(
        id: Int,
        type: LikeableType
    ): Observable<Response<ToggleLikeMutation.Data>> {
        val mutation = ToggleLikeMutation(id = Input.fromNullable(id), type = Input.fromNullable(type))
        val mutationCall = apolloHandler.apolloClient.mutate(mutation)
        return Rx2Apollo.from(mutationCall)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun toggleActivitySubscription(activityId: Int, subscribe: Boolean): Completable {
        val mutation = ToggleActivitySubsriptionMutation(
            activityId = Input.fromNullable(activityId),
            subscribe = Input.fromNullable(subscribe)
        )
        val mutationCall = apolloHandler.apolloClient.prefetch(mutation)
        return Rx2Apollo.from(mutationCall)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun deleteActivity(id: Int): Completable {
        val mutation = DeleteActivityMutation(id = Input.fromNullable(id))
        val mutationCall = apolloHandler.apolloClient.prefetch(mutation)
        return Rx2Apollo.from(mutationCall)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun deleteActivityReply(id: Int): Completable {
        val mutation = DeleteActivityReplyMutation(id = Input.fromNullable(id))
        val mutationCall = apolloHandler.apolloClient.prefetch(mutation)
        return Rx2Apollo.from(mutationCall)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getActivities(
        page: Int,
        typeIn: List<ActivityType>?,
        userId: Int?,
        following: Boolean?
    ): Observable<Response<ActivityQuery.Data>> {
        val query = ActivityQuery(
            page = Input.optional(page),
            type_in = Input.optional(typeIn),
            userId = Input.optional(userId),
            isFollowing = Input.optional(following)
        )
        val queryCall = apolloHandler.apolloClient.query(query)
        return Rx2Apollo.from(queryCall)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun saveTextActivity(
        id: Int?,
        text: String
    ): Observable<Response<SaveTextActivityMutation.Data>> {
        val mutation = SaveTextActivityMutation(id = Input.optional(id), text = Input.fromNullable(text))
        val mutationCall = apolloHandler.apolloClient.mutate(mutation)
        return Rx2Apollo.from(mutationCall)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun saveMessageActivity(
        id: Int?,
        message: String,
        recipientId: Int,
        private: Boolean
    ): Observable<Response<SaveMessageActivityMutation.Data>> {
        val mutation = SaveMessageActivityMutation(
            id = Input.optional(id),
            message = Input.fromNullable(message),
            recipientId = Input.fromNullable(recipientId),
            private_ = Input.fromNullable(private)
        )
        val mutationCall = apolloHandler.apolloClient.mutate(mutation)
        return Rx2Apollo.from(mutationCall)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun saveActivityReply(
        id: Int?,
        activityId: Int,
        text: String
    ): Observable<Response<SaveActivityReplyMutation.Data>> {
        val mutation = SaveActivityReplyMutation(
            id = Input.optional(id),
            activityId = Input.fromNullable(activityId),
            text = Input.fromNullable(text)
        )
        val mutationCall = apolloHandler.apolloClient.mutate(mutation)
        return Rx2Apollo.from(mutationCall)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}