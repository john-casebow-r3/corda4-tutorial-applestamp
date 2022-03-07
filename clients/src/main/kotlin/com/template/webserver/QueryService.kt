package com.template.webserver

import com.template.states.AppleStamp
import com.template.states.BasketOfApples
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.Vault
import org.springframework.stereotype.Component

@Component
class QueryService(val rpc: NodeRPCConnection) {

    fun queryAppleBasket() : List<StateAndRef<BasketOfApples>> {
        val vaultQuery: Vault.Page<BasketOfApples> = rpc.proxy.vaultQuery(BasketOfApples::class.java)
        return vaultQuery.states
    }

    fun queryAppleStamp() : List<StateAndRef<AppleStamp>> {
        val vaultQuery: Vault.Page<AppleStamp> = rpc.proxy.vaultQuery(AppleStamp::class.java)
        return vaultQuery.states
    }
}