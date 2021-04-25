package ru.ntr.nioserver;

import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Log4j
public class NIOServer {

    private final int port = Integer.parseInt(System.getenv("PORT"));
    private final String serverDir = System.getenv("SERVER_DIR");
    private ConcurrentMap<SocketChannel, CommandExecutor> executors = new ConcurrentHashMap<>();
    private ServerSocketChannel serverChannel;
    private Selector selector;

    public NIOServer() {

        try {
            serverChannel = ServerSocketChannel.open();
            selector = Selector.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            log.info("Server started on port " + port + ".");

            while (serverChannel.isOpen()) {
                selector.select();
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
            log.info("Server was broken", e);
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

            String cmd = s.toString();

            try {
                CommandExecutor executor = executors.get(channel);

                channel.write(
                        ByteBuffer.wrap(
                                executor.execute(cmd).getBytes(StandardCharsets.UTF_8))
                );
            } catch (ClosedChannelException e) {
                executors.remove(channel);
                log.error("Chanel was closed :", e);
            }
        }

            /*Set<SelectionKey> keys = selector.keys();

            for (SelectionKey selectionKey : keys) {
                if (selectionKey.channel() instanceof SocketChannel &&
                        selectionKey.isValid()) {
                    SocketChannel responseChannel = (SocketChannel) selectionKey.channel();
                    responseChannel.write(ByteBuffer.wrap(
                            executor.execute(cmd).getBytes(StandardCharsets.UTF_8)
                    ));
                }
            }*/ catch (Exception e) {
            log.error("Server terminated due error :", e);
        }

    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        executors.putIfAbsent(channel, new CommandExecutor(new FileManagerImpl(serverDir)));
        log.info("Client " + channel.getRemoteAddress() + "accepted.");
    }
}