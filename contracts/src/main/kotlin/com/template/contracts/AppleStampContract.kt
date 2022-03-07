package com.template.contracts

import com.template.states.AppleStamp
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class AppleStampContract : Contract {

    companion object {
        const val ID = "com.template.contracts.AppleStampContract"
    }

    override fun verify(tx: LedgerTransaction) {
        val command: CommandData = tx.commands.get(0).value

        when {
            command is Commands.Issue -> {
                val output: AppleStamp = tx.outputsOfType<AppleStamp>().get(0)
                requireThat {
                    "This transaction should have no input states".using(tx.inputs.size == 0)
                    "This transaction should only output one AppleStamp state".using(tx.outputs.size == 1)
                    "This transactions should have a clear description of the goods being issued".using(output.stampDesc != "")
                }
            }
            command is BasketOfApplesContract.Commands.Redeem -> {
                // NOP
            }
//            else -> throw IllegalStateException("Incorrect command type: " + command)
        }
    }

    interface Commands : CommandData {
        class Issue : Commands
    }
}
