package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */

//This class is the same as ViewModel, but it takes the application context as a parameter
// and makes it available as a property. We are going to need this later on to access resources
// such as strings and styles through Application context.
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    // A Job is background job that runs in the Coroutine scope(here Main Scope)
    private var viewModelJob = Job()

    // To cancel the job when the ViewModel instance is destroyed
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    // To update the UI(which will be done by the View Model) using the Main thread,
    // so the dispatcher is Main thread and the job is viewModelJob
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Variable to hold data of current night
    private var tonight = MutableLiveData<SleepNight?>()

    // Getting all data from DAO
    // getAllNights() returns LiveData of List of SleepNight
    private val nights = database.getAllNights()

    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()

    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality

    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight() {

        // Using coroutine to get data from DB so that it is Non blocking in nature
        // UI Scope specifies that the coroutine will work in
        uiScope.launch {

            tonight.value = getTonightFromDatabase()
        }
    }

    /**
     * Suspended function to get current night data from DB
     */
    private suspend fun getTonightFromDatabase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            if (night?.startTimeMilli != night?.endTimeMilli) {
                night = null
            }
            night
        }
    }

    /**
     * To insert the new night that's been tracked
     */
    fun onStartTracking() {
        uiScope.launch {
            val newNight = SleepNight()

            insert(newNight)

            tonight.value = getTonightFromDatabase()
        }

    }

    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }

    fun onStopTracking() {
        uiScope.launch {
            // return@launch specifies from which among several nested function it returns from.
            val oldNight = tonight.value ?: return@launch

            oldNight.endTimeMilli = System.currentTimeMillis()

            update(oldNight)

            _navigateToSleepQuality.value = oldNight
        }
    }

    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

}

