package com.pet.scheduler;

import java.io.Serializable;
import java.util.Date;

/**
 * Class Description	: 
 * Created By			: Khairul Ikhwan
 * Created Date			: Feb 4, 2014
 * Current Version		: 1.0
 * Latest Changes By	: 
 * Latest Changes Date	: 
 */
public interface Schedule extends Serializable {
	
	public Date getNextJobSchedule();
	
	public boolean hasNextSchedule();
	
	public void signalJobComplete();
	
}
