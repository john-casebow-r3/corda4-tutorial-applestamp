package com.template.webserver

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import javax.annotation.PostConstruct

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/api") // The paths for HTTP requests are relative to this base path.
class RestApiController(
    val networkService: NetworkService,
    val flowService: FlowService,
    val queryService: QueryService,
    val rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestApiController::class.java)
    }

    private val mapper: ObjectMapper = ObjectMapper()

    @PostConstruct
    fun setup() {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.registerModule(ParameterNamesModule())
    }

    @GetMapping(value = ["/network/nodeinfo"], produces = ["application/json"])
    private fun nodeInfoEndpoint(): String {
        return mapper.writeValueAsString(networkService.getNodeInfo())
    }

    @GetMapping(value = ["/network/networkmap"], produces = ["application/json"])
    private fun networkMapEndpoint(): String {
        return mapper.writeValueAsString(networkService.getNetworkMap())
    }

    @GetMapping(value = ["/flows"], produces = ["application/json"])
    private fun getFlowsEndpoint(): String {
        return mapper.writeValueAsString(flowService.getFlows())
    }

    @PostMapping(value = ["/flows/appleBasket"], consumes = ["application/json"], produces = ["application/json"])
    private fun startBasketFlowEndpoint(@RequestBody basket: AppleBasket): String {
        return mapper.writeValueAsString(flowService.packAppleBasket(basket.description, basket.weight))
    }

    @PostMapping(value = ["/flows/appleStamp"], consumes = ["application/json"], produces = ["application/json"])
    private fun startStampFlowEndpoint(@RequestBody stamp: AppleStamp): String {
        return mapper.writeValueAsString(flowService.issueAppleStamp(stamp.description, stamp.holder, stamp.weight))
    }

    @PostMapping(value = ["/flows/redeemAppleStamp"], consumes = ["application/json"], produces = ["application/json"])
    private fun startStampFlowEndpoint(@RequestBody stampRedemption: AppleStampRedemption): String {
        return mapper.writeValueAsString(flowService.redeemAppleStamp(stampRedemption.holder, stampRedemption.id))
    }

    @GetMapping(value = ["/query/appleBasket"], produces = ["application/json"])
    private fun queryAppleBasketEndpoint(): String {
        return mapper.writeValueAsString(queryService.queryAppleBasket())
    }

    @GetMapping(value = ["/query/appleStamp"], produces = ["application/json"])
    private fun queryAppleStampEndpoint(): String {
        return mapper.writeValueAsString(queryService.queryAppleStamp())
    }
}