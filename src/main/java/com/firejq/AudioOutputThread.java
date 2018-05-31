package com.firejq;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;

import javax.sound.sampled.*;
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
public class AudioOutputThread extends Thread {

	private InetAddress remoteAddress;

	public AudioOutputThread(String ipAddress) {
		try {
			this.remoteAddress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("音频输出线程（给对方听）启动");

		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		try (DatagramSocket dSocket = new DatagramSocket()) {

			while (true) {
				// 设置音频编码器
				AudioFormat audioFormat = new AudioFormat(44100.0F,
														  16,
														  2,
														  true,
														  false);
				// 通过AudioSystem获取本地音频混合器信息
				Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
				// 通过AudioSystem获取本地音频混合器
				Mixer mixer = AudioSystem.getMixer(minfoSet[4]);
				// 通过设置好的音频编解码器获取数据线信息
				DataLine.Info dataLineInfo
						= new DataLine.Info(TargetDataLine.class, audioFormat);
				// 打开并开始捕获音频
				TargetDataLine line = (TargetDataLine)
						AudioSystem.getLine(dataLineInfo);
				line.open(audioFormat);
				line.start();
				// 获得当前音频采样率
				int sampleRate = (int) audioFormat.getSampleRate();
				// 获取当前音频通道数量
				int numChannels = audioFormat.getChannels();
				// 初始化音频缓冲区 (size 是音频采样率 * 通道数)
				byte [] audioBytes = new byte[sampleRate * numChannels];

				DatagramPacket dPacket = new DatagramPacket(audioBytes,
															audioBytes.length,
															remoteAddress,
															Config.DEFAULT_PORT);
				// todo The message is larger than the maximum supported by the underlying transport: Datagram send failed
				dSocket.send(dPacket);
				Thread.sleep(50); // 50毫秒刷新一次音频
			}

		} catch (IOException | InterruptedException
				| LineUnavailableException e) {
			e.printStackTrace();
		}
	}
}
