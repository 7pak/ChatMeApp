package io.abood.messengerfacebood.notification

import io.abood.messengerfacebood.notification.Contacts.Companion.CONTENT_TYPE
import io.abood.messengerfacebood.notification.Contacts.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {
    @Headers("Authorization: key=${SERVER_KEY}","Content-Type:${CONTENT_TYPE}")
    @POST("fcm/send")
    suspend fun pushNotification(
        @Body notification: PushNotification
    ):Response<ResponseBody>

}