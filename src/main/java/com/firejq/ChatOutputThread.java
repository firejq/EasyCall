package com.firejq;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
public class ChatOutputThread extends Thread {

	private InetAddress remoteAddress;

	public ChatOutputThread(String ipAddress) {
		try {
			this.remoteAddress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("输出线程（给对方看）启动");

		try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
			 DatagramSocket dSocket = new DatagramSocket(Config.OUTPUT_PORT)) {

			CanvasFrame canvas = new CanvasFrame("自己");//新建一个窗口
			canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			canvas.setAlwaysOnTop(true);
			grabber.start();   // 开始获取摄像头数据

			while (true) {
				Frame imageFrame = grabber.grab();
				canvas.showImage(imageFrame);
				Java2DFrameConverter converter = new Java2DFrameConverter();
				double g = grabber.getGamma();
				double inverseGamma = g == 0.0 ? 1.0 : 1.0/g;
				Image image;
				image = converter.getBufferedImage(imageFrame,
												   inverseGamma,
												   false,
												   null);

				byte [] content;
				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					ImageIO.write((BufferedImage) image, "jpg", out);
					content = out.toByteArray();
				}

				DatagramPacket dPacket
						= new DatagramPacket(content,
											 content.length,
											 remoteAddress,
											 Config.INPUT_PORT);
				dSocket.send(dPacket);
				Thread.sleep(50);//50毫秒刷新一次图像
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
