package com.pet.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.pet.scheduler.exception.SchedulerException;
import com.pet.scheduler.model.Job;
import com.pet.scheduler.util.SchedulerConstants;

/**
 */
public class SchedulerService {
	
	private static final Logger logger = Logger.getLogger(SchedulerService.class);

	private static SchedulerService INSTANCE;
	
	private boolean isTerm;
	
	private Map<Long, Job> jobListing;
	
	private String dataPath;
	
	private boolean isServiceStarted;
	
	public static final SchedulerService getInstance() {
		if(INSTANCE == null)  {
			logger.debug("SchedulerService is null, creating new instance.");
			INSTANCE = new SchedulerService();
		}
		return INSTANCE;
	}
	
	private SchedulerService() {
		jobListing = new HashMap<Long, Job>();
		File dataDirectory = new File(SchedulerConstants.DATA_FOLDER);
		if(!dataDirectory.exists())
			dataDirectory.mkdir();
		dataPath = dataDirectory.getAbsolutePath();
		readExistingJob();
	}
	
	private void readExistingJob() {
		File dataDirectory = new File(dataPath);
		for(File jobFile : dataDirectory.listFiles()) {
			if(jobFile.isFile() && jobFile.getName().endsWith(SchedulerConstants.JOB_FILE_EXT)) {
				Job job = readJobFile(jobFile.getAbsolutePath());
				jobListing.put(job.getId(), job);
			}
		}
	}
	
	public void addJob(Job job) {
		logger.debug("Adding new job to scheduler service monitor. [startDate : " + job.getStartDate() + ", expiryDate : " + job.getExpiryDate() + "]");
		jobListing.put(job.getId(), job);
		writeJobFile(job);
	}
	
	public void removeJob(long id) {
		logger.debug("Removing job id " + id);
		jobListing.remove(id);
		deleteJobFile(id);
	}
	
	public void updateJob(Job job) {
		logger.debug("Updating job id " + job.getId());
		removeJob(job.getId());
		addJob(job);
	}
	
	public Job getJobById(long id) {
		Job job = null;
		File jobFile = new File(dataPath, id + SchedulerConstants.JOB_FILE_EXT);
		if(jobFile.exists())
			job = readJobFile(jobFile.getAbsolutePath());
		return job;
	}
	
	private synchronized void deleteJobFile(long id) {
		logger.debug("Deleting job file " + id + SchedulerConstants.JOB_FILE_EXT);
		File jobFile = new File(dataPath, id + SchedulerConstants.JOB_FILE_EXT);
		jobFile.delete();
	}
	
	private synchronized void writeJobFile(Job job) {
		logger.debug("Writing job file for id : " + job.getId());
		long id = job.getId();
		String jobFileName = id + SchedulerConstants.JOB_FILE_EXT;
		File jobFile = new File(dataPath, jobFileName);
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(jobFile.getAbsoluteFile(), true);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(job);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(oos != null)
					oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Job readJobFile(String jobFilePath) {
		Job job = null;
		File jobFile = new File(jobFilePath);
		if(jobFile.exists()) {
			FileInputStream fis = null;
			ObjectInputStream ois = null;
			try {
				fis = new FileInputStream(jobFile);
				ois = new ObjectInputStream(fis);
				Object obj = ois.readObject();
				if(obj instanceof Job) 
					job = (Job) obj;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					if(ois != null)
						ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if(fis != null)
						fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		return job;
	}
	
	public void startService() throws SchedulerException {
		if(isServiceStarted)
			throw new SchedulerException("Failed to start service. Service already start.");
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				logger.debug("Starting new scheduler service.");
				isServiceStarted = true;
				while(!isTerm) {
					try {
						logger.debug("Found " + jobListing.size() + " jobs");
						for(long jobId : jobListing.keySet()) {
							Job job = jobListing.get(jobId);
							logger.debug("Processing job for  " + job.getName());
							Schedule jobSchedule = job.getSchedule();
							if(jobSchedule != null) {
								if(jobSchedule.hasNextSchedule()) {
									Date nextRunningTime = jobSchedule.getNextJobSchedule();
									Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
										if(job.isActive()) {
											Date expiryDate = job.getExpiryDate();
											Date startDate = job.getStartDate();
											if(startDate == null || (startDate != null && startDate.before(new Date()))) {
												if(expiryDate == null || (expiryDate != null && expiryDate.after(new Date()))) {
													if(nextRunningTime.before(currentTimeStamp) || nextRunningTime.equals(currentTimeStamp)) {
														Executor executor = job.getExecutor();
														if(executor != null) {
															executor.execute();
														} else {
															logger.debug("Job " + job.getName() + " is missing executor, so removing it.");
															removeJob(jobId);
														}
														jobSchedule.signalJobComplete();
														job.setSchedule(jobSchedule);
														jobListing.put(jobId, job);
														updateJob(job);
													} else {
														logger.debug("Not triggering it yet. Next running time is : " + nextRunningTime);
													}
												} else {
													logger.debug("Job already expired. So not executing it.");
												}
											} else {
												logger.debug("Job schedule in future. So not executing it [" + startDate + "]");
											}
										} else {
											logger.debug("Job is inactive. So not executing it.");
										}
								} else {
									removeJob(jobId);
								}
							} else {
								logger.debug("Job " + job.getName() + " missing schedule, so removing it.");
								removeJob(jobId);
							}
							
						}
						
						Thread.sleep(SchedulerConstants.SLEEP_DURATION);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
	}
	
	
	public void stopService() {
		this.isTerm = true;
	}
	
}
