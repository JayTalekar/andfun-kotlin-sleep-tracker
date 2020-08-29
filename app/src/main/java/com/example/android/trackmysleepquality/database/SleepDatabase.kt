/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// To tell which entity to use use the parameters with Annotation
@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase() {

    // Intialize the DAO related with the entity, so to interact with the Database
    abstract val sleepDatabaseDao: SleepDatabaseDao

    //Companion Object to access the database without instantiating the class
    companion object {

        @Volatile
        private var INSTANCE: SleepDatabase? = null

        // A method to get the Database instance
        fun getInstance(context: Context): SleepDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {

                    // Room's Database builder to instantiate the local variable
                    /*Migration fallback means that if user has upgraded the app
                      with newer schema of database then the database builder will
                      make the instance with newer schema by convering the old schema
                      to new schema, so that users don't lose data on upgrading the app*/
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            SleepDatabase::class.java,
                            "sleep_history_database"
                    ).fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }

                return instance

            }

        }
    }


}
