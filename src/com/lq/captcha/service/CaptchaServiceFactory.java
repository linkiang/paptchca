package com.lq.captcha.service;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.patchca.color.ColorFactory;
import org.patchca.color.SingleColorFactory;
import org.patchca.filter.predefined.CurvesRippleFilterFactory;
import org.patchca.font.RandomFontFactory;
import org.patchca.service.Captcha;
import org.patchca.service.CaptchaService;
import org.patchca.service.ConfigurableCaptchaService;
import org.patchca.utils.encoder.EncoderHelper;
import org.patchca.word.RandomWordFactory;

///
// 验证码处理器工厂
// 单一多彩、带干扰线的验证码；
// 
// @author iPan
// @version 2014-2-19
//
public class CaptchaServiceFactory {
	private static final String DEFAULT_CHARACTERS = "123456789"; // 自己设置！
	private static int DEFAULT_FONT_SIZE = 25;
	private static int DEFAULT_WORD_LENGTH = 4;
	private static int DEFAULT_WIDTH = 80;
	private static int DEFAULT_HEIGHT = 35;

	private CaptchaServiceFactory() {
	}

	public static CaptchaService create(int fontSize, int wordLength, String characters, int width, int height) {
		ConfigurableCaptchaService service = null;
		// 字体大小设置
		RandomFontFactory ff = new RandomFontFactory();
		ff.setMinSize(fontSize);
		ff.setMaxSize(fontSize);
		// 生成的单词设置
		RandomWordFactory rwf = new RandomWordFactory();
		rwf.setCharacters(characters);
		rwf.setMinLength(wordLength);
		rwf.setMaxLength(wordLength);
		// 干扰线波纹
		CurvesRippleFilterFactory crff = new CurvesRippleFilterFactory();
		// 处理器
		service = new ExConfigurableCaptchaService();
		service.setFontFactory(ff);
		service.setWordFactory(rwf);
		service.setFilterFactory(crff);
		// 生成图片大小（像素）
		service.setWidth(width);
		service.setHeight(height);
		return service;
	}

	public static CaptchaService create() {
		return create(DEFAULT_FONT_SIZE, DEFAULT_WORD_LENGTH, DEFAULT_CHARACTERS, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	// 随机变色处理
	static class ExConfigurableCaptchaService extends ConfigurableCaptchaService {
		private static final Random RANDOM = new Random();
		private List colorList = new ArrayList(); // 为了性能

		public ExConfigurableCaptchaService() {
			colorList.add(new SingleColorFactory(Color.blue));
			colorList.add(new SingleColorFactory(Color.black));
			colorList.add(new SingleColorFactory(Color.red));
			colorList.add(new SingleColorFactory(Color.pink));
			colorList.add(new SingleColorFactory(Color.orange));
			colorList.add(new SingleColorFactory(Color.green));
			colorList.add(new SingleColorFactory(Color.magenta));
			colorList.add(new SingleColorFactory(Color.cyan));
		}

		public ColorFactory nextColorFactory() {
			int index = RANDOM.nextInt(colorList.size());
			return (ColorFactory) colorList.get(index);
		}

		@Override
		public Captcha getCaptcha() {
			ColorFactory cf = nextColorFactory();
			CurvesRippleFilterFactory crff = (CurvesRippleFilterFactory) this.getFilterFactory();
			crff.setColorFactory(cf);
			this.setColorFactory(cf);
			return super.getCaptcha();
		}
	}

	public static void main(String[] args) throws Exception {
		CaptchaService service = CaptchaServiceFactory.create();
		for (int i = 0; i < 10; ++i) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream("d:/none_" + i + ".png");
				String captcha = EncoderHelper.getChallangeAndWriteImage(service, "png", fos);
				System.out.println(captcha);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception e) {
					}
				}
			}
		}
	}
}
