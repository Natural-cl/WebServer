package com.webserver.core;

import com.webserver.http.EmptyRequestException;
import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 负责与指定客户端进行HTTP交互
 * HTTP协议要求与客户端的交互规则采取一问一答的方式。因此，处理客户端交互以3步形式完成:
 * 1:解析请求(一问)
 * 2:处理请求
 * 3:发送响应(一答)
 */
public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            //1解析请求
            HttpRequest request = new HttpRequest(socket);
            HttpResponse response = new HttpResponse(socket);

            //2处理请求
            //首先通过request获取请求中的抽象路径
            String path = request.getUri();
            File file = new File("./webapps" + path);

            //若该资源存在并且是一个文件，则正常响应
            if (file.exists() && file.isFile()) {
                System.out.println("该资源已找到!" + file.getName());
                String line = file.getName();
                Map<String, String> map = new HashMap<>();
                map.put("html", "text/html");
                map.put("css", "text/css");
                map.put("js", "application/javascript");
                map.put("png", "image/png");
                map.put("gif", "image/gif");
                map.put("jpg", "image/jpeg");
                String ext = line.substring(line.lastIndexOf(".") + 1);
                response.putHeader("Content-Type", map.get(ext));
                response.putHeader("Content-Length", file.length() + "");
                response.setEntity(file);
            } else {
                System.out.println("该资源不存在!");
                File notFoundPage = new File("./webapps/root/404.html");
                response.setStatusCode(404);
                response.setStatusReason("NotFound");
                response.putHeader("Content-Type", "text/html");
                response.putHeader("Content-Length", notFoundPage.length() + "");
                response.setEntity(notFoundPage);
            }
            System.out.println("响应发送完毕!");
            response.flush();

            //统一设置其他响应头
            response.putHeader("Server", "WebServer");//Server

            //3发送响应
        } catch (EmptyRequestException e) {
            //什么都不用做,上面抛出该异常就是为了忽略处理和响应操作
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //处理完毕后与客户端断开连接
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
