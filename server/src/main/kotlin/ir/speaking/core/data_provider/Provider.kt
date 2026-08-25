package ir.speaking.core.data_provider

import ir.speaking.core.data_provider.challenge.populateAdvancedChallenges
import ir.speaking.core.data_provider.challenge.populateChallenges
import ir.speaking.core.data_provider.discount.populateDiscount
import ir.speaking.core.data_provider.plan.populatePlans
import ir.speaking.core.data_provider.scenario.*
import ir.speaking.core.data_provider.user.populateUsers
import ir.speaking.core.data_provider.word.populateDailyWords

object Provider {

    fun initData() {
        populateResilienceAndHope()
    }
}