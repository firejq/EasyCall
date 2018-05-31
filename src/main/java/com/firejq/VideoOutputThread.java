package com.firejq;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
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
public class VideoOutputThread extends Thread {

	private InetAddress remoteAddress;

	public VideoOutputThread(String ipAddress) {
		try {
			this.remoteAddress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("输出线程（给对方看）启动");

		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
			 DatagramSocket dSocket = new DatagramSocket()) {

			CanvasFrame canvas = new CanvasFrame("自己");//新建一个窗口
			canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			canvas.setAlwaysOnTop(true);

			/* 尝试开启摄像头 */
			int tryOpen = 0; // 摄像头开启状态
			try {
				grabber.start(); // 开始获取摄像头数据
				tryOpen += 1;
			} catch (org.bytedeco.javacv.FrameGrabber.Exception e1) {
				try {
					grabber.restart();
					tryOpen += 1;
				} catch (org.bytedeco.javacv.FrameGrabber.Exception e2) {
					tryOpen -= 1;
					grabber.stop();
				}
			}
			if (tryOpen < 1) {
				System.err.println("摄像头开启失败！");
				return;
			}

			while (canvas.isDisplayable()) {
				Frame frame = grabber.grab();
				if (frame.image == null) {
					continue;
				}
				canvas.showImage(frame);

				double g = grabber.getGamma();
				double inverseGamma = g == 0.0 ? 1.0 : 1.0/g;
				BufferedImage bImage = (new Java2DFrameConverter())
						.getBufferedImage(frame,
										  inverseGamma,
										  false,
										  null);
				byte [] content;
				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					ImageIO.write(bImage, "jpg", out);
					content = out.toByteArray();
				}

				DatagramPacket dPacket = new DatagramPacket(content,
															content.length,
															remoteAddress,
															Config.INPUT_PORT);
				dSocket.send(dPacket);
				Thread.sleep(50); // 50毫秒刷新一次图像
			}

			canvas.dispose();
			grabber.stop();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
