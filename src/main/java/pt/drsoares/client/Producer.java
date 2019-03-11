package pt.drsoares.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;


public class Producer {

    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        Producer producer = new Producer("localhost", 5556);
        producer.start();

        try {
            while (true) {
                Thread.sleep(1000L);
                producer.write("Message at " + System.currentTimeMillis() + "\n host:" + InetAddress.getLocalHost());
                System.out.println(producer.channel.isOpen());
            }
        } finally {
            producer.close();
        }
    }

    private volatile Channel channel;
    private final String host;
    private final int port;

    EventLoopGroup group = new NioEventLoopGroup(1);

    public Producer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void start() {
        connect();
    }

    private void connect() {
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ProducerInitializer());

        ChannelFuture future = bootstrap.connect(host, port);
        this.channel = future.channel();
        ChannelFuture closeFuture = channel.closeFuture();
        closeFuture.addListener((ChannelFutureListener) f -> group.schedule(this::connect, 100L, TimeUnit.MILLISECONDS));
    }

    public void close() {
        group.shutdownGracefully();
    }

    private void write(String s) {
        channel.writeAndFlush(s + '\0');
    }
}
