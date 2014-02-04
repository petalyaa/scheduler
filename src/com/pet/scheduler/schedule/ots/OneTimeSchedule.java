package com.pet.scheduler.schedule.ots;

import java.sql.Timestamp;

import com.pet.scheduler.Schedule;

/**
 * Class Description	: 
 * Created By			: Khairul Ikhwan
 * Created Date			: Feb 4, 2014
 * Current Version		: 1.0
 * Latest Changes By	: 
 * Latest Changes Date	: 
 */
public class OneTimeSchedule implements Schedule {

	private static final long serialVersionUID = 151267258223383195L;
	
	private Timestamp runningTime;
	
	private boolean isPending;
	
	/**
	 * @param params
	 */
	public OneTimeSchedule(Timestamp runningTime) {
		this.runningTime = runningTime;
		this.isPending = true;
	}

	/* (non-Javadoc)
	 * @see com.pet.scheduler.Schedule#getNextJobSchedule()
	 */
	@Override
	public Timestamp getNextJobSchedule() {
		return runningTime;
	}
	
	/* (non-Javadoc)
	 * @see com.pet.scheduler.Schedule#hasNextSchedule()
	 */
	@Override
	public boolean hasNextSchedule() {
		return isPending;
	}

	/* (non-Javadoc)
	 * @see com.pet.scheduler.Schedule#signalJobComplete()
	 */
	@Override
	public void signalJobComplete() {
		isPending = false;
	}

}
