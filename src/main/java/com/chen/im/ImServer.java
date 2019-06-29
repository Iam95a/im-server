package com.chen.im;

import com.chen.im.common.protobuf.RequestMessageProto;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.InetSocketAddress;

public class ImServer {

    public static void main(String[] args) throws Exception {
        // create and configure beans
        ApplicationContext context = new ClassPathXmlApplicationContext("services.xml");
        AppContext.init(context);
        NioEventLoopGroup group = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(8888))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        //protobuf的顺序还是蛮重要的  具体为什么需要查一下资料
                        // 实体类传输数据，protobuf序列化
                        sc.pipeline().addLast("decoder",
                                new ProtobufDecoder(RequestMessageProto.RequestMessage.getDefaultInstance()));
                        sc.pipeline().addLast("encoder",
                                new ProtobufEncoder());

//                        sc.pipeline().addLast(new ProtobufVarint32FrameDecoder());
//                        sc.pipeline().addLast(new ProtobufDecoder(RequestMessageProto.RequestMessage.getDefaultInstance()));
//                        sc.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
//                        sc.pipeline().addLast(new ProtobufEncoder());
                        sc.pipeline().addLast(new ImHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        ;
        ChannelFuture f = serverBootstrap.bind().sync();

        System.out.println("server started ");
        f.channel().closeFuture().sync();
    }
}
