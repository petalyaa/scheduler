package com.pet.scheduler.schedule.cron;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

import org.quartz.CronExpression;

import com.pet.scheduler.Schedule;
import com.pet.scheduler.exception.SchedulerException;

/**
 * Class Description	: 
 * Created By			: Khairul Ikhwan
 * Created Date			: Feb 4, 2014
 * Current Version		: 1.0
 * Latest Changes By	: 
 * Latest Changes Date	: 
 */
public class CronSchedule implements Schedule {

	private static final long serialVersionUID = -3369227113576999961L;
	
	private Date nextScheduleTime;
	
	private Date lastScheduleTime;
	
	private CronExpression cronExpr;
	
	public CronSchedule(String crontab) throws SchedulerException {
		try {
			cronExpr = new CronExpression(crontab);
			if(lastScheduleTime == null)
				lastScheduleTime = new Timestamp(System.currentTimeMillis());
			nextScheduleTime = cronExpr.getNextValidTimeAfter(lastScheduleTime);
		} catch (ParseException e) {
			throw new SchedulerException("Failed to parse crontab", e);
		}
		
	}

	/* (non-Javadoc)
	 * @see com.pet.scheduler.Schedule#getNextJobSchedule()
	 */
	@Override
	public Date getNextJobSchedule() {
		return nextScheduleTime;
	}

	/* (non-Javadoc)
	 * @see com.pet.scheduler.Schedule#hasNextSchedule()
	 */
	@Override
	public boolean hasNextSchedule() {
		nextScheduleTime = cronExpr.getNextValidTimeAfter(lastScheduleTime);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.pet.scheduler.Schedule#signalJobComplete()
	 */
	@Override
	public void signalJobComplete() {
		lastScheduleTime = nextScheduleTime;
	}

}
