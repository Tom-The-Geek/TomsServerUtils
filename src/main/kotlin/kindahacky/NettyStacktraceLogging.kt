package me.geek.tom.serverutils.kindahacky

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import me.geek.tom.serverutils.TomsServerUtils

class NettyStacktraceLogging : ChannelInboundHandlerAdapter() {
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        TomsServerUtils.LOGGER.error("Error on netty thread!", cause)
        ctx.fireExceptionCaught(cause)
    }
}
