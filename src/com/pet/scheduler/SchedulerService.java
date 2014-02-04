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

import com.pet.scheduler.model.Job;
import com.pet.scheduler.util.SchedulerConstants;

/**
 */
public class SchedulerService {

	private static SchedulerService INSTANCE;
	
	private boolean isTerm;
	
	private Map<Long, Job> jobListing;
	
	private String dataPath;
	
	public static final SchedulerService getInstance() {
		if(INSTANCE == null) 
			INSTANCE = new SchedulerService();
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
		jobListing.put(job.getId(), job);
		writeJobFile(job);
	}
	
	public void removeJob(long id) {
		jobListing.remove(id);
		deleteJobFile(id);
	}
	
	public void updateJob(Job job) {
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
		File jobFile = new File(dataPath, id + SchedulerConstants.JOB_FILE_EXT);
		jobFile.delete();
	}
	
	private synchronized void writeJobFile(Job job) {
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
	
	public void startService() {
		// Prepare all the required data...
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				while(!isTerm) {
					try {
						System.err.println("Found " + jobListing.size() + " jobs");
						for(long jobId : jobListing.keySet()) {
							Job job = jobListing.get(jobId);
							System.err.println("Processing job for  " + job.getName());
							Schedule jobSchedule = job.getSchedule();
							if(jobSchedule != null) {
								if(jobSchedule.hasNextSchedule()) {
									Date nextRunningTime = jobSchedule.getNextJobSchedule();
									Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
									if(nextRunningTime.before(currentTimeStamp) || nextRunningTime.equals(currentTimeStamp)) {
										Executor executor = job.getExecutor();
										if(executor != null) {
											executor.execute();
										} else {
											System.err.println("Job " + job.getName() + " is missing executor, so removing it.");
											removeJob(jobId);
										}
										jobSchedule.signalJobComplete();
										job.setSchedule(jobSchedule);
										jobListing.put(jobId, job);
									} else {
										System.err.println("Not triggering it yet. Next running time is : " + nextRunningTime);
									}
								} else {
									removeJob(jobId);
								}
							} else {
								System.err.println("Job " + job.getName() + " missing schedule, so removing it.");
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
