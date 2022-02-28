package com.template.states

import groovy.util.GroovyTestCase.assertEquals
import junit.framework.Assert.assertFalse
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import org.junit.Test

class BasketOfApplesTests {

    var farm = TestIdentity(CordaX500Name("Home Farm", "TestLand", "US"))
    var bob = TestIdentity(CordaX500Name("Bob", "TestLand", "US"))

    @Test
    fun constructor_test() {
        val state = BasketOfApples("test", farm.party, bob.party, 100);
        assertEquals(listOf(farm.party, bob.party), state.participants)
    }

    @Test
    fun secondary_constructor_test() {
        val state = BasketOfApples("test", farm.party,100);
        assertEquals(farm.party, state.farm)
        assertEquals(farm.party, state.owner)
        assertEquals(listOf(farm.party, farm.party), state.participants)
    }

    @Test
    fun changeowner_creates_new_instance() {
        val state = BasketOfApples("test", farm.party,100);
        val newState = state.changeOwner(bob.party)
        assertEquals(farm.party, state.owner)
        assertEquals(bob.party, newState.owner)
        assertFalse(state == newState)
    }
}