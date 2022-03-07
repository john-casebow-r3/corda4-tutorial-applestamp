package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AppleStampContract
import com.template.states.AppleStamp
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.Arrays.asList

@InitiatingFlow
@StartableByRPC
class CreateAndIssueAppleStampInitiator(private val stampDescription: String, private val holder: Party, private val weight: Int) :
    FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {

        val notary: Party = serviceHub.networkMapCache.notaryIdentities.get(0);
        val stampId: UniqueIdentifier = UniqueIdentifier()
        val initialState: AppleStamp = AppleStamp(stampDescription, ourIdentity, holder, weight, stampId)

        var builder: TransactionBuilder = TransactionBuilder(notary)
            .addOutputState(initialState)
            .addCommand(AppleStampContract.Commands.Issue(), asList(ourIdentity.owningKey, holder.owningKey))
        builder.verify(serviceHub)

        val partiallySignedTx = serviceHub.signInitialTransaction(builder)
        val session: FlowSession = initiateFlow(holder)
        val fullySignedTx = subFlow(CollectSignaturesFlow(partiallySignedTx, asList(session)))

        return subFlow(FinalityFlow(fullySignedTx, asList(session)))
    }
}

@InitiatedBy(CreateAndIssueAppleStampInitiator::class)
class CreateAndIssueAppleStampResponder(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        logger.info("--------------")
        logger.info("Initiating Flow: CreateAndIssueAppleStampResponder")
        logger.info("--------------")

        val txnId = subFlow(object : SignTransactionFlow(session) {
            override fun checkTransaction(stx: SignedTransaction) {
                // NOP
            }
        }).id
        return subFlow(ReceiveFinalityFlow(session, expectedTxId = txnId))
    }
}
