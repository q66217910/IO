package com.mcw;

import com.mcw.hander.McwHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 启动类
 */
public class Application {

    public static void main(String[] args) {

        //boss
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);

        //worker
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);


        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(80)
                .childHandler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new IdleStateHandler(10, 0, 0));
                        //  decode
                        pipeline.addLast(new HttpContentDecompressor());
                        // encode
                        pipeline.addLast(new HttpContentCompressor());
                        // handler
                        pipeline.addLast(new McwHandler());
                    }
                });

        try {
            ChannelFuture sync = serverBootstrap.bind().sync();
            sync.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
