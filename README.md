# EasyCall

:tv: Easy Peer-to-Peer chat, video & audio calls in the LAN.

<!-- 解决 TCP 粘包、拆包问题 https://blog.insanecoder.top/tcp-packet-splice-and-split-issue/  -->

## 1. Feature

## 2. Usage

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