package pt.drsoares.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class Producer {

    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        Producer producer = new Producer("localhost", 5556);
        producer.start();

        try {
            while (true) {
                Thread.sleep(1000L);
                producer.write("Message at " + System.currentTimeMillis() + "\n host:" + InetAddress.getLocalHost());
            }
        } finally {
            producer.close();
        }
    }

    private Channel channel;
    private final String host;
    private final int port;

    EventLoopGroup group = new NioEventLoopGroup(1);

    public Producer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void start() {
        channel = connect();
    }

    private Channel connect() {
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ProducerInitializer());

            ChannelFuture future = bootstrap.connect(host, port);
            return future.sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws InterruptedException {
        group.shutdownGracefully();
    }

    private void write(String s) {
        channel.writeAndFlush(s + '\0');
    }
}
