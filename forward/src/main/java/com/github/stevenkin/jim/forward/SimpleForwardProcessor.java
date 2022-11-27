package com.github.stevenkin.jim.forward;

import com.github.stevenkin.serialize.Frame;
import com.github.stevenkin.serialize.Package;
import io.netty.channel.ChannelHandlerContext;

public class SimpleForwardProcessor implements ForwardProcessor{
    @Override
    public void process(ChannelHandlerContext ctx, Frame msg) throws Exception {
        System.out.println(msg);
    }
}
