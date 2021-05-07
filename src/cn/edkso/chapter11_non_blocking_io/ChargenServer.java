package cn.edkso.chapter11_non_blocking_io;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author 十下
 * 一个基于NIO 的chargen服务端
 * 通信数据协议：RFC 864
 */
public class ChargenServer {
    public static int DEFAULT_PORT = 19;

    public static void main(String[] args) {
        int port;
        try {
            port = Integer.parseInt(args[1]);
        }catch (RuntimeException e){
            port = DEFAULT_PORT;
        }
        System.out.println("连接监听端口号：" + port);

        byte[] rotation = new byte[95*2];
        for (byte i = ' '; i < '~'; i++){
            rotation[i-' '] = i;
            rotation[i + 95 - ' '] = i;
        }

        //
        // -------------------------------------------------
        //

        ServerSocketChannel serverChannel;
        Selector selector;
        try {
            //1. 创建ServerSocketChannel对象
            serverChannel = ServerSocketChannel.open();

            //2. 将对象绑定到端口19的服务器Socket；
            //2-1. JDK1.7之前
            /*ServerSocket ss = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress(port);
            ss.bind(address);*/
            //2-2. JDK1.7以及之后
            serverChannel.bind(new InetSocketAddress(19));

            //3. 设置serverChannel为非阻塞模式
            serverChannel.configureBlocking(false);

            //4. 创建Selector对象
            selector = Selector.open();

            //5. 向选择器注册-接收连接事件
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //~~~服务器长时间运行无线while循环
        while (true){
            try {
                //6. 检查是否有可操作的数据（阻塞方法）
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //7. 得到所有包含通道（channel）的keys
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()){
                //7-1. 得到一个包含通道（channel）的key
                SelectionKey key = iterator.next();
                //7-2. 从集合中删除这个建，从而不会处理两次
                iterator.remove();

                //7-3. 处理通道
                try {
                    if (key.isAcceptable()){
                        //7-3-1. 得到一个与客户端连接的client通道
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        System.out.println("与客户端：" + client + "建立连接");
                        client.configureBlocking(false);

                        //7-3-2. 为与客户端连接的通道建立缓冲区
                        ByteBuffer buffer = ByteBuffer.allocate(74);
                        buffer.put(rotation,0,72);
                        buffer.put((byte) '\r');
                        buffer.put((byte) '\n');
                        buffer.flip();

                        //7-3-3. 把与客户端连接的通道注册到选择器，并附加上buffer（缓冲区）
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE,buffer);
                    }else if (key.isWritable()){
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();


                        if (!buffer.hasRemaining()){
                            //用下一行重新填充缓冲区
                            buffer.rewind();
                            //得到上一次的首字符
                            int first = buffer.get();
                            //准备改变缓冲区中的数据
                            buffer.rewind();
                            //寻找rotation中新的首字符位置
                            int position = first - ' ' + 1;
                            //将数据从rotation复制到缓冲区
                            buffer.put(rotation,position,72);
                            //在缓冲区末尾存储一个行分隔符
                            buffer.put((byte) '\r');
                            buffer.put((byte) '\n');
                            //准备缓冲区进行写入
                            buffer.flip();
                        }
                        client.write(buffer);
                    }
                } catch (IOException e) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }

            }
        }
    }
}
