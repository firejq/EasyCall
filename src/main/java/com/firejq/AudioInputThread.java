package com.firejq;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.CanvasFrame;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author <a href="mailto:firejq@outlook.com">firejq</a>
 */
public class AudioInputThread extends Thread {

	private InetAddress remoteAddress;

	public AudioInputThread(String ipAddress) {
		try {
			this.remoteAddress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("音频输入线程（听对方）启动");

		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		try (DatagramSocket dSocket = new DatagramSocket(Config.INPUT_PORT)) {


			while (true) { //窗口是否关闭
				byte [] buf = new byte[65507];
				DatagramPacket dPacket = new DatagramPacket(buf, 65507);
				dSocket.receive(dPacket);

				if (!dPacket.getAddress().equals(this.remoteAddress)) {
					continue;
				}

				// 设置音频编码器
				AudioFormat audioFormat = new AudioFormat(44100.0F,
														  16,
														  2,
														  true,
														  false);
				// 通过设置好的音频编解码器获取数据线信息
				DataLine.Info dataLineInfo
						= new DataLine.Info(TargetDataLine.class, audioFormat);
				TargetDataLine line = (TargetDataLine)
						AudioSystem.getLine(dataLineInfo);
				line.open(audioFormat);
				// 读取
				int nBytesRead = line.read(dPacket.getData(),
										   0,
										   line.available());
				// 因为我们设置的是16位音频格式,所以需要将byte[]转成short[]
				int nSamplesRead = nBytesRead / 2;
				short[] samples = new short[nSamplesRead];
				ByteBuffer.wrap(dPacket.getData())
						  .order(ByteOrder.LITTLE_ENDIAN)
						  .asShortBuffer()
						  .get(samples);
				// 将short[]包装到ShortBuffer
				ShortBuffer sBuff = ShortBuffer.wrap(samples,
													 0,
													 nSamplesRead);
				// todo play the audio


			}

		} catch (IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
	}
}
