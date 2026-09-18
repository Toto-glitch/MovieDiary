package com.example.moviediary

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.moviediary.dao.GenreDao
import com.example.moviediary.dao.MovieDao
import com.example.moviediary.dao.MovieTagDao
import com.example.moviediary.dao.TagDao
import com.example.moviediary.models.Genre
import com.example.moviediary.models.Movie
import com.example.moviediary.models.MovieTagRef
import com.example.moviediary.models.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

@Database(
    entities = [Movie::class, Genre::class, Tag::class, MovieTagRef::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
    abstract fun genreDao(): GenreDao
    abstract fun tagDao(): TagDao
    abstract fun movieTagDao(): MovieTagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "movie_diary"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val genreDao = getDatabase(context).genreDao()
                                listOf("Драма", "Фантастика", "Криминал", "Комедия", "Триллер",
                                    "Биография", "Анимация", "Ужасы", "История", "Романтика")
                                    .forEach { genreDao.insert(Genre(name = it)) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
