package com.webserver.core;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 *  负责与指定客户端进行HTTP交互
 *  HTTP协议要求与客户端的交互规则采取一问一答的方式。因此，处理客户端交互以3步形式完成:
 *  1:解析请求(一问)
 *  2:处理请求
 *  3:发送响应(一答)
 */
public class ClientHandler implements Runnable{
    private Socket socket;
    public ClientHandler(Socket socket){
        this.socket = socket;
    }

    public void run() {
        try{
            //1解析请求
            HttpRequest request = new HttpRequest(socket);
            HttpResponse response =new HttpResponse(socket);

            //2处理请求
            //首先通过request获取请求中的抽象路径
            String path = request.getUri();
            File file = new File("./webapps"+path);

            //若该资源存在并且是一个文件，则正常响应
            if(file.exists()&&file.isFile()) {
                System.out.println("该资源已存在!");
                response.setEntity(file);
            }else {
                System.out.println("该资源不存在!");
                File notFoundPage = new File("./webapps/root/404.html");
                response.setStatusCode(404);
                response.setStatusReason("NotFound");
                response.setEntity(notFoundPage);
            }
            System.out.println("响应发送完毕!");
            response.flush();

            //3发送响应
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            //处理完毕后与客户端断开连接
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
