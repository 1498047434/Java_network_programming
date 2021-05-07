package cn.edkso.chapter11_non_blocking_io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author 十下
 * 一个基于NIO 的chargen客户端，与服务端rama.poly.edu:19进行通信(连接不上)
 * 与服务端localhost:19进行通信(用自己的)
 * 通信数据协议：RFC 864
 */
public class ChargenClient {
    public static int DEFAULT_PORT = 19;

    public static void main(String[] args) {
        if (args.length == 0){
            System.out.println("请输入服务端的 host [prot]");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(args[1]);
        }catch (RuntimeException e){
            port = DEFAULT_PORT;
        }


        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(args[0], port);
            SocketChannel client = SocketChannel.open(inetSocketAddress);

            ByteBuffer byteBuffer = ByteBuffer.allocate(74);
            WritableByteChannel out = Channels.newChannel(System.out);

            while (client.read(byteBuffer) != -1){
                byteBuffer.flip();
                out.write(byteBuffer);
                byteBuffer.clear();
//                byteBuffer.flip();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
