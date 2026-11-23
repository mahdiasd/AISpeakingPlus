package ir.aispeaking.data.mapper.leitner

import ir.aispeaking.data.model.leitner.FlashcardDto
import ir.aispeaking.domain.model.leitner.Flashcard

internal fun FlashcardDto.toDomain(): Flashcard {
    return Flashcard(
        id = id,
        userId = userId,
        lexiconId = lexiconId,
        customWord = customWord,
        customMeaning = customMeaning,
        customExample = customExample,
        customAudioUrl = customAudioUrl,
        boxNumber = boxNumber,
        reviewCount = reviewCount,
        lastReviewedAt = lastReviewedAt,
        nextReviewAt = nextReviewAt,
        createdAt = createdAt,
        headword = headword,
        phonetic = phonetic,
        partOfSpeech = partOfSpeech,
        meaningFa = meaningFa,
        exampleEn = exampleEn,
        exampleFa = exampleFa,
        audioUrl = audioUrl
    )
}
