package com.template.webserver

import net.corda.core.node.NodeInfo
import org.springframework.stereotype.Component

@Component
class NetworkService(val rpc: NodeRPCConnection) {

    fun getNodeInfo() : NodeInfo {
        return rpc.proxy.nodeInfo()
    }

    fun getNetworkMap(): List<NodeInfo> {
        return rpc.proxy.networkMapSnapshot()
    }
}