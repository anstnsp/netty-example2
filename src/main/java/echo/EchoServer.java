package echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class EchoServer {
    private static final Logger logger = LoggerFactory.getLogger(EchoServer.class);
    private final int port;
    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            logger.error("에러다");
        }
        int port =Integer.parseInt(args[0]);
        new EchoServer(port).start();
    }

    public void start() throws Exception {
        final EchoServerHandler serverHandler = new EchoServerHandler();
        EventLoopGroup group = new NioEventLoopGroup(); // EventLoopGroup을생성
        try {
            ServerBootstrap b = new ServerBootstrap(); //ServerBootStrap을 생성
            b.group(group)
            .channel(NioServerSocketChannel.class) //NIO 전송 채널을 이용하도록 설정
            .localAddress(new InetSocketAddress(port)) //지정된 포트를 이용해 소켓 주소를 설정
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception { //EchoServerHandler하나를 채널의 ChannelPipeline으로 추가
                    ch.pipeline().addLast(serverHandler); //EchoServerHandler는 @Sharable이므로 동일한 항목을 이용할 수 있음.
                }
            });
            ChannelFuture future = b.bind().sync(); //서버를 비동기식으로 바인딩, sync()는 바인딩이 완료되기를 대기
            future.channel().closeFuture().sync(); //채널의 CloseFuture를얻고 완료될 때까지 현재스레드를 블로킹
        } finally {
            group.shutdownGracefully().sync(); //EventLoopgroup을종료하고 모든 리소스를 해제
        }
    }
}
