package com.jppedrosa.moovies.di

import com.jppedrosa.moovies.BuildConfig
import com.jppedrosa.moovies.data.remote.TmdbApi
import com.jppedrosa.moovies.data.repository.RepositoryImpl
import com.jppedrosa.moovies.domain.repository.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * @author João Pedro Pedrosa (<a href="mailto:joaopopedrosa@gmail.com">joaopopedrosa@gmail.com</a>) on 23/09/2022.
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
        httpClient.addInterceptor(interceptor)
        httpClient.addInterceptor(Interceptor { chain: Interceptor.Chain ->
            val original: Request = chain.request()
            val request: Request = original.newBuilder()
                .header("Content-Type", "application/json;charset=utf-8")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        })
        httpClient.addInterceptor(Interceptor { chain: Interceptor.Chain ->
            val request: Request = chain.request()
            val newRequest: Request = request.newBuilder()
                .addHeader("Authorization", "Bearer " + BuildConfig.TMDB_ACCESS_TOKEN)
                .build()
            chain.proceed(newRequest)
        })
        return httpClient.build()
    }

    @Provides
    @Singleton
    fun provideTmdbApi(okHttpClient: OkHttpClient): TmdbApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.TMDB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(api: TmdbApi): Repository {
        return RepositoryImpl(api)
    }
}