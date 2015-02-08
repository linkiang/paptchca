package com.lq.captcha.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lq.captcha.servlet.SimpleCaptcha;
import com.oracle.webservices.internal.api.databinding.Databinding.Builder;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class Zcaptcha {

	private static int IMAGE_WIDTH = 100;
	private static int IMAGE_HEIGHT = 50;
	private static int LINE_COUNT = 40;// 干扰线数量
	private static int WORD_COUNT = 4;// 随机产生字符数量
	private static float FATOR = 0.7f;
	private final static String SIMPLE_CAPCHA_SESSION_KEY = "SIMPLE_CAPCHA_SESSION_KEY";

	private static char[] CAPTCHARS = new char[] { 'a', 'b', 'c', 'd', 'e', '2', '3', '4', '5', '6', '7', '8', 'g', 'f', 'y', 'n', 'm', 'n', 'p', 'w', 'x' };

	private static Font[] FONTS = new Font[5];
	static {
		FONTS[0] = new Font("Ravie", Font.BOLD, 70);
		FONTS[1] = new Font("Antique Olive Compact", Font.PLAIN, 45);
		FONTS[2] = new Font("Forte", Font.BOLD, 45);
		FONTS[3] = new Font("Wide Latin", Font.PLAIN, 40);
		FONTS[4] = new Font("Gill Sans Ultra Bold", Font.PLAIN, 70);
	}

	private Random random = new Random();

	private static Zcaptcha ME = new Zcaptcha();

	private Zcaptcha() {
	}

	/**
	 * 产生随机字体
	 * 
	 * @return
	 */
	private Font getFont() {
		return FONTS[random.nextInt(5)];
	}

	/**
	 * 随机产生定义的颜色
	 * 
	 * @return
	 */
	private Color getRandColor() {
		Random random = new Random();
		Color color[] = new Color[10];
		color[0] = new Color(32, 158, 25);
		color[1] = new Color(218, 42, 19);
		color[2] = new Color(31, 75, 208);
		return color[random.nextInt(3)];
	}

	private void shear(Graphics g, int w1, int h1, Color color) {

		shearX(g, w1, h1, color);
		shearY(g, w1, h1, color);
	}

	private void shearX(Graphics g, int w1, int h1, Color color) {

		int period = random.nextInt(2);

		boolean borderGap = true;
		int frames = 1;
		int phase = random.nextInt(2);

		for (int i = 0; i < h1; i++) {
			double d = (double) (period >> 1) * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
			g.copyArea(0, i, w1, 1, (int) d, 0);
			if (borderGap) {
				g.setColor(color);
				g.drawLine((int) d, i, 0, i);
				g.drawLine((int) d + w1, i, w1, i);
			}
		}

	}

	private void shearY(Graphics g, int w1, int h1, Color color) {

		int period = random.nextInt(40) + 10; // 50;

		boolean borderGap = true;
		int frames = 20;
		int phase = 7;
		for (int i = 0; i < w1; i++) {
			double d = (double) (period >> 1) * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
			g.copyArea(i, 0, 1, h1, 0, (int) d);
			if (borderGap) {
				g.setColor(color);
				g.drawLine(i, (int) d, i, 0);
				g.drawLine(i, (int) d + h1, i, h1);
			}

		}

	}

	// 画一横线
	private void drawThickLine(Graphics g, int x1, int y1, int x2, int y2, int thickness, Color c) {

		// The thick line is in fact a filled polygon
		g.setColor(c);
		int dX = x2 - x1;
		int dY = y2 - y1;
		// line length
		double lineLength = Math.sqrt(dX * dX + dY * dY);

		double scale = (double) (thickness) / (2 * lineLength);

		// The x and y increments from an endpoint needed to create a
		// rectangle...
		double ddx = -scale * (double) dY;
		double ddy = scale * (double) dX;
		ddx += (ddx > 0) ? 0.5 : -0.5;
		ddy += (ddy > 0) ? 0.5 : -0.5;
		int dx = (int) ddx;
		int dy = (int) ddy;

		// Now we can compute the corner points...
		int xPoints[] = new int[4];
		int yPoints[] = new int[4];

		xPoints[0] = x1 + dx;
		yPoints[0] = y1 + dy;
		xPoints[1] = x1 - dx;
		yPoints[1] = y1 - dy;
		xPoints[2] = x2 - dx;
		yPoints[2] = y2 - dy;
		xPoints[3] = x2 + dx;
		yPoints[3] = y2 + dy;

		g.fillPolygon(xPoints, yPoints, 4);
	}

	/*
	 * 绘制干扰线
	 */
	private void drowLine(Graphics g) {
		int x = random.nextInt(IMAGE_WIDTH);
		int y = random.nextInt(IMAGE_HEIGHT);
		int xl = random.nextInt(13);
		int yl = random.nextInt(15);
		g.drawLine(x, y, x + xl, y + yl);
	}

	private void drowLines(Graphics g) {
		for (int i = 0; i <= LINE_COUNT; i++) {
			drowLine(g);
		}
	}

	private String getRandomString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < WORD_COUNT; i++) {
			result.append(CAPTCHARS[random.nextInt(CAPTCHARS.length)]);
		}
		return result.toString();
	}

	public Zcaptcha build() {
		return ME;
	}

	public static void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		Zcaptcha cap = ME;
		String randString = cap.getRandomString();

		/**
		 * 放放Session
		 */
		req.getSession().setAttribute(Zcaptcha.SIMPLE_CAPCHA_SESSION_KEY, randString);
		/**
		 * 得到输出流
		 */
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(resp.getOutputStream());

		BufferedImage bi = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_BYTE_INDEXED);

		Graphics2D graphics = bi.createGraphics();

		/**
		 * 设置背景色
		 */
		graphics.setColor(Color.white);

		graphics.fillRect(0, 0, bi.getWidth(), bi.getHeight());

		graphics.setColor(Color.black);
		// AttributedString attstr = new AttributedString(test);

		// TextLayout textTl = new TextLayout(test, new Font("Courier",
		// Font.BOLD, 70), new FontRenderContext(null, true, false));
		// AffineTransform textAt = graphics.getTransform();

		cap.drawString(graphics, randString, 10, 70);
		// textTl.draw(graphics, 4, 60);
		int w = bi.getWidth();
		int h = bi.getHeight();
		cap.shear(graphics, w, h, Color.white);
		cap.drawThickLine(graphics, 0, cap.random.nextInt(IMAGE_HEIGHT) + 1, IMAGE_WIDTH, cap.random.nextInt(IMAGE_HEIGHT) + 1, 4, Color.BLACK);

		resp.setContentType("image/jpg");

		encoder.encode(bi);

	}

	public static void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		Zcaptcha cap = ME;
		String randString = cap.getRandomString();

		/**
		 * 放放Session
		 */
		req.getSession().setAttribute(Zcaptcha.SIMPLE_CAPCHA_SESSION_KEY, randString);
		/**
		 * 得到输出流
		 */

		// BufferedImage类是具有缓冲区的Image类,Image类是用于描述图像信息的类
		BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_BGR);

		Graphics g = image.getGraphics();// 产生Image对象的Graphics对象,改对象可以在图像上进行各种绘制操作
		g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

		cap.drawString1(g, randString, 0, (int)(IMAGE_HEIGHT*FATOR));
		g.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, (int)(IMAGE_HEIGHT*FATOR)));
		// 绘制干扰线
		g.setColor(cap.getRandColor());
		cap.drowLines(g);
		int w = image.getWidth();
		int h = image.getHeight();
		cap.shear(g, w, h, Color.white);
		cap.drawThickLine(g, 0, cap.random.nextInt(IMAGE_HEIGHT) + 1, IMAGE_WIDTH, cap.random.nextInt(IMAGE_HEIGHT) + 1, 4, Color.BLACK);

		resp.setContentType("image/jpg,image/jpeg");// 设置相应类型,告诉浏览器输出的内容为图片
