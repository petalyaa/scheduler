package com.pet.scheduler.test;

import com.pet.scheduler.Executor;

/**
 * Class Description	: 
 * Created By			: Khairul Ikhwan
 * Created Date			: Feb 4, 2014
 * Current Version		: 1.0
 * Latest Changes By	: 
 * Latest Changes Date	: 
 */
public class TestHelloExecutor implements Executor {

	private static final long serialVersionUID = 656640056001031236L;
	
	private long jobId;
	
	public TestHelloExecutor(long jobId) {
		this.jobId = jobId;
	}
	
	/* (non-Javadoc)
	 * @see com.pet.scheduler.Executor#execute()
	 */
	@Override
	public void execute() {
		System.err.println("Greet hello world now from job " + jobId + "!!!");
	}

}
