package com.musicx.player.data.remote

import com.musicx.player.data.model.*
import com.musicx.player.data.security.SecurePreferencesManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface MusicApiService {
    @GET("api/v1/health")
    suspend fun getHealth(): HealthResponse

    @GET("api/v1/music/library")
    suspend fun getLibrary(@Query("page") page: Int = 1, @Query("limit") limit: Int = 50): LibraryResponse

    @GET("api/v1/music/search")
    suspend fun search(@Query("q") query: String): SearchResponse

    @GET("api/v1/music/artists")
    suspend fun getArtists(): ArtistsResponse

    @GET("api/v1/music/albums")
    suspend fun getAlbums(): AlbumsResponse

    @GET("api/v1/music/genres")
    suspend fun getGenres(): GenresResponse

    @GET("api/v1/music/random")
    suspend fun getRandom(@Query("limit") limit: Int = 10): LibraryResponse

    @GET("api/v1/music/stats")
    suspend fun getStats(): StatsResponse
}

class NetworkClientProvider(private val securePrefs: SecurePreferencesManager) {
    private var cachedService: MusicApiService? = null
    private var cachedUrl: String = ""

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val orig = chain.request()
                val token = securePrefs.getApiToken()
                val reqBuilder = orig.newBuilder()
                if (token.isNotBlank()) reqBuilder.header("Authorization", "Bearer $token")
                val urlBuilder = orig.url.newBuilder()
                if (token.isNotBlank() && orig.url.queryParameter("api_key") == null) {
                    urlBuilder.addQueryParameter("api_key", token)
                }
                chain.proceed(reqBuilder.url(urlBuilder.build()).build())
            }
            .build()
    }

    @Synchronized
    fun getService(): MusicApiService {
        val url = securePrefs.getBaseUrl().let { if (it.endsWith("/")) it else "$it/" }
        if (cachedService != null && cachedUrl == url) return cachedService!!
        cachedUrl = url
        cachedService = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MusicApiService::class.java)
        return cachedService!!
    }

    fun buildStreamUrl(songId: String): String {
        val base = securePrefs.getBaseUrl().trimEnd('/')
        val token = securePrefs.getApiToken()
        return if (token.isNotBlank()) "$base/api/v1/music/song/$songId/stream?api_key=$token"
        else "$base/api/v1/music/song/$songId/stream"
    }
}
