package com.template.contracts

import com.template.states.AppleStamp
import com.template.states.BasketOfApples
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class AppleStampContractTests {
    private val ledgerServices: MockServices = MockServices(listOf("com.template"))
    var alice = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var bob = TestIdentity(CordaX500Name("Bob", "TestLand", "US"))

    @Test
    fun issue_command_should_have_no_input_states_and_one_output_state() {
        val state = AppleStamp("test", alice.party, bob.party, 10, UniqueIdentifier("foo"));
        val state2 = AppleStamp("test2", alice.party, bob.party, 10, UniqueIdentifier("foo2"));
        ledgerServices.ledger {
            // fail
            transaction {
                input(AppleStampContract.ID, state)
                output(AppleStampContract.ID, state)
                command(alice.publicKey, AppleStampContract.Commands.Issue())
                fails()
            }
            // fail
            transaction {
                //failing transaction
                output(AppleStampContract.ID, state)
                output(AppleStampContract.ID, state2)
                command(alice.publicKey, AppleStampContract.Commands.Issue())
                fails()
            }
            // pass
            transaction {
                //passing transaction
                output(AppleStampContract.ID, state)
                command(alice.publicKey, AppleStampContract.Commands.Issue())
                verifies()
            }
        }
    }

    @Test
    fun issue_command_output_state_should_have_description() {
        val badState = AppleStamp("", alice.party, bob.party, 10, UniqueIdentifier("foo"));
        val goodState = AppleStamp("test2", alice.party, bob.party, 10, UniqueIdentifier("foo"));
        ledgerServices.ledger {
            // fail
            transaction {
                output(AppleStampContract.ID, badState)
                command(alice.publicKey, AppleStampContract.Commands.Issue())
                fails()
            }
            // pass
            transaction {
                output(AppleStampContract.ID, goodState)
                command(alice.publicKey, AppleStampContract.Commands.Issue())
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
                input(BasketOfApplesContract.ID, stampState)
                output(BasketOfApplesContract.ID, goodBasketState)
                command(alice.publicKey, BasketOfApplesContract.Commands.PackBasket())
                fails()
            }
        }
    }
}