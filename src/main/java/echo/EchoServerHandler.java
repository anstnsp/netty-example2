package echo;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * SimpleChannelInboundHandler와 ChannelInboundHandler 비교
 * 클라이언트에서는 EchoServerHandler에서 이용한ChannelInboundHandlerAdapter가 아니라 SimpleChannelInboundHandler를 이용했다.
 * 그 이유는 무엇일까? 이것은 비니지스 논리가 메세지를 처리하는 방법, 그리고 네티가 리소스를관리하는 방법의두 요소 간 상호작용과 관계있다.
 *
 * 클라이언트에서 channelRead0()이 완료된 시점에는 들어오는 메세지가 이미확보됐고 이용도 끝난상태다.
 * 따라서 메서드가 반활될 때 SimpleChannelInboundHandler는 메세지가 들어있는 ByteBuf에 대한 메모리참조를 해제한다.
 * 반면 EchoServerHandler에서는 아직 들어오는 메세지를 발신자에게 반향 출력해야하며, channelRead()가 반환될 때까지 비동기식인 write()
 * 작업이 완료되지 않았을 수 있다. 따라서EchoServerHandler는 이 시점까지 메세지를 해제하지 않는 ChannelInboundHandlerAdapter를 확장한다.
 *
 * 메세지는 EchoServerHandler의 channelReadComplete()에서 writeAndFlush()가 호출될 때 헤제된다.
 */

/**
 * 핸들러는 비지니스논리를 구현하는 곳.
 * 어플리케이션은 ChannelHandler를 구현하거나확장해 이벤트수명주기를 후크하고 커스텀 어플리케이션 논리를 제공한다.
 */
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 메세지가 들어올때마다 호출됨.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        logger.info("Server received: "+ in.toString(StandardCharsets.UTF_8));
        ctx.write(in);
    }

    /**
     * channelRead()의 마지막 호출에서 현재 일괄처리의 마지막메세지를 처리했음을 핸들러에 통보한다.
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE); //대기중인 메세지를 원격 피어로 플러시하고 채널을닫음.
    }

    /**
     * 체인의 어디서도 excectionCaught()이 구현되지 않은경우 수신된 예외가 ChannelPipeline의끝까지 이동한후 로깅된다.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close(); //채널을 닫음.
    }
}
