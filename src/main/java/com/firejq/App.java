package com.firejq;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Scanner;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author <a href="mailto:firejq@outlook.com">firejq</a>
 */
public class App {

	private static final String REQ_STRING = "connect req";

	private static final String AGREE_STRING = "ok";

	private static final String REJECT_STRING = "no";

	private static final String ONLINE_STRING = "online";

	private static final String BROCAST_ADDRESS = "228.5.6.7";

	/**
	 * 等待连接
	 */
	public void waitFor() {
		System.out.println("Wait for request...");

		// 广播在线信息
		new Thread(() -> {
			try (MulticastSocket mutiSocket
						 = new MulticastSocket(Config.DEFAULT_PORT)) {

				InetAddress group = InetAddress.getByName(BROCAST_ADDRESS);
				mutiSocket.setTimeToLive(1); // 设置广播组地址
				mutiSocket.joinGroup(group); // 加入组播地址
				mutiSocket.setLoopbackMode(false); // 设置发送的数据报会回送到自身

				byte [] buffer = ONLINE_STRING.getBytes("UTF-8");
				DatagramPacket packet
						= new DatagramPacket(buffer,
											 buffer.length,
											 group,
											 Config.DEFAULT_PORT);
				while (true) {
					mutiSocket.send(packet);
					Thread.sleep(1000L);
				}
				// mutiSocket.leaveGroup(group);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}).start();

		try (DatagramSocket inSocket = new DatagramSocket(Config.DEFAULT_PORT)) {
			// 等待连接
			while (true) {
				byte [] respBytes = new byte[65507];
				DatagramPacket respPacket = new DatagramPacket(respBytes,
															   respBytes.length);
				inSocket.receive(respPacket);
				if (new String(respPacket.getData(), "UTF-8")
						.trim()
						.equals(REQ_STRING)) {
					String remoteAddress = respPacket.getAddress()
													 .getHostAddress();
					int port = respPacket.getPort();
					System.out.println(
							remoteAddress + " want to connect with you, y/n");
					Scanner scanner = new Scanner(System.in);
					switch (scanner.next()) {
						case "y": {
							this.agreeTo(remoteAddress, port);
							this.callConnect(remoteAddress, port);
							return;
						}
						default: {
							this.rejectTo(remoteAddress, port);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 向指定地址发起Call请求
	 * @param remoteAddress
	 */
	public void requestFor(String remoteAddress, int port) {
		System.out.println("Make a request for " + remoteAddress);

		try (DatagramSocket outSocket = new DatagramSocket();
			 DatagramSocket inSocket = new DatagramSocket(Config.DEFAULT_PORT)) {

			InetAddress remoteIp = InetAddress.getByName(remoteAddress);
			// 发送请求
			byte [] reqBytes = REQ_STRING.getBytes("UTF-8");
			DatagramPacket reqPacket
					= new DatagramPacket(reqBytes, reqBytes.length,
										 remoteIp, port);
			outSocket.send(reqPacket);

			// 获取答复
			while (true) {
				byte [] respBytes = new byte[65507];
				DatagramPacket respPacket = new DatagramPacket(respBytes,
															   respBytes.length);
				inSocket.receive(respPacket);
				if (respPacket.getAddress().equals(remoteIp)
						&& reqPacket.getPort() == port) {
					switch (new String(respPacket.getData(),
									   "UTF-8")) {
						case AGREE_STRING: {
							System.out.println(
									remoteAddress + " receive your call request.");
							this.callConnect(remoteAddress, port);
							return;
						}
						case REJECT_STRING: {
							System.out.println(
									remoteAddress + " reject your call request.");
							return;
						}
						default: {
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 列出待连接的所有App客户端信息
	 */
	public void list() {
		HashSet<SocketAddress> clientSet = new HashSet<>();
		try (MulticastSocket mutiSocket
					 = new MulticastSocket(Config.DEFAULT_PORT)) {
			InetAddress group = InetAddress.getByName(BROCAST_ADDRESS);
			mutiSocket.setTimeToLive(1); // 设置广播组地址
			mutiSocket.joinGroup(group); // 加入组播地址

			while (true) {
				byte[] buffer = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				mutiSocket.receive(packet);
				if (new String(packet.getData(), "UTF-8")
						.trim()
						.equals(ONLINE_STRING)) {
					InetSocketAddress socketAddress
							= new InetSocketAddress(packet.getAddress(),
													packet.getPort());
					if (clientSet.add(socketAddress)) {
						System.out.println(socketAddress.getAddress()
														.getHostAddress() + " "
												   + socketAddress.getPort());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 发送AGREE标识到指定地址
	 * @param remoteAddress
	 */
	private void agreeTo(String remoteAddress, int port) {
		this.sendStringTo(AGREE_STRING, remoteAddress, port);
	}

	/**
	 * 发送REJECT标识到指定地址
	 * @param remoteAddress
	 */
	private void rejectTo(String remoteAddress, int port) {
		this.sendStringTo(REJECT_STRING, remoteAddress, port);
	}

	/**
	 * 发送指定字符串到指定地址
	 * @param content
	 * @param remoteAddress
	 */
	private void sendStringTo(String content, String remoteAddress, int port) {
		try (DatagramSocket outSocket = new DatagramSocket()) {
			InetAddress inetAddress = InetAddress.getByName(remoteAddress);
			byte [] data = content.getBytes("UTF-8");
			DatagramPacket dPacket = new DatagramPacket(data,
														data.length,
														inetAddress,
														port);
			outSocket.send(dPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 与指定IP地址的客户端建立连接
	 * @param remoteAddress
	 */
	private void callConnect(String remoteAddress, int remotePort) {
		try {
			new VideoInputThread(remoteAddress, remotePort).start();
			Thread.sleep(1000L); // 本地调试时才需要
			new VideoOutputThread(remoteAddress, remotePort).start();
			//		new AudioInputThread(remoteAddress).start();
			//		Thread.sleep(1000L); // 本地调试时才需要
			//		new AudioOutputThread(remoteAddress).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * App main method
	 * @param args
	 */
	public static void main(String [] args) {
		System.out.println("Welcom to EasyCall");
		App app = new App();

		if ((args.length == 1 || args.length == 2)
				&& (args[0].trim().equals("wait")
				|| args[0].trim().equals("list"))) {
			if (args.length == 2
					&& Integer.parseInt(args[1]) > 0
					&& Integer.parseInt(args[1]) < 65535) {
				Config.DEFAULT_PORT = Integer.parseInt(args[1]);
			}

			if (args[0].trim().equals("wait")) {
				// eg: app.jar wait 2333
				app.waitFor();
				return;
			}
			if (args[0].trim().equals("list")) {
				// eg: app.jar list 2333
				app.list();
				return;
			}
		}

		if (args.length >= 3 && args[0].trim().equals("request")) {
			// eg: app.jar request 2333 192.168.1.100 6666
			if (args.length == 3) {
				app.requestFor(args[1], Integer.parseInt(args[2]));
				return;
			}

			if (args.length == 4
					&& Integer.parseInt(args[1]) > 0
					&& Integer.parseInt(args[1]) < 65535) {
				Config.DEFAULT_PORT = Integer.parseInt(args[1]);
				app.requestFor(args[2], Integer.parseInt(args[3]));
				return;
			}
		}
	}
}
