package com.template.states

import groovy.util.GroovyTestCase.assertEquals
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import org.junit.Test

class AppleStampTests {

    var alice = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var bob = TestIdentity(CordaX500Name("Bob", "TestLand", "US"))

    @Test
    fun constructorTest() {
        val state = AppleStamp("test", alice.party, bob.party, 10, UniqueIdentifier("foo"));
        assertEquals(listOf(alice.party, bob.party), state.participants)
    }
}