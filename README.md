# EasyCall

:tv: Easy Peer-to-Peer chat, video & audio calls in the LAN.

<!-- todo: 解决 TCP 粘包、拆包问题 https://blog.insanecoder.top/tcp-packet-splice-and-split-issue/  -->

## 1. Feature

- 使用 [javacv](https://github.com/bytedeco/javacv) 抓取摄像头画面和音频数据
- 基于 UDP 去中心化字节流传输，不经过中间服务器
- 使用广播方式获取在线客户端
- 使用多线程非阻塞 IO 模型

## 2. Usage

- 构建
  ```
  gradle build
  ```
- 列出局域网中所有可连接的 EasyCall 客户端
  ```
  java -jar EasyCall.jar list
  ```
- 启动 EasyCall 客户端，保持在线并等待连接请求
  ```
  java -jar EasyCall.jar wait [localPort]
  ```
- 启动 EasyCall 客户端，向指定 EasyCall 客户端发起连接请求
  ```
  java -jar EasyCall.jar request [localPort] remoteIpAddress remotePort
  ```

## 3. License

The EasyCall is under the MIT License.