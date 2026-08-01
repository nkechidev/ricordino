package com.ricordino.di

import android.content.Context
import androidx.room.Room
import com.ricordino.data.local.NoteDao
import com.ricordino.data.local.RicordinoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RicordinoDatabase =
        Room.databaseBuilder(context, RicordinoDatabase::class.java, "ricordino.db").build()

    @Provides
    fun provideNoteDao(database: RicordinoDatabase): NoteDao = database.noteDao()
}
