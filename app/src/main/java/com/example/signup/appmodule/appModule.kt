package com.example.signup.appmodule

import com.example.signup.realtimedatabase.ProfileRepository
import com.example.signup.realtimedatabase.RealTimeRepositoryImpl
import com.example.signup.realtimedatabase.RealTimeService
import com.example.signup.realtimedatabase.Repository1
import com.example.signup.signuprepo.MainViewModel
import com.example.signup.signuprepo.Repository
import com.example.signup.signuprepo.RepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val appModule = module {
    single {
        FirebaseAuth.getInstance()
    }

    single<Repository> {
        RepositoryImpl(get(), get())
    }

    single {
        MainViewModel(get())
    }

    single<DatabaseReference> {
        FirebaseDatabase.getInstance().reference
    } withOptions {
        qualifier("FirebaseDb")
    }

    single<RealTimeService> { RealTimeRepositoryImpl(get()) }
    single<Repository1> { Repository1(get()) }
    single<ProfileRepository> { ProfileRepository(get()) }

}
