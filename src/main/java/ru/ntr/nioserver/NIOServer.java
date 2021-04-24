package ru.ntr.nioserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {

    private ServerSocketChannel serverChannel;
    private Selector selector;

    public NIOServer() {

        try {
            serverChannel = ServerSocketChannel.open();
            selector = Selector.open();
            serverChannel.bind(new InetSocketAddress(8189));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server started...");

            while (serverChannel.isOpen()) {
                selector.select(); // block

                System.out.println("Keys selected!");

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    keyIterator.remove();
                }
            }
        } catch (Exception e) {
            System.err.println("Server was broken");
        }
    }

    public static void main(String[] args) throws IOException {
        new NIOServer();
    }

    private void handleRead(SelectionKey key) {
        try {

            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(256);

            int read;

            StringBuilder s = new StringBuilder();
            while (true) {

                read = channel.read(buffer);

                if (read == -1) {
                    channel.close();
                    break;
                }

                if (read == 0) {
                    break;
                }

                buffer.flip();

                while (buffer.hasRemaining()) {
                    s.append((char) buffer.get());
                }

                buffer.clear();
            }
            String message = "from server: " + s.toString();

            System.out.println("received: " + message);

            Set<SelectionKey> keys = selector.keys();

            for (SelectionKey selectionKey : keys) {
                if (selectionKey.channel() instanceof SocketChannel &&
                        selectionKey.isValid()) {
                    SocketChannel responseChannel = (SocketChannel) selectionKey.channel();
                    responseChannel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                }
            }
        } catch (Exception e) {
            System.err.println("Connection was broken");
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted!");
    }
}