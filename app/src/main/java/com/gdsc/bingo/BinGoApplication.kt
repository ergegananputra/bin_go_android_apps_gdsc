package com.gdsc.bingo

import android.app.Application
import com.gdsc.bingo.services.realm.model.ForumsRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class BinGoApplication : Application() {



    override fun onCreate() {
        super.onCreate()
//        DynamicColors.applyToActivitiesIfAvailable(this)

        realm = Realm.open(
            configuration = RealmConfiguration.create(
                schema = setOf(
                    ForumsRealm::class
                )
            )
        )
    }

    companion object {
        lateinit var realm : Realm
    }
}