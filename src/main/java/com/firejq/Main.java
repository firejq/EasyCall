package com.firejq;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author <a href="mailto:firejq@outlook.com">firejq</a>
 */
public class Main {
	public void demo() throws Exception {
		OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
		grabber.start();   // 开始获取摄像头数据

		CanvasFrame canvas = new CanvasFrame("摄像头");//新建一个窗口
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);

		while (true) {
			if(!canvas.isDisplayable()) {//窗口是否关闭
				grabber.stop();//停止抓取
				System.exit(2);//退出
				return;
			}
			// 获取摄像头图像并放到窗口上显示，
			// 这里的Frame frame=grabber.grab(); frame是一帧视频图像
			canvas.showImage(grabber.grab());

			Thread.sleep(20);//50毫秒刷新一次图像
		}
	}

	/**
	 * 转流器 按帧录制本机摄像头视频（边预览边录制，停止预览即停止录制）
	 *
	 * @author eguid
	 * @param outputFile - 录制的文件路径，也可以是rtsp或者rtmp等流媒体服务器发布地址
	 * @param frameRate - 视频帧率
	 * @throws Exception
	 * @throws InterruptedException
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 */
	public void recordCamera(String outputFile, double frameRate)
			throws Exception, InterruptedException,
				   org.bytedeco.javacv.FrameRecorder.Exception {
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		// 本机摄像头默认0，这里使用javacv的抓取器
		// 至于使用的是ffmpeg还是opencv，请自行查看源码
		//		FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(0);
		FrameGrabber grabber = FrameGrabber.createDefault(0);
		grabber.start();//开启抓取器

		OpenCVFrameConverter.ToIplImage converter
				= new OpenCVFrameConverter.ToIplImage();//转换器
		//抓取一帧视频并将其转换为图像，至于用这个图像用来做什么？加水印，人脸识别等等自行添加
		opencv_core.IplImage grabbedImage = converter.convert(grabber.grab());
		int width = grabbedImage.width();
		int height = grabbedImage.height();

		FrameRecorder recorder = FrameRecorder.createDefault(outputFile,
															 width,
															 height);
		// avcodec.AV_CODEC_ID_H264，编码
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		// 封装格式，如果是推送到rtmp就必须是flv封装格式
		recorder.setFormat("flv");
		recorder.setFrameRate(frameRate);
		recorder.start();//开启录制器

		CanvasFrame canvas = new CanvasFrame("camera",
											 CanvasFrame.getDefaultGamma()
													 / grabber.getGamma());
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);
		Frame rotatedFrame;
		long startTime = System.currentTimeMillis();
		while (canvas.isVisible()
				&& (grabbedImage = converter.convert(grabber.grab())) != null) {
			rotatedFrame = converter.convert(grabbedImage);
			canvas.showImage(rotatedFrame);
			recorder.setTimestamp(1000 * (System.currentTimeMillis() - startTime));
			recorder.record(rotatedFrame);
			Thread.sleep(30);
		}
		canvas.dispose();
		recorder.stop();
		recorder.release();
		grabber.stop();
	}

	/**
	 * 按帧录制视频
	 *
	 * @param inputFile-该地址可以是网络直播/录播地址，也可以是远程/本地文件路径
	 * @param outputFile
	 *            -该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
	 * @throws FrameGrabber.Exception
	 */
	public void frameRecord(String inputFile,
							String outputFile,
							boolean AUDIO_ENABLED)
			throws Exception {

		boolean isStart = true;//该变量建议设置为全局控制变量，用于控制录制结束
		// 是否录制音频
		int audioChannel = AUDIO_ENABLED ? 1 : 0;
		// 获取视频源
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
		// 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
		//		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile,
		//															   1280,
		//															   720,
		//															   audioChannel);

		// 开始取视频源
		grabber.start();
		//			grabber.setTimestamp();
		CanvasFrame canvas = new CanvasFrame("摄像头");//新建一个窗口
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);

		//			recorder.start();
		Frame frame;
		while (isStart && canvas.isDisplayable()) {
			frame = grabber.grab();
			// byte [] bytes = frame.image; todo
			if (frame == null)
				break;
			if (frame.image == null)
				continue;
			//				recorder.record(frame);
			// 获取摄像头图像并放到窗口上显示，
			canvas.showImage(frame);
			Thread.sleep(100);//50毫秒刷新一次图像
		}
		//			recorder.stop();
		grabber.stop();
	}

	public static void main(String [] args) throws InterruptedException {
		// 同意chat后执行以下逻辑
		String remoteAddress = "127.0.0.1";

		new ChatInputThread(remoteAddress).start();
		Thread.sleep(1000L); // 本地调试时才需要
		new ChatOutputThread(remoteAddress).start();

	}
}
