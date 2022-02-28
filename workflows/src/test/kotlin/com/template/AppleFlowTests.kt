package com.template

import com.google.common.collect.ImmutableList
import com.template.flows.CreateAndIssueAppleStampInitiator
import com.template.flows.PackageApplesInitiator
import com.template.flows.RedeemApplesInitiator
import com.template.states.AppleStamp
import com.template.states.BasketOfApples
import groovy.util.GroovyTestCase.assertEquals
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Future


class AppleFlowTests {
    private lateinit var network: MockNetwork
    private lateinit var farm: StartedMockNode
    private lateinit var buyer: StartedMockNode

    @Before
    fun setup() {
//        network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
//                TestCordapp.findCordapp("com.template.contracts"),
//                TestCordapp.findCordapp("com.template.flows")
//        )))
        network = MockNetwork(MockNetworkParameters().withCordappsForAllNodes(
            ImmutableList.of(
            TestCordapp.findCordapp("com.template.contracts"),
            TestCordapp.findCordapp("com.template.flows"))))

        farm = network.createPartyNode(CordaX500Name("Home Farm", "TestLand", "US"))
        buyer = network.createPartyNode(CordaX500Name("Bob", "TestLand", "US"))
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun redeem_basket_of_apples() {
        val packageFlow = PackageApplesInitiator("10 apples", 10)
        val basketFuture: Future<SignedTransaction> = farm.startFlow(packageFlow)
        network.runNetwork()

        val stampFlow = CreateAndIssueAppleStampInitiator("A voucher for 10 apples", buyer.info.legalIdentities[0])
        val stampFuture: Future<SignedTransaction> = farm.startFlow(stampFlow)
        network.runNetwork()

        val appleStamp:AppleStamp = stampFuture.get().tx.outputStates.get(0) as AppleStamp
        val stampId: UniqueIdentifier = appleStamp.linearId

        val redemptionFlow = RedeemApplesInitiator(buyer.info.legalIdentities[0], stampId)
        val redemptionFuture: Future<SignedTransaction> = farm.startFlow(redemptionFlow)
        network.runNetwork()

        //successful query means the state is stored at the buyer's vault. Flow went through.
        val basketCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.UNCONSUMED)
        var basketStates = buyer.services.vaultService.queryBy(BasketOfApples::class.java, basketCriteria).states
        var basketState: BasketOfApples = basketStates[0].state.data

        assertEquals("10 apples", basketState.description)

    }
}