package com.wellcheck.app.network

import com.google.gson.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

private class LocalDateDeserializer : JsonDeserializer<String> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): String {
        return when {
            json.isJsonArray -> {
                val arr   = json.asJsonArray
                val year  = arr[0].asInt
                val month = arr[1].asInt.toString().padStart(2, '0')
                val day   = arr[2].asInt.toString().padStart(2, '0')
                "$year-$month-$day"
            }
            json.isJsonPrimitive -> json.asString
            else -> ""
        }
    }
}

object RetrofitClient {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(String::class.java, LocalDateDeserializer())
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}