//		resp.setContentType("image/jpeg");
		resp.setHeader("Pragma", "No-cache");// 设置响应头信息，告诉浏览器不要缓存此内容
		resp.setHeader("Cache-Control", "no-cache");
		resp.setDateHeader("Expire", 0);

		g.dispose();
		try {
			ImageIO.write(image, "JPEG", resp.getOutputStream());// 将内存中的图片通过流动形式输出到客户端
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void drawString(Graphics g, String s, int x, int y) {
		int width = IMAGE_WIDTH/s.length();
		for (int i = 0; i < s.length(); i++) {
			// g.setFont(new Font("Courier", Font.BOLD, 70));
			g.setFont(ME.getFont());
			g.setColor(ME.getRandColor());
			g.drawString(String.valueOf(s.charAt(i)), x + width * i, y);
		}

	}

	/*
	 * 绘制字符串
	 */
	private void drawString1(Graphics g, String s, int x, int y) {
		int width = IMAGE_WIDTH/s.length();
		for (int i = 0; i < s.length(); i++) {
			g.setFont(getFont1());
			g.setColor(new Color(random.nextInt(101), random.nextInt(111), random.nextInt(121)));
			g.translate(random.nextInt(3), random.nextInt(3));
			g.drawString(String.valueOf(s.charAt(i)), x + width * i, y);
		}
	}

	/*
	 * 获得字体
	 */
	private Font getFont1() {
		return new Font("Fixedsys", Font.CENTER_BASELINE, (int) (IMAGE_HEIGHT*FATOR));
	}

}
