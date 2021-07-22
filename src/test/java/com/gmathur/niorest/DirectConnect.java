package com.gmathur.niorest;

import com.gmathur.niorest.http.HttpRequest;
import com.gmathur.niorest.http.HttpRequestType;
import com.gmathur.niorest.http.HttpUserAgent;
import com.gmathur.niorest.http.HttpVersion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class DirectConnect {
    public static void main(String[] args) {
        HttpRequest.HttpRequestBuilder builder = HttpRequest.newBuilder();
        HttpRequest request = builder
                .host("localhost").port((short) 8080)
                .endpoint("/health?name=gaurav")
                .useragent(new HttpUserAgent("niorestapp", "0.1alpha"))
                .version(HttpVersion.HTTP_1_1)
                .type(HttpRequestType.HTTP_GET)
                .build();

        byte[] requestBytes = request.toBytes();
        System.out.println(requestBytes);

        try {
            Socket socket = new Socket("localhost", 8080);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(requestBytes);

            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int read;
            while (-1 != (read = inputStream.read(buffer))) {
                String response = new String(buffer, 0, read);
                System.out.print(response);
                System.out.flush();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        System.out.println("Done!");
    }
}
