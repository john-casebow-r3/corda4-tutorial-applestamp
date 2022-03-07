package com.template.states

import com.template.contracts.AppleStampContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(AppleStampContract::class)
data class AppleStamp(val stampDesc:String,
                      val issuer: Party,
                      val holder: Party,
                      val weight: Int,
                      override val linearId: UniqueIdentifier,
                      override val participants: List<AbstractParty> = listOf(issuer, holder)
) : LinearState