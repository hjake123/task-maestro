import java.util.Date;
import java.util.Timer;

public class MTaskNotification{
		
	private MTask assignedTask;
	private Date dateToNotify;
	private boolean notified;
	private Timer timer;
	private NotifyTask notifier;
	private String message;
	private boolean addTaskName;
	
	public MTask getAssignedTask() {
		return assignedTask;
	}
	
	public void setAssignedTask(MTask task) {
		assignedTask = new MTask(task);
	}
	
	public String getAssignedTaskName() {
		return assignedTask.getName();
	}
	
	public Date getDateToNotify() {
		return dateToNotify;
	}
	
	public void setDateToNotify(Date dateToNotify) {
		this.dateToNotify = dateToNotify;
	}
	public boolean shouldAddTaskName() {
		return addTaskName;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public boolean hasNotified() {
		return notified;
	}
	
	public void setNotified(boolean n) {
		notified = n;
	}

	public MTaskNotification(MTask task, Date date, String message, boolean addTaskName){
		notified = false;
		this.setAssignedTask(task);
		dateToNotify = date;
		
		timer = new Timer();
		this.message = message;
		this.addTaskName = addTaskName;
	}	
	
	public MTaskNotification(MTask task, long time, String message, boolean addTaskName){
		notified = false;
		this.setAssignedTask(task);
		dateToNotify = new Date();
		dateToNotify.setTime(time);
		
		timer = new Timer();
		this.message = message;
		this.addTaskName = addTaskName;
	}		
	
	public MTaskNotification(MTask task, long time, String message, boolean notified, boolean addTaskName){
		this.notified = notified;
		this.setAssignedTask(task);
		dateToNotify = new Date();
		dateToNotify.setTime(time);
		
		timer = new Timer();
		this.message = message;
		this.addTaskName = addTaskName;
	}
	
	public void startTimer() {
		notifier = new NotifyTask(this);
		timer.schedule(notifier, dateToNotify);
	}
	
	public void cancel() {
		if(notifier != null) {
			notifier.cancel();
		}
	}
	
	public String toCode() {
		return assignedTask.getId() + "\t" + dateToNotify.getTime() + "\t" + message + "\t" + notified + "\t" + addTaskName;
	}
	
	public String toString() {
		return "Notification for '" + assignedTask.getName() + "' (" + assignedTask.getId() + "), to occur on " + dateToNotify + ". Message: \"" + message + "\" Notified: " + notified;
	}
}
