package com.github.stevenkin.jim.forward;

import com.github.stevenkin.serialize.Serialization;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ForwardServer {
    private int port;

    private final ForwardServerHandler forwardServerHandler;

    private final Serialization serialization;

    private ChannelFuture channelFuture;

    private boolean registered = false;

    public ForwardServer(int port, Serialization serialization) {
        this.port = port;
        this.forwardServerHandler = new ForwardServerHandler();
        this.serialization = serialization;
    }

    public void registerProcessor(ForwardProcessor processor) {
        forwardServerHandler.registerProcessor(processor);
        registered = true;
    }

    public void open() {
        if (!registered) {
            throw new RuntimeException("must register forwardProcessor");
        }
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FrameDecoder());
                        pipeline.addLast(new FrameEncoder());
                        pipeline.addLast(new PackageDecoder(serialization));
                        pipeline.addLast(new PackageEncoder(serialization));
                        pipeline.addLast(forwardServerHandler);
                    }
                });

        try {
            channelFuture = bootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        channelFuture.addListener((future) -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }

        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            boss.shutdownGracefully().syncUninterruptibly();
            worker.shutdownGracefully().syncUninterruptibly();
        }));
    }

    public void close() {
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
    }

}
