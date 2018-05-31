package com.firejq;

import org.junit.Test;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author <a href="mailto:firejq@outlook.com">firejq</a>
 */
public class MainTest {

	@Test
	public void main() throws Exception {
		Main main = new Main();
		// 这里可将地址换成你的远程视频服务器地址
		// 例如 recordCamera("rtmp://192.168.30.21/live/record1",25);
//		main.recordCamera("C:\\Users\\firej\\Downloads\\output1234.mp4", 25);

//		String inputFile = "rtsp://admin:admin@192.168.2.236:37779/cam/realmonitor?channel=1&subtype=0";
		String inputFile = "C:\\Users\\firej\\Downloads\\output1234.mp4";
		// Decodes-encodes
		String outputFile = "C:\\Users\\firej\\Downloads\\recorde.mp4";
		main.frameRecord(inputFile, outputFile, true);
	}
}