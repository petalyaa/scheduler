package com.pet.scheduler.exception;

import java.text.ParseException;

/**
 * Class Description	: 
 * Created By			: Khairul Ikhwan
 * Created Date			: Feb 4, 2014
 * Current Version		: 1.0
 * Latest Changes By	: 
 * Latest Changes Date	: 
 */
public class SchedulerException extends Exception {

	private static final long serialVersionUID = -5241093877344207269L;
	
	public SchedulerException() {
		super();
	}
	
	public SchedulerException(String msg) {
		super(msg);
	}
	
	/**
	 * @param string
	 * @param e
	 */
	public SchedulerException(String string, ParseException e) {
		super(string, e);
	}

}
