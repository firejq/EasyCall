package com.firejq;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
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

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author <a href="mailto:firejq@outlook.com">firejq</a>
 */
public class VideoInputThread extends Thread {

	private InetAddress remoteAddress;

	public VideoInputThread(String ipAddress) {
		try {
			this.remoteAddress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("视频输入线程（看对方）启动");

		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		try (DatagramSocket dSocket = new DatagramSocket(Config.INPUT_PORT)) {

			// 新建一个窗口，显示对方摄像头的画面
			CanvasFrame canvas = new CanvasFrame("对方");
			canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			canvas.setAlwaysOnTop(true);

			while (canvas.isDisplayable()) { //窗口是否关闭
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

			canvas.dispose();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
