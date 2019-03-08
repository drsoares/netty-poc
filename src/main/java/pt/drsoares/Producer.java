package pt.drsoares;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;


public class Producer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Producer producer = new Producer("localhost", 5556);

        producer.start();
        try {
            while (true) {
                Thread.sleep(100L);
                producer.write("hello" + System.currentTimeMillis());
            }
        } finally {
            producer.close();
        }
    }

    private Channel channel;
    private final String host;
    private final int port;

    EventLoopGroup group = new NioEventLoopGroup();

    public Producer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void start() {
        try {
            ChannelFuture future = newBootstrap();
            channel = future.sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private ChannelFuture newBootstrap() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ProducerInitializer());
        return bootstrap.connect(host, port);
    }

    public void close() throws InterruptedException {
        channel.closeFuture().sync();
        group.shutdownGracefully();
    }

    private void write(String s) {
        channel.writeAndFlush(s + System.lineSeparator());
    }
}
