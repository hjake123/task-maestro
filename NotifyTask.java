import java.util.TimerTask;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class NotifyTask extends TimerTask{
	
	private MTaskNotification notif;
	
	public NotifyTask(MTaskNotification notif) {
		this.notif = notif;
	}
	
	public void run() { 
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if(notif.hasNotified()) {
					return;
				}
				notif.setNotified(true);
				Alert notif_alert = new Alert(Alert.AlertType.INFORMATION);
				notif_alert.setTitle("Notification");
				notif_alert.setHeaderText("Notification for task '" + notif.getAssignedTaskName() +"':");
				String message = notif.getMessage();
				if(notif.shouldAddTaskName()) {
					message += "'" + notif.getAssignedTaskName() + "'!";
				}
				notif_alert.setContentText(message);
				notif_alert.show();
				
			}
			
		});
	}
	
}
