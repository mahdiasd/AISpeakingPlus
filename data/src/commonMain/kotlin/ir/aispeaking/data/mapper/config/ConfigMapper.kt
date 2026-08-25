package ir.aispeaking.data.mapper.config

import ir.aispeaking.domain.model.config.Config
import ir.aispeaking.domain.model.config.ConfigRequest
import ir.aispeaking.domain.model.config.Update
import ir.aispeaking.network.dto.config.ConfigResponse
import ir.aispeaking.network.dto.config.UpdateResponse

fun ConfigResponse.toDomain(): Config {
    return Config(
        update = update.toDomain()
    )
}

fun UpdateResponse.toDomain(): Update {
    return Update(
        forceVersion = forceVersion,
        lastVersion = lastVersion,
        suggestVersion = suggestVersion,
        link = link,
        message = message
    )
}

fun ConfigRequest.toRequest(): ir.aispeaking.network.dto.config.ConfigRequest {
    return ir.aispeaking.network.dto.config.ConfigRequest(
        deviceName = deviceName,
        androidVersion = androidVersion,
        firebaseToken = firebaseToken,
    )
}