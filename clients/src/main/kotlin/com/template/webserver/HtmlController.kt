package com.template.webserver

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class HtmlController (val flowService: FlowService, val queryService: QueryService){

    @GetMapping("/")
    fun home(model: Model): String {
        return "redirect:/store"
    }

    @GetMapping("/store")
    fun getStore(model: Model): String {
        val stampList: List<AppleStamp> = queryService.queryAppleStamp().map{ it ->
            AppleStamp(
                it.state.data.stampDesc,
                it.state.data.issuer.toString(),
                it.state.data.holder.toString(),
                it.state.data.weight,
                it.state.data.linearId.toString()
            )
        }
        val basketList: List<AppleBasket> = queryService.queryAppleBasket().map{ it ->
            AppleBasket(
                it.state.data.description,
                it.state.data.owner.toString(),
                it.state.data.weight
            )
        }

        model.addAttribute("title", "Apple Store")
        model.addAttribute("stamps", stampList)
        model.addAttribute("baskets", basketList)

        return "store"
    }

    @PostMapping("/stamps")
    fun postStamps(model: Model, @ModelAttribute("stamp") appleStamp: AppleStamp): String {
        flowService.issueAppleStamp(appleStamp.description, appleStamp.holder, appleStamp.weight)
        return "redirect:/store"
    }

    @PostMapping("/baskets")
    fun postStamps(model: Model, @ModelAttribute("basket") appleBasket: AppleBasket): String {
        flowService.packAppleBasket(appleBasket.description, appleBasket.weight)
        return "redirect:/store"
    }

    @PostMapping("/redeem")
    fun postStamps(model: Model, @ModelAttribute("stamp") appleStampRedemption: AppleStampRedemption): String {
        flowService.redeemAppleStamp(appleStampRedemption.holder, appleStampRedemption.id)
        return "redirect:/store"
    }

}