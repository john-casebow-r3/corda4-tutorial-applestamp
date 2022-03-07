package com.template.contracts

import com.template.states.AppleStamp
import com.template.states.BasketOfApples
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class BasketOfApplesContractTests {
    private val ledgerServices: MockServices = MockServices(listOf("com.template"))
    var alice = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var bob = TestIdentity(CordaX500Name("Bob", "TestLand", "US"))
    var fred = TestIdentity(CordaX500Name("Fred", "TestLand", "US"))

    @Test
    fun pack_basket_command_should_have_no_input_states_and_one_output_state() {
        val state = BasketOfApples("test", alice.party, bob.party, 100);
        ledgerServices.ledger {
            // fail
            transaction {
                input(BasketOfApplesContract.ID, state)
                output(BasketOfApplesContract.ID, state)
                command(alice.publicKey, BasketOfApplesContract.Commands.PackBasket())
                fails()
            }
            // fail
            transaction {
                output(BasketOfApplesContract.ID, state)
                output(BasketOfApplesContract.ID, state)
                command(alice.publicKey, BasketOfApplesContract.Commands.PackBasket())
                fails()
            }
        }
    }

    @Test
    fun pack_basket_command_output_state_should_have_description_and_weight() {
        val badStateNoDesc = BasketOfApples("", alice.party, bob.party, 100);
        val badStateNoWeight = BasketOfApples("test", alice.party, bob.party, 0);
        val goodState = BasketOfApples("test", alice.party, bob.party, 100);
        ledgerServices.ledger {
            // fail
            transaction {
                output(BasketOfApplesContract.ID, badStateNoDesc)
                command(alice.publicKey, BasketOfApplesContract.Commands.PackBasket())
                fails()
            }
            // fail
            transaction {
                output(BasketOfApplesContract.ID, badStateNoWeight)
                command(alice.publicKey, BasketOfApplesContract.Commands.PackBasket())
                fails()
            }            // fail
            transaction {
                output(BasketOfApplesContract.ID, goodState)
                command(alice.publicKey, BasketOfApplesContract.Commands.PackBasket())
                verifies()
            }
        }
    }

    @Test
    fun redeem_command_should_have_two_input_states_and_one_output_state() {
        val initialBasketState = BasketOfApples("test", alice.party, alice.party, 100);
        val stampState = AppleStamp("test", alice.party, bob.party, 10, UniqueIdentifier("foo"));
        val basketState = BasketOfApples("test", alice.party, bob.party, 100);
        ledgerServices.ledger {
            // fail
            transaction {
                input(BasketOfApplesContract.ID, stampState)
                command(alice.publicKey, BasketOfApplesContract.Commands.Redeem())
                fails()
            }
            // fail
            transaction {
                output(BasketOfApplesContract.ID, basketState)
                command(alice.publicKey, BasketOfApplesContract.Commands.Redeem())
                fails()
            }
            // pass
            transaction {
                input(BasketOfApplesContract.ID, stampState)
                input(BasketOfApplesContract.ID, initialBasketState)
                output(BasketOfApplesContract.ID, basketState)
                command(alice.publicKey, BasketOfApplesContract.Commands.Redeem())
                verifies()
            }
        }
    }

    @Test
    fun redeem_command_issuer_of_stamp_should_be_same_as_basket() {
        val stampState = AppleStamp("test", alice.party, bob.party, 10, UniqueIdentifier("foo"));
        val badInputBasketState = BasketOfApples("test", fred.party, fred.party, 100);
        val goodInputBasketState = BasketOfApples("test", alice.party, alice.party, 100);
        val outputBasketState = BasketOfApples("test", alice.party, bob.party, 100);
        ledgerServices.ledger {
            transaction {
                input(AppleStampContract.ID, stampState)
                input(BasketOfApplesContract.ID, badInputBasketState)
                output(BasketOfApplesContract.ID, outputBasketState)
                command(alice.publicKey, BasketOfApplesContract.Commands.Redeem())
                fails()
            }
            // pass
            transaction {
                input(AppleStampContract.ID, stampState)
                input(BasketOfApplesContract.ID, goodInputBasketState)
                output(BasketOfApplesContract.ID, outputBasketState)
                command(alice.publicKey, BasketOfApplesContract.Commands.Redeem())
                verifies()
            }
        }
    }

    @Test
    fun redeem_command_weight_of_stamp_should_be_same_as_basket() {
        val stampState = AppleStamp("test", alice.party, bob.party, 10, UniqueIdentifier("foo"));
        val badInputBasketState = BasketOfApples("test", alice.party, alice.party, 100);
        val goodInputBasketState = BasketOfApples("test", alice.party, alice.party, 10);
        val outputBasketState = BasketOfApples("test", alice.party, bob.party, 10);
        ledgerServices.ledger {
            transaction {
                input(AppleStampContract.ID, stampState)
                input(BasketOfApplesContract.ID, badInputBasketState)
                output(BasketOfApplesContract.ID, outputBasketState)
                command(alice.publicKey, BasketOfApplesContract.Commands.Redeem())
                fails()
            }
            // pass
            transaction {
                input(AppleStampContract.ID, stampState)
                input(BasketOfApplesContract.ID, goodInputBasketState)
                output(BasketOfApplesContract.ID, outputBasketState)
                command(alice.publicKey, BasketOfApplesContract.Commands.Redeem())
                verifies()
            }
        }
    }

    @Test
    fun redeem_command_weight_should_be_greater_than_zero() {
        val initialBasketState = BasketOfApples("test", alice.party, alice.party, 100);
        val stampState = AppleStamp("test", alice.party, bob.party, 10, UniqueIdentifier("foo"));
        val badBasketState = BasketOfApples("test", alice.party, bob.party, 0);
        val goodBasketState = BasketOfApples("test", alice.party, bob.party, 100);
        ledgerServices.ledger {
            transaction {
                input(AppleStampContract.ID, stampState)
                input(BasketOfApplesContract.ID, initialBasketState)
                output(BasketOfApplesContract.ID, badBasketState)
                command(alice.publicKey, BasketOfApplesContract.Commands.Redeem())
                fails()
            }
            // pass
            transaction {
                input(AppleStampContract.ID, stampState)
                input(BasketOfApplesContract.ID, initialBasketState)
                output(BasketOfApplesContract.ID, goodBasketState)
                command(alice.publicKey, BasketOfApplesContract.Commands.Redeem())
                verifies()
            }
        }
    }

    @Test
    fun bad_command() {
        val stampState = AppleStamp("test", alice.party, bob.party, 10, UniqueIdentifier("foo"));
        val goodBasketState = BasketOfApples("test", alice.party, bob.party, 100);
        ledgerServices.ledger {
            transaction {
                input(AppleStampContract.ID, stampState)
                output(BasketOfApplesContract.ID, goodBasketState)
                command(alice.publicKey, AppleStampContract.Commands.Issue())
                fails()
            }
        }
    }
}