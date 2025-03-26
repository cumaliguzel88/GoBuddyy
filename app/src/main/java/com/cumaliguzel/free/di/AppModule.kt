package com.cumaliguzel.free.di

import android.content.Context
import com.cumaliguzel.free.data.repository.MapsRepository
import com.cumaliguzel.free.domain.usecase.CalculateDistanceUseCase
import com.cumaliguzel.free.domain.usecase.ReverseGeocodingUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Singleton olarak Hilt'e bağlıyoruz
object AppModule {

    @Provides
    @Singleton
    fun provideMapsRepository(@ApplicationContext context: Context): MapsRepository {
        return MapsRepository(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    /*
    /**
     * 🔥 Realtime Database'i Hilt'e sağlıyoruz!
     */
    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

     */
    @Provides
    @Singleton
    fun provideReverseGeocodingUseCase(@ApplicationContext context: Context): ReverseGeocodingUseCase {
        return ReverseGeocodingUseCase(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        // Tam olarak kendi Realtime Database URL'ni kullan:
        // Firebase Console -> Realtime Database -> URL'yi kopyala
        return FirebaseDatabase.getInstance("https://gobuddy-45268-default-rtdb.europe-west1.firebasedatabase.app")
    }

    @Provides
    @Singleton
    fun provideCalculateDistanceUseCase(): CalculateDistanceUseCase {
        return CalculateDistanceUseCase()
    }



}