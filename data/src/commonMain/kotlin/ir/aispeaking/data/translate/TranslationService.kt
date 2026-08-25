package ir.aispeaking.data.translate

import ir.aispeaking.domain.model.translate.Translation
import org.koin.core.annotation.Single

@Single
class TranslationService : TranslatorService {
//    private val translator by lazy { Translator() }
//    private val source = Language.ENGLISH
//    private val target = Language.PERSIAN

    override suspend fun translate(
        text: String,
    ): Result<Translation> {
        TODO("implement translator")
    }
}