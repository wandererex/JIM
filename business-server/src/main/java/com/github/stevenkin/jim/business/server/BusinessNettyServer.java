package com.github.stevenkin.jim.business.server;

import com.github.stevenkin.jim.business.server.config.BusinessServerProperties;
import com.github.stevenkin.serialize.Serialization;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class BusinessNettyServer implements SmartInitializingSingleton, ApplicationListener<ContextClosedEvent> {
    private final BusinessServerProperties config;

    private final Serialization serialization;

    private ChannelFuture channelFuture;

    public BusinessNettyServer(BusinessServerProperties config, Serialization serialization) {
        this.config = config;
        this.serialization = serialization;
    }

    public void init() {
        EventLoopGroup boss = new NioEventLoopGroup(this.config.getBossLoopGroupThreads());
        EventLoopGroup worker = new NioEventLoopGroup(this.config.getWorkerLoopGroupThreads());
        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.config.getConnectTimeoutMillis())
                .option(ChannelOption.SO_BACKLOG, this.config.getSoBacklog())
                .childOption(ChannelOption.WRITE_SPIN_COUNT, this.config.getWriteSpinCount())
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(this.config.getWriteBufferLowWaterMark(), this.config.getWriteBufferHighWaterMark()))
                .childOption(ChannelOption.TCP_NODELAY, this.config.isTcpNodelay())
                .childOption(ChannelOption.SO_KEEPALIVE, this.config.isSoKeepalive())
                .childOption(ChannelOption.SO_LINGER, this.config.getSoLinger())
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, this.config.isAllowHalfClosure())
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                    }
                });

        if (this.config.getSoRcvbuf() != -1) {
            bootstrap.childOption(ChannelOption.SO_RCVBUF, this.config.getSoRcvbuf());
        }

        if (this.config.getSoSndbuf() != -1) {
            bootstrap.childOption(ChannelOption.SO_SNDBUF, this.config.getSoSndbuf());
        }

        if ("0.0.0.0".equals(this.config.getHost())) {
            channelFuture = bootstrap.bind(this.config.getPort());
        } else {
            try {
                channelFuture = bootstrap.bind(new InetSocketAddress(InetAddress.getByName(this.config.getHost()), this.config.getPort()));
            } catch (UnknownHostException var6) {
                channelFuture = bootstrap.bind(this.config.getHost(), this.config.getPort());
                var6.printStackTrace();
            }
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

    @Override
    public void afterSingletonsInstantiated() {
        init();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
    }
}
