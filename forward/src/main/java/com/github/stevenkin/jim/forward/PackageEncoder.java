package com.github.stevenkin.jim.forward;

import com.github.stevenkin.serialize.Frame;
import com.github.stevenkin.serialize.Package;
import com.github.stevenkin.serialize.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class PackageEncoder extends MessageToMessageEncoder<Frame> {
    private Serialization<Frame> serialization;

    public PackageEncoder(Serialization serialization) {
        this.serialization = serialization;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Frame pkg, List<Object> out) throws Exception {
        ByteBuf buffer = ctx.alloc().buffer();
        ByteBuf buf = serialization.serialize(pkg, buffer);
        out.add(buf);
    }
}
