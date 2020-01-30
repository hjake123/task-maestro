import java.util.Date;

public class MTask implements Comparable<MTask>{
	
	private String name;
	private Date startDate = new Date();
	private Date endDate = new Date();
	private int id;
	private boolean completed;
	
	public MTask (String name, Date startDate, long duration) {
		this.setName(name);
		this.setStartDate(startDate);
		endDate = new Date();
		this.setDuration(duration);
		id = this.hashCode();
		completed = false;
	}
	
	public MTask (String name, Date startDate, long duration, boolean completed) {
		this.setName(name);
		this.setStartDate(startDate);
		endDate = new Date();
		this.setDuration(duration);
		id = this.hashCode();
		this.completed = completed;
	}
	
	public MTask (int id, String name, long start, long end, boolean completed) {
		this.id = id;
		this.setName(name);
		startDate.setTime(start);
		endDate.setTime(end);
		this.setCompleted(completed);
	}
	
	public MTask(MTask that) {
		this.setName(that.getName());
		this.setStartDate(that.getStartDate());
		endDate = new Date();
		this.setDuration(that.getDuration());
		this.setCompleted(that.isCompleted());
		id = that.getId();
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isCompleted() {
		return completed;
	}
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setDuration(long durationInMillis) {
		long startInMillis = startDate.getTime();
		startInMillis += durationInMillis;
		endDate.setTime(startInMillis);
	}
	
	public long getDuration() {
		return endDate.getTime() - startDate.getTime();
	}
	
	public long getDurationInMinutes() {
		return getDuration() / 60000;
	}
	
	public long getTimeUntil(Date otherDate) {
		return otherDate.getTime() - startDate.getTime();
	}
	
	public boolean isConflicting(MTask otherTask) {
		if(completed || otherTask.isCompleted()) {
			return false;
		}
		if(otherTask.getStartDate() == this.getStartDate()) {
			return true;
		}
		if(otherTask.getEndDate() == this.getEndDate()) {
			return true;
		}
		if(otherTask.getStartDate().before(endDate)) {
			if(otherTask.getEndDate().after(startDate)) {
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
		
	}
	
	public String toString() {
		return "Task '" + name + "' (" + id + ") " + startDate.toString() + " - " + endDate.toString() + ". Completed: " + completed; 
	}
	
	public String toCode() {
		return id + "\t" + name + "\t" + startDate.getTime() + "\t" + endDate.getTime() + "\t" + completed;
	}

	@Override
	public int compareTo(MTask that) {
		try {
			if(this.startDate.getTime() - that.startDate.getTime() == 0) {
				return (int) (this.endDate.getTime() - that.endDate.getTime());
			}
			return (int) (this.startDate.getTime() - that.startDate.getTime());
		}catch(Exception e) {
			if(this.startDate.getTime() < that.startDate.getTime()) {
				return -1;
			}else if (this.startDate.getTime() > that.startDate.getTime()) {
				return 1;
			}else {
				return 0;
			}
		}
	}
}
