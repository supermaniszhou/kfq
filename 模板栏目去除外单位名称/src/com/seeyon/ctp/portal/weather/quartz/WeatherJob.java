package com.seeyon.ctp.portal.weather.quartz;

import java.util.Date;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.portal.weather.manager.WeatherAreaInfoManager;


public class WeatherJob extends TimerTask{
	private static final Log log = LogFactory.getLog(WeatherJob.class);
	//获取天气数据
	private void initWeatherData(){
		WeatherAreaInfoManager weatherAreaInfoManager=(WeatherAreaInfoManager)AppContext.getBean("weatherAreaInfoManager");
		weatherAreaInfoManager.initWeatherData();		
	}

	@Override
	public void run() {
		try {
			//zhou
//			initWeatherData();
			log.info("天气定时任务执行成功,时间:"+new Date());
		} catch (Exception e) {
			log.error("天气定时任务执行异常!",e);
		}
	}
}
