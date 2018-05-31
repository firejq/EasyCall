package com.firejq;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author <a href="mailto:firejq@outlook.com">firejq</a>
 */
public class Main {
	public static void main(String [] args) throws InterruptedException {
		// 同意chat后执行以下逻辑
		String remoteAddress = "127.0.0.1";

		new VideoInputThread(remoteAddress).start();
		Thread.sleep(1000L); // 本地调试时才需要
		new VideoOutputThread(remoteAddress).start();
		//		new AudioInputThread(remoteAddress).start();
		//		Thread.sleep(1000L); // 本地调试时才需要
		//		new AudioOutputThread(remoteAddress).start();

	}
}
