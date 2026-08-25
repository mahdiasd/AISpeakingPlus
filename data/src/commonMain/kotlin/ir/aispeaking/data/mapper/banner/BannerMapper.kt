package ir.aispeaking.data.mapper.banner

import ir.aispeaking.domain.model.banner.Banner
import ir.aispeaking.domain.model.banner.LinkType
import ir.aispeaking.network.dto.banner.BannerResponse

fun BannerResponse.toDomain(): Banner {
    return Banner(
        bannerLink = bannerLink,
        clickableLink = clickableLink,
        linkType = linkType.toLinkType()
    )
}

fun String.toLinkType(): LinkType {
    return when (this) {
        LinkType.ExternalLink.name -> LinkType.ExternalLink
        LinkType.InternalLink.name -> LinkType.InternalLink
        else -> throw Exception("toLinkType is null")
    }
}