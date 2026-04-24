package com.daniel.pastedrop.di

import android.content.Context
import androidx.room.Room
import com.daniel.pastedrop.data.local.PasteDropDatabase
import com.daniel.pastedrop.data.local.SnippetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): PasteDropDatabase =
        Room.databaseBuilder(ctx, PasteDropDatabase::class.java, "pastedrop.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSnippetDao(db: PasteDropDatabase): SnippetDao = db.snippetDao()
}
