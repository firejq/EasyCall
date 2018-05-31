package com.firejq;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

	/**
	 * 等待连接
	 */
	public void waitFor() {
		System.out.println("Wait for request...");
		try (DatagramSocket inSocket = new DatagramSocket(Config.DEFAULT_PORT)) {
			// 等待连接
			while (true) {
				byte [] respBytes = new byte[65507];
				DatagramPacket respPacket = new DatagramPacket(respBytes,
															   respBytes.length);
				inSocket.receive(respPacket);
				if (new String(respPacket.getData(), "UTF-8")
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
				&& (args[0].equals("wait") || args[0].equals("list"))) {
			if (args.length == 2
					&& Integer.parseInt(args[1]) > 0
					&& Integer.parseInt(args[1]) < 65535) {
				Config.DEFAULT_PORT = Integer.parseInt(args[1]);
			}

			if (args[0].equals("wait")) {
				// eg: app.jar wait 2333
				app.waitFor();
				return;
			}
			if (args[0].equals("list")) {
				// eg: app.jar list 2333

			}
		}

		if (args.length >= 3 && args[0].equals("request")) {
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
