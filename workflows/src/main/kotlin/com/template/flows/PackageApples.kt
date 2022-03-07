package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.BasketOfApplesContract
import com.template.states.BasketOfApples
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.Arrays.asList

@InitiatingFlow
@StartableByRPC
class PackageApplesInitiator(private val appleDescription: String, private val weight: Int) :
    FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {

        val notary: Party = serviceHub.networkMapCache.notaryIdentities[0];
        val identifier: UniqueIdentifier = UniqueIdentifier();
        val initialState: BasketOfApples = BasketOfApples(appleDescription, ourIdentity, weight)

        var builder: TransactionBuilder = TransactionBuilder(notary)
            .addOutputState(initialState, BasketOfApplesContract.ID)
            .addCommand(BasketOfApplesContract.Commands.PackBasket(), asList(ourIdentity.owningKey))
        builder.verify(serviceHub)

        val initialTransaction = serviceHub.signInitialTransaction(builder)

        return subFlow(FinalityFlow(initialTransaction, asList()))
    }
}
