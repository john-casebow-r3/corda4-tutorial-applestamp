package com.template.webserver

import com.template.flows.CreateAndIssueAppleStampInitiator
import com.template.flows.PackageApplesInitiator
import com.template.flows.RedeemStampInitiator
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.FlowHandle
import net.corda.core.transactions.SignedTransaction
import org.springframework.stereotype.Component

@Component
class FlowService(val rpc: NodeRPCConnection) {

    fun getFlows() : List<String> {
        return rpc.proxy.registeredFlows()
    }

    fun packAppleBasket(description: String, weight: Int): FlowHandle<SignedTransaction> {
        return rpc.proxy.startFlowDynamic(
            PackageApplesInitiator::class.java,
            description,
            weight
        )
    }

    fun issueAppleStamp(description: String, holder: String, weight: Int): FlowHandle<SignedTransaction> {
        var holderParty: Party? = getParty(holder)
        return issueAppleStamp(description, holderParty!!, weight)
    }

    fun issueAppleStamp(description: String, holder: Party, weight: Int): FlowHandle<SignedTransaction> {
        return rpc.proxy.startFlowDynamic(
            CreateAndIssueAppleStampInitiator::class.java,
            description,
            holder,
            weight
        )
    }

    fun redeemAppleStamp(buyer: String, stampId: String): FlowHandle<SignedTransaction> {
        var buyerParty: Party? = getParty(buyer)
        return redeemAppleStamp(buyerParty!!, stampId)
    }

    fun redeemAppleStamp(buyer: Party, stampId: String): FlowHandle<SignedTransaction> {
        return rpc.proxy.startFlowDynamic(
            RedeemStampInitiator::class.java,
            buyer,
            UniqueIdentifier.fromString(stampId)
        )
    }

    fun getParty(partyName: String) : Party? {
        return if (partyName.startsWith("O="))
            rpc.proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(partyName))
        else
            rpc.proxy.partiesFromName(partyName, false).first()
    }
}
