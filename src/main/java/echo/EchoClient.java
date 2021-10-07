package echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class EchoClient {
    private final String host;
    private final int port;

    private static final Logger logger = LoggerFactory.getLogger(EchoClient.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            logger.error("에러다");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        new EchoClient(host, port).start();
    }
    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap(); //BootStrap을 생성
            b.group(group) //클라이언트 이벤트를 처리할 EventLoopGroup을 지정함. NIO 구현이이용됨.
                .channel(NioSocketChannel.class) //채널유형으로 NIO 전송 유형 중 하나를 지정
            .remoteAddress(new InetSocketAddress(host,port)) //서버의 InetSocketAddress를 설정
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new EchoClientHandler());
                }
            });
            ChannelFuture future = b.connect().sync(); //원격 피어로 연결하고 연결이 완료되기를 기다림.
            future.channel().closeFuture().sync(); //채널이 닫힐 때까지 블로킹함
        } finally {
            group.shutdownGracefully().sync(); //스레드 풀을 종료하고 모든 리소스를 해제함.
        }
    }
}
