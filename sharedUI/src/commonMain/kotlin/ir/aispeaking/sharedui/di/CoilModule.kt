package ir.aispeaking.sharedui.di

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import okio.FileSystem
import org.koin.dsl.module

val coilModule = module {
    single<ImageLoader> {
        val context: PlatformContext = get()
        newImageLoader(context)
    }
}

fun newImageLoader(context: PlatformContext): ImageLoader {
    return ImageLoader.Builder(context)
        .components {
            add(KtorNetworkFetcherFactory())
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "coil3_stage_cache")
                .maxSizeBytes(100L * 1024 * 1024) // 100MB disk cache
                .build()
        }
        .crossfade(true)
        .build()
}
