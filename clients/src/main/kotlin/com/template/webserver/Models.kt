package com.template.webserver

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class AppleBasket @JsonCreator constructor (
    val description: String,
    val owner: String,
    val weight: Int
)

data class AppleStamp @JsonCreator constructor(
    @JsonProperty("description") val description: String,
    @JsonProperty("issuer") val issuer: String?,
    @JsonProperty("holder") val holder: String,
    @JsonProperty("weight") val weight: Int,
    @JsonProperty("id") val id: String?
)

data class AppleStampRedemption @JsonCreator constructor(
    val holder: String,
    val id: String
)