package ir.speaking.feature.config

import ir.speaking.feature.config.dto.AppVersionConfigDto
import ir.speaking.feature.config.dto.UpdateAppVersionConfigRequest
import ir.speaking.feature.config.repository.AppVersionConfigRepository
import ir.speaking.feature.config.service.AppConfigService
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AppConfigServiceTest {

    private class FakeAppVersionConfigRepository(
        var stored: AppVersionConfigDto? = null
    ) : AppVersionConfigRepository() {
        override suspend fun findByPlatform(platform: String): AppVersionConfigDto? {
            return stored?.takeIf { it.platform == platform.trim().lowercase() }
        }

        override suspend fun upsert(
            platform: String,
            req: UpdateAppVersionConfigRequest
        ): AppVersionConfigDto {
            val saved = AppVersionConfigDto(
                platform = platform.trim().lowercase(),
                lastVersion = req.lastVersion,
                minVersion = req.minVersion,
                suggestVersion = req.suggestVersion,
                link = req.link,
                message = req.message,
                updatedAt = "2026-11-27T16:00:00Z"
            )
            stored = saved
            return saved
        }
    }

    @Test
    fun `getAppVersionConfig and getUpdateResponseDto return safe defaults when db has no record`() = runBlocking {
        val service = AppConfigService(FakeAppVersionConfigRepository(stored = null), redissonClient = null)

        val config = service.getAppVersionConfig()
        assertEquals(AppConfigService.DEFAULT_PLATFORM, config.platform)
        assertEquals(AppConfigService.DEFAULT_LAST_VERSION, config.lastVersion)
        assertEquals(AppConfigService.DEFAULT_MIN_VERSION, config.minVersion)
        assertEquals(AppConfigService.DEFAULT_SUGGEST_VERSION, config.suggestVersion)
        assertEquals(AppConfigService.DEFAULT_LINK, config.link)
        assertEquals(AppConfigService.DEFAULT_MESSAGE, config.message)
        assertNull(config.updatedAt)

        val updateDto = service.getUpdateResponseDto()
        assertEquals(AppConfigService.DEFAULT_MIN_VERSION, updateDto.forceVersion)
        assertEquals(AppConfigService.DEFAULT_LAST_VERSION, updateDto.lastVersion)
        assertEquals(AppConfigService.DEFAULT_SUGGEST_VERSION, updateDto.suggestVersion)
        assertEquals(AppConfigService.DEFAULT_LINK, updateDto.link)
        assertEquals(AppConfigService.DEFAULT_MESSAGE, updateDto.message)
    }

    @Test
    fun `updateAppVersionConfig trims fields and maps minVersion to forceVersion`() = runBlocking {
        val repo = FakeAppVersionConfigRepository()
        val service = AppConfigService(repo, redissonClient = null)

        val updated = service.updateAppVersionConfig(
            UpdateAppVersionConfigRequest(
                lastVersion = 15,
                minVersion = 12,
                suggestVersion = 14,
                link = "  https://cafebazaar.ir/app/ir.aispeaking?l=fa  ",
                message = "  نسخه جدید منتشر شد.  "
            )
        )

        assertEquals(15, updated.lastVersion)
        assertEquals(12, updated.minVersion)
        assertEquals(14, updated.suggestVersion)
        assertEquals("https://cafebazaar.ir/app/ir.aispeaking?l=fa", updated.link)
        assertEquals("نسخه جدید منتشر شد.", updated.message)

        val updateDto = service.getUpdateResponseDto()
        assertEquals(12, updateDto.forceVersion)
        assertEquals(15, updateDto.lastVersion)
        assertEquals(14, updateDto.suggestVersion)
        assertEquals("https://cafebazaar.ir/app/ir.aispeaking?l=fa", updateDto.link)
        assertEquals("نسخه جدید منتشر شد.", updateDto.message)
    }

    @Test
    fun `updateAppVersionConfig rejects invalid version numbers, link, and blank message`() = runBlocking {
        val service = AppConfigService(FakeAppVersionConfigRepository(), redissonClient = null)

        assertFailsWith<IllegalArgumentException> {
            service.updateAppVersionConfig(
                UpdateAppVersionConfigRequest(0, 0, 0, "https://cafebazaar.ir/app/ir.aispeaking", "پیام")
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.updateAppVersionConfig(
                UpdateAppVersionConfigRequest(10, -1, 10, "https://cafebazaar.ir/app/ir.aispeaking", "پیام")
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.updateAppVersionConfig(
                UpdateAppVersionConfigRequest(10, 10, -1, "https://cafebazaar.ir/app/ir.aispeaking", "پیام")
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.updateAppVersionConfig(
                UpdateAppVersionConfigRequest(10, 11, 10, "https://cafebazaar.ir/app/ir.aispeaking", "پیام")
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.updateAppVersionConfig(
                UpdateAppVersionConfigRequest(10, 9, 11, "https://cafebazaar.ir/app/ir.aispeaking", "پیام")
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.updateAppVersionConfig(
                UpdateAppVersionConfigRequest(10, 9, 10, "cafebazaar.ir/app/ir.aispeaking", "پیام")
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.updateAppVersionConfig(
                UpdateAppVersionConfigRequest(10, 9, 10, "https://cafebazaar.ir/app/ir.aispeaking", "   ")
            )
        }
    }
}
