package com.template.states

import com.template.contracts.BasketOfApplesContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(BasketOfApplesContract::class)
data class BasketOfApples(
    val description: String,
    val farm: Party,
    var owner: Party,
    val weight: Int,
    override val participants: List<AbstractParty> = listOf(farm, owner)) : ContractState {

        constructor(description: String, farm: Party, weight: Int) : this(description, farm, farm, weight)

        fun changeOwner(newOwner: Party): BasketOfApples {
            return BasketOfApples(description, farm, newOwner, weight)
        }
    }

