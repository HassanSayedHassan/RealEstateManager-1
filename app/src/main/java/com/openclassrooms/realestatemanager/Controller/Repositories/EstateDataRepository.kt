package com.openclassrooms.realestatemanager.Controller.Repositories

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.persistence.db.SimpleSQLiteQuery
import android.os.AsyncTask
import android.util.Log
import com.openclassrooms.realestatemanager.Database.Dao.EstateDao
import com.openclassrooms.realestatemanager.Database.RealEstateManagerDatabase
import com.openclassrooms.realestatemanager.Models.Estate
import com.openclassrooms.realestatemanager.Models.FullEstate
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.Executor

/**
 * Created by Adrien Deguffroy on 11/10/2018.
 */
class EstateDataRepository(private val database: RealEstateManagerDatabase) {

    // --- GET ---

    fun getEstates(): LiveData<List<FullEstate>> {
        return this.database.estateDao().getItems()
    }

    fun gesEstatesBySearch(query:SimpleSQLiteQuery) : LiveData<List<FullEstate>>{
        return this.database.estateDao().getItemsBySearch(query)
    }

    fun gesEstateByID(estateID:Long) : LiveData<FullEstate>{
        return this.database.estateDao().getItemsByID(estateID)
    }

    // --- CREATE ---

    fun createEstate(estate: Estate) : Observable<Long> {
       return Observable.fromCallable{database.estateDao().insertItem(estate)}
    }

    // --- UPDATE ---
    fun updateEstate(estate: Estate) : Observable<Any> {
      return Observable.fromCallable{database.estateDao().updateItem(estate)}
    }

}