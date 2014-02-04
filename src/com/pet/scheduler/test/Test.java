package com.pet.scheduler.test;

import java.util.Calendar;
import java.util.Date;

import com.pet.scheduler.Executor;
import com.pet.scheduler.Schedule;
import com.pet.scheduler.SchedulerService;
import com.pet.scheduler.exception.SchedulerException;
import com.pet.scheduler.model.Job;
import com.pet.scheduler.schedule.cron.CronSchedule;

/**
 * Class Description	: 
 * Created By			: Khairul Ikhwan
 * Created Date			: Feb 3, 2014
 * Current Version		: 1.0
 * Latest Changes By	: 
 * Latest Changes Date	: 
 */
public class Test {
	
	public static void main(String[] args) {
		SchedulerService service = SchedulerService.getInstance();
		try {
			service.startService();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		
//		int i = 0;
//		int jobToTest = 1;
//		
//		while(i < jobToTest) {
//			try {
//				Thread.sleep(2000);
//				Job helloJob = new Job();
//				helloJob.setName("HelloJob");
//				
//				String cronTab = "0 * * * * ?";
//				Schedule helloCronSchedule = new CronSchedule(cronTab);
//				helloJob.setSchedule(helloCronSchedule);
//				
//				Executor executor = new TestHelloExecutor(helloJob.getId());
//				helloJob.setExecutor(executor);
//				helloJob.setActive(true);
//				
//				Calendar cal = Calendar.getInstance();
//				cal.setTime(new Date());
//				cal.add(Calendar.MINUTE, 2);
//				helloJob.setStartDate(cal.getTime());
//				cal.add(Calendar.MINUTE, 2);
//				helloJob.setExpiryDate(cal.getTime());
//				
//				service.addJob(helloJob);
//				i++;
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//				break;
//			} catch (SchedulerException e) {
//				e.printStackTrace();
//				break;
//			}
//		}
	}

}
