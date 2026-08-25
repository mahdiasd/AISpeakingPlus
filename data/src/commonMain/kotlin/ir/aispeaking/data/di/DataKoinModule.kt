package ir.aispeaking.data.di

import ir.aispeaking.domain.di.DomainKoinModule
import ir.aispeaking.network.di.NetworkKoinModule
import ir.aispeaking.storage.di.StorageKoinModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@org.koin.core.annotation.Configuration
@ComponentScan("ir.aispeaking.data")
class DataKoinModule
