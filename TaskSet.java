import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeSet;

public class TaskSet {

	private HashSet<MTask> tasks;
	private HashMap<MTask,HashSet<MTaskNotification>> notifMap;
	
	public TaskSet() {
		tasks = new HashSet<MTask>();
		notifMap = new HashMap<MTask,HashSet<MTaskNotification>>();
	}
	
	public void addTask(MTask task) {
		tasks.add(task);
		notifMap.put(task, new HashSet<MTaskNotification>());
	}
	
	public boolean safelyAddTask(MTask task) {
		for(MTask t : tasks) {
			if(t.isConflicting(task)) {
				return false;
			}
		}
		this.addTask(task);
		return true;
	}
	
	public HashSet<MTask> getTasks(){
		return tasks;
	}
	
	public TreeSet<MTask> getTasksByDate(){
		TreeSet<MTask> mt = new TreeSet<MTask>();
		mt.addAll(tasks);
		return mt;
	}
	
	public void clearTasks() {
		tasks.clear();
	}
	
	public boolean addNotification(MTask task, MTaskNotification notif) {
		if(notifMap.containsKey(task)) {
			HashSet<MTaskNotification> notifSet = notifMap.get(task);
			notif.startTimer();
			notifSet.add(notif);
			return true;
		}
		return false;
	}
	
	public HashSet<MTaskNotification> getNotifications(MTask task){
		return notifMap.get(task);
	}
	
	public boolean addNotification(MTask task, Date date, String message, boolean addName) {
		if(notifMap.containsKey(task)) {
			HashSet<MTaskNotification> notifSet = notifMap.get(task);
			MTaskNotification notif = new MTaskNotification(task, date, message, addName);
			notif.startTimer();
			notifSet.add(notif);
			return true;
		}
		return false;
	}
	
	public boolean addNotification(MTask task, long time, String message, boolean addName) {
		if(notifMap.containsKey(task)) {
			HashSet<MTaskNotification> notifSet = notifMap.get(task);
			MTaskNotification notif = new MTaskNotification(task, time, message, addName);
			notif.startTimer();
			notifSet.add(notif);
			return true;
		}
		return false;
	}
	
	public boolean cancelNotification(MTaskNotification notif) {
		notif.cancel();
		for(MTask task : notifMap.keySet()) {
			if(notif.getAssignedTask().getId() == task.getId()) { 
				return notifMap.get(task).remove(notif);
			}
		}
		System.out.println("Never found a matching notification.");
		return false;
	}
	
	public void cancelNotifications(MTask task) {
		HashSet<MTaskNotification> notifSet = notifMap.get(task);
		for(MTaskNotification notif : notifSet) {
			notif.cancel();
			notifSet.remove(notif);
		}
		//notifMap.put(task, notifSet); //if it doesn't remove tasks from the set, enable this line.
	}
	
	public void cancelAllNotifications() {
		for(MTask task : notifMap.keySet()) {
			HashSet<MTaskNotification> notifSet = notifMap.get(task);
			for(MTaskNotification notif : notifSet) {
				notif.cancel();
				notifSet.remove(notif);
			}
		}
	}
	
	public boolean save(File savefile) {
		PrintWriter pr;
		if(savefile == null || !savefile.exists()) {
			return false;
		}
		try {
			pr = new PrintWriter(savefile);
		} catch (FileNotFoundException e) {
			return false;
		}
		for(MTask t : tasks) {
			pr.write(t.toCode() + "\n");
		}
		pr.write("NOTIF\n");
		for(HashSet<MTaskNotification> hm : notifMap.values()) {
			for(MTaskNotification n : hm) {
				pr.write(n.toCode() + "\n");
			}
		}
				
		pr.close();
		return true;
	}
	
	public boolean load(File savefile) {
		Scanner scan;
		try {
			scan = new Scanner(savefile);
		}catch(FileNotFoundException e) {
			System.out.println("File not found exception!");
			return false;
		}catch(NullPointerException e) {
			System.out.println("File not found exception!");
			return false;
		}
		if(savefile.length() == 0) {
			System.out.println("File empty!");
			scan.close();
			return false;
		}
		
		cancelAllNotifications();
		clearTasks();
		
		String line = scan.nextLine();
		while(!line.equals("NOTIF")) {
			String[] tokens = line.split("\\\t");
			try {
				addTask(new MTask(Integer.parseInt(tokens[0]), tokens[1], Long.parseLong(tokens[2]), Long.parseLong(tokens[3]), Boolean.parseBoolean(tokens[4])));
			}catch(NumberFormatException e) {
				System.out.println("Number format exception! Either " + tokens[0] + ", " + tokens[2] + " or" + tokens[3] + " is not a number!");
				scan.close();
				return false;
			}
			line = scan.nextLine();
		}
		while(scan.hasNextLine()) {
			line = scan.nextLine();
			String[] tokens = line.split("\\\t");
			int assignedTaskId = Integer.parseInt(tokens[0]);
			MTask task = null;
			for(MTask t : tasks) {
				if(t.getId() == assignedTaskId) {
					task = t;
					break;
				}
				scan.close();
				return false;
			}
			if(task == null){
				System.out.println("Warning: Notification for nonexistant task discarded.");
			}else {
				MTaskNotification notif = new MTaskNotification(task, Long.parseLong(tokens[1]), tokens[2], Boolean.parseBoolean(tokens[3]), Boolean.parseBoolean(tokens[4]));
				if(notif.getDateToNotify().getTime() < System.currentTimeMillis()) {
					notif.setNotified(true);
				}
				addNotification(task, notif);
			}
		}
		scan.close();
		return true;
	}
	
	public MTask getTask(String name) {
		for(MTask t : tasks) {
			if(t.getName().equals(name)) {
				return t;
			}
		}
		return null;
	}
	
	public boolean removeTask(MTask task) {
		notifMap.remove(task);
		return tasks.remove(task);
	}
	
	public String getTSV() {
		String out = "";
		
		for(MTask t : tasks) {
			out += t.getName() + "\t" + t.getStartDate() + "\t" + t.getEndDate() + "\t" + t.isCompleted() + "\n";
		}
		
		return out;
	}
	
	public boolean exportAsTSV(File ef) {
		PrintWriter pr;
		if(ef == null || !ef.exists()) {
			return false;
		}
		try {
			pr = new PrintWriter(ef);
		} catch (FileNotFoundException e) {
			return false;
		}
		
		pr.write(this.getTSV());
		pr.close();
		return true;
	}
	
	public void debugPrint() {
		if(tasks.isEmpty()) {
			System.out.println("Empty!");
			return;
		}
		for(MTask t : tasks) {
			System.out.println(t);
		}
		for(HashSet<MTaskNotification> hm : notifMap.values()) {
			for(MTaskNotification n : hm) {
				System.out.println(n);
			}
		}
	}

	public HashSet<MTaskNotification> getAllNotifications() {
		HashSet<MTaskNotification> out = new HashSet<MTaskNotification>();
		for(MTask mt: notifMap.keySet()) {
			out.addAll(notifMap.get(mt));
		}
		return out;
	}
}
