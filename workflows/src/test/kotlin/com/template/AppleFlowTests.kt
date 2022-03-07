package com.template

import com.google.common.collect.ImmutableList
import com.template.flows.CreateAndIssueAppleStampInitiator
import com.template.flows.PackageApplesInitiator
import com.template.flows.RedeemStampInitiator
import com.template.states.AppleStamp
import com.template.states.BasketOfApples
import groovy.util.GroovyTestCase.assertEquals
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
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
        farm.startFlow(PackageApplesInitiator("10 apples", 10))
        farm.startFlow(PackageApplesInitiator("20 apples", 20))
        farm.startFlow(PackageApplesInitiator("30 apples", 30))
        network.runNetwork()

        val farmParty = farm.info.legalIdentities[0]
        val buyerParty = buyer.info.legalIdentities[0]

        val stampFuture: Future<SignedTransaction> = farm
            .startFlow(CreateAndIssueAppleStampInitiator("A voucher for 20 apples", buyerParty,20))
        network.runNetwork()

        val appleStamp:AppleStamp = stampFuture.get().tx.outputStates.get(0) as AppleStamp
        val stampId: UniqueIdentifier = appleStamp.linearId

        val redemptionFuture: Future<SignedTransaction> = farm.startFlow(RedeemStampInitiator(buyerParty, stampId))
        network.runNetwork()

        // Assert that the basket of 20 apples is now owned by the buyer
        val basketCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.UNCONSUMED)
        var transactedBasket: BasketOfApples = this.buyer.services.vaultService
            .queryBy(BasketOfApples::class.java, basketCriteria).states.filter{ it -> it.state.data.weight == 20}.first().state.data
        assertEquals(buyerParty, transactedBasket.owner)

        //Assert that the other baskets are not owned by the buyer
        var untransactedBasket: BasketOfApples = this.farm.services.vaultService
            .queryBy(BasketOfApples::class.java, basketCriteria).states.filter{ it -> it.state.data.weight == 10}.first().state.data
        assertEquals(farmParty, untransactedBasket.owner)
    }
}