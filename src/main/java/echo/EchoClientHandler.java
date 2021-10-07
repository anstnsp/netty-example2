package echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(EchoClientHandler.class);

    //서버에 대한 연결이 만들어지면 호출된다.
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //서버에대해 연결이만들어 지자 마자 아래의 메세지를 서버로 전송
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", StandardCharsets.UTF_8)); //채널활성화 알림을받으면 메세지를 전송
    }

    /**
     * 서버로부터 메시지를 수신하면 호출된다.
     * 주의할점은 서버가 전송한 메세제기ㅏ 여러 청크로 수신될 수 있다는 점이다.
     * 즉,서버가 5바이트를전송할 때 5바이트가 모두 한번에 수신된다는 보장이없다.
     * 데이터가 이렇게 적은경우에도 channelRead0()메서드가 두 번 호출될 수 있다.
     * 즉, 3바이트를 저장하는 ByteBuf 하나와 2바이트를저장하는 ByteBuf 하나로 한 번씩, 두 번 호출될 수 있다.
     * 다만 TCP는 스트림 기반 프로토콜이므로 서버가 보낸 순서대로 바이트를 수신할 수 있게 보장한다.
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            logger.info("Client received: "+ msg.toString(StandardCharsets.UTF_8));
    }

    //처리중 예외가 발생하면 호출된다.
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause); //예외 시 오류를 로깅하고 채널을 닫음
        ctx.close();
    }
}
