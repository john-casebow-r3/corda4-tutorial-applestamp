package com.template.contracts

import com.template.states.AppleStamp
import com.template.states.BasketOfApples
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class BasketOfApplesContract : Contract {

    companion object {
        const val ID = "com.template.contracts.BasketOfApplesContract"
    }

    override fun verify(tx: LedgerTransaction) {
        val command: CommandData = tx.commands.get(0).value
        val output: BasketOfApples = tx.outputsOfType<BasketOfApples>().get(0)

        when {
            command is Commands.PackBasket -> requireThat {
                "This transaction should not have any input states".using(tx.inputs.size == 0)
                "This transaction should only have one output state".using(tx.outputs.size == 1)
                "Output state should have a meaningful description".using(output.description != "")
                "Output state should not have zero weight".using(output.weight > 0)
            }
            command is Commands.Redeem -> {
                val input: AppleStamp = tx.inputsOfType<AppleStamp>().get(0)
                requireThat {
                    "This transaction should have two input states".using(tx.inputs.size == 2)
                    "This transaction should have one output state".using(tx.outputs.size == 1)
                    "Input state issuer should be same as output state farm".using(input.issuer == output.farm)
                    "Output state weight should be greater than zero".using(output.weight > 0)
                }
            }
            else -> throw IllegalStateException("Incorrect command type: " + command)
        }
    }

    interface Commands : CommandData {
        class PackBasket : Commands
        class Redeem : Commands
    }
}
