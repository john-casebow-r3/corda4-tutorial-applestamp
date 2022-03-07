package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.BasketOfApplesContract
import com.template.states.AppleStamp
import com.template.states.BasketOfApples
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*
import java.util.Arrays.asList


@InitiatingFlow
@StartableByRPC
class RedeemStampInitiator(private val buyer: Party, private val stampId: UniqueIdentifier) :
    FlowLogic<SignedTransaction>() {

    private fun equals(party1: Party, party2: Party): Boolean {
        return party1.name.commonName.equals(party2.name.commonName)
    }

    @Suspendable
    override fun call(): SignedTransaction {

        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // Find the AppleStamp input state with the correct stamp id
        val inputCriteria: QueryCriteria = QueryCriteria.LinearStateQueryCriteria()
            .withStatus(Vault.StateStatus.UNCONSUMED)
            .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT)
            .withUuid(listOf(UUID.fromString(stampId.toString())))
        val appleStamp: StateAndRef<AppleStamp> = serviceHub.vaultService.queryBy(AppleStamp::class.java, inputCriteria).states.get(0)
        val stampWeight: Int = appleStamp.state.data.weight
        val stampIssuer: Party = appleStamp.state.data.issuer

        // Find the BasketOfApples output state that we want to modify the owner of
        // Needs to be of the same weight

        val outputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria()
            .withStatus(Vault.StateStatus.UNCONSUMED)
            .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT)
        val states = serviceHub.vaultService.queryBy(BasketOfApples::class.java, outputCriteria).states
        val basketOfApples: StateAndRef<BasketOfApples> = states.filter{
                it -> it.state.data.weight == stampWeight && equals(it.state.data.owner, stampIssuer)
        }.get(0)
        val original: BasketOfApples = basketOfApples.state.data

        val newState = original.changeOwner(buyer)

        val txBuilder: TransactionBuilder = TransactionBuilder(notary)
            .addInputState(appleStamp)
            .addInputState(basketOfApples)
            .addOutputState(newState, BasketOfApplesContract.ID)
            .addCommand(BasketOfApplesContract.Commands.Redeem(), asList(ourIdentity.owningKey, buyer.owningKey))

        txBuilder.verify(serviceHub)

        val partiallySignedTx = serviceHub.signInitialTransaction(txBuilder)
        val session: FlowSession = initiateFlow(buyer)
        val fullySignedTx = subFlow(CollectSignaturesFlow(partiallySignedTx, Arrays.asList(session)))

        return subFlow(FinalityFlow(fullySignedTx, Arrays.asList(session)))
    }
}

@InitiatedBy(RedeemStampInitiator::class)
class RedeemStampResponder(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {

        val signedTransaction = subFlow(object : SignTransactionFlow(session) {
            @Throws(FlowException::class)
            override fun checkTransaction(stx: SignedTransaction) {
                // NOP
            }
        })
        return subFlow(ReceiveFinalityFlow(session, signedTransaction.id))
    }
}
