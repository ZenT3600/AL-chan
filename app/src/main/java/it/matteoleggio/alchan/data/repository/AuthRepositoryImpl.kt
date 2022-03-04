package it.matteoleggio.alchan.data.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloHttpException
import it.matteoleggio.alchan.data.datasource.UserDataSource
import it.matteoleggio.alchan.data.localstorage.AppSettingsManager
import it.matteoleggio.alchan.data.localstorage.UserManager
import it.matteoleggio.alchan.data.network.Converter
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.libs.SingleLiveEvent
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver

class AuthRepositoryImpl(private val userDataSource: UserDataSource,
                         private val userManager: UserManager
) : AuthRepository {

    private val _viewerDataResponse = SingleLiveEvent<Resource<Boolean>>()
    override val viewerDataResponse: LiveData<Resource<Boolean>>
        get() = _viewerDataResponse

    override val isLoggedIn: Boolean
        get() = userManager.bearerToken != null

    override fun setBearerToken(accessToken: String) {
        userManager.setBearerToken(accessToken)
    }

    @SuppressLint("CheckResult")
    override fun retrieveViewerData() {
        _viewerDataResponse.postValue(Resource.Loading())

        userDataSource.getViewerData().subscribeWith(object : Observer<Response<ViewerQuery.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<ViewerQuery.Data>) {
                if (t.hasErrors()) {
                    _viewerDataResponse.postValue(Resource.Error(t.errors!![0].message))
                } else {
                    userManager.setViewerData(Converter.convertUser(t.data?.viewer))
                    _viewerDataResponse.postValue(Resource.Success(true))
                }
            }

            override fun onError(e: Throwable) {
                AndroidUtility.rxApolloHandleError(_viewerDataResponse, e)
            }

            override fun onComplete() { }
        })
    }
}