package cn.edkso.chapter8_socket_client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 通过time.nist.gov对话构造一个Date
 */
public class Daytime {

    static public Date getDateFromNetwork(){
        Socket socket = null;
        try {
            socket = new Socket("time.nist.gov", 13);
            socket.setSoTimeout(15000);
            InputStream in = socket.getInputStream();
            StringBuilder time = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(in, "ASCII");

            for (int c = reader.read(); c != -1; c = reader.read()) {
                time.append((char)c);
            }
            return paeseDate(time.toString());
        } catch (IOException e) {
            return null;
        } finally {
            if (socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static private Date paeseDate(String s) {
        String[] split = s.split(" ");
        String dateTime = split[1] + " " + split[2] + " UTC";
        try {
            return new SimpleDateFormat("yy-MM-dd hh:mm:ss").parse(dateTime);
        } catch (ParseException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(Daytime.getDateFromNetwork());
    }
}
