package com.firejq;

import org.bytedeco.javacv.CanvasFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author <a href="mailto:firejq@outlook.com">firejq</a>
 */
public class ChatInputThread extends Thread {

	private InetAddress remoteAddress;

	public ChatInputThread(String ipAddress) {
		try {
			this.remoteAddress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("输入线程（看对方）启动");
		try (DatagramSocket dSocket = new DatagramSocket(Config.INPUT_PORT)) {

			CanvasFrame canvas = new CanvasFrame("对方");//新建一个窗口
			canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			canvas.setAlwaysOnTop(true);

			while (true) {
				byte [] buf = new byte[65507];
				DatagramPacket dPacket = new DatagramPacket(buf, 65507);
				dSocket.receive(dPacket);

				if (!dPacket.getAddress().equals(this.remoteAddress)) {
					continue;
				}
				ByteArrayInputStream bin = new ByteArrayInputStream(
						dPacket.getData());
				BufferedImage image = ImageIO.read(bin);
				canvas.showImage(image);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
