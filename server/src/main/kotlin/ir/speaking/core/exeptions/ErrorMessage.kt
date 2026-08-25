package ir.speaking.core.exeptions

object ErrorMessage {
    const val INVALID_MOBILE_NUMBER = "Your phone number is wrong!"
    const val USER_EXIST = "You already signed up!"
    const val DISCOUNT_CODE_EXIST = "You already signed up!"
    const val USER_NOT_EXIST = "We can't find you! Are you sure you signed up?"
    const val OTP_CODE_NOT_CORRECT = "The code you entered is wrong!"
    const val NOT_FOUND = "We can't find what you are looking for :("
    const val GONE_ACTIVE_WORD = "We don't have new words now. Please wait for the next word."
    const val GONE_ACTIVE_CHALLENGE = "We don't have a new game now. Please wait for the next game to start."
    const val NO_ACTIVE_SUBSCRIPTION = "There is no active plan."
    const val CONFLICT_TRANSLATION = "You already added this word/text."

    const val DONT_ACCESS = "You don't have permission to access this api."
}