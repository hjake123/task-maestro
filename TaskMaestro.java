import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class TaskMaestro extends Application{
	
	public static void main(String[] args) {
		launch();
	}
	
	//Control elements from the various main scenes.
	MenuBar menu_bar = new MenuBar();
	
	Menu file_menu = new Menu("File");
	Menu view_menu = new Menu("View");
	
	MenuItem new_option = new MenuItem("New");
	MenuItem save_option = new MenuItem("Save");
	MenuItem save_as_option = new MenuItem("Save As");
	MenuItem load_option = new MenuItem("Load");
	MenuItem export_option = new MenuItem("Export to TSV");
	MenuItem close_option = new MenuItem("Close");
	
	MenuItem tasks_option = new MenuItem("Tasks");
	MenuItem notif_option = new MenuItem("Notifications");
	
	BorderPane contain_pane = new BorderPane();
	
	Button add_task_button = new Button("Create");
	Button edit_task_button = new Button("Edit");
	Button delete_task_button = new Button("Remove");
	
	Button add_notif_button = new Button("Add");
	Button cancel_notif_button = new Button("Cancel");
	
	CheckBox task_complete_box = new CheckBox("Complete");
	CheckBox mute_box = new CheckBox("Mute Notifications");
	
	TableView<MTask> task_list = new TableView<MTask>();
	MTask selected_task;
	
	TableView<MTaskNotification> notif_list = new TableView<MTaskNotification>();
	MTaskNotification selected_notif;
	
	FileChooser f_choose_dialog = new FileChooser();
	ExtensionFilter mtd = new ExtensionFilter("Supported Files", "*.mtd");
	File recentFile = null;
	
	FileChooser tsv_choose_dialog = new FileChooser();
	ExtensionFilter tsv = new ExtensionFilter("Tab-Seperated Value Files", "*.tsv");
	
	boolean unsaved_changes = false; //set to true every time something changes, set to false when the file is saved.
	boolean notif_autocancel = false; //whether to automatically cancel notifications after they have completed.
	boolean debug_mode = false; //disable before release.
	
	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage stage) throws Exception {
		
		//Creates the back-end database object and initializes states of various elements.
		TaskSet task_set = new TaskSet();
		edit_task_button.setDisable(true);
		delete_task_button.setDisable(true);
		
		f_choose_dialog.getExtensionFilters().add(mtd);
		tsv_choose_dialog.getExtensionFilters().add(tsv);
		tsv_choose_dialog.getExtensionFilters().add(new ExtensionFilter("Text Files", "*.txt"));
		
		stage.setResizable(false);
		
		if(debug_mode) {
			task_set.addTask(new MTask("Jump up in the air!", new Date(System.currentTimeMillis() + 60000), 100000, true));
			task_set.addTask(new MTask("Jump up, don't be scared!", new Date(System.currentTimeMillis() + 2+60000), 10000));
			task_set.addTask(new MTask("Jump and let your cares go soar away!", new Date(System.currentTimeMillis() + 3+60000), 1000));
		}
		
		//The task viewing and modifying scene.
		VBox vb_task = new VBox();
		
		file_menu.getItems().addAll(new_option, load_option, save_option, save_as_option, export_option, close_option);
		view_menu.getItems().addAll(tasks_option, notif_option);
				
		menu_bar.getMenus().addAll(file_menu, view_menu);
		
		contain_pane.setTop(menu_bar);
		
		ObservableList<MTask> tasks = FXCollections.observableArrayList(task_set.getTasks());
		task_list.setItems(tasks);
		
		//set up the table view
		TableColumn<MTask, String> name_col = new TableColumn<MTask, String>("Name");
		name_col.setMinWidth(300);
		name_col.setCellValueFactory(new PropertyValueFactory<MTask, String>("name"));
		
		TableColumn<MTask, String> date_col = new TableColumn<MTask, String>("Date");
		date_col.setMinWidth(200);
		date_col.setCellValueFactory(new PropertyValueFactory<MTask, String>("startDate"));
		
		TableColumn<MTask, String> dura_col = new TableColumn<MTask, String>("Duration in minutes");
		dura_col.setMinWidth(200);
		dura_col.setCellValueFactory(new PropertyValueFactory<MTask, String>("durationInMinutes"));
		
		task_list.getColumns().addAll(name_col, date_col, dura_col);
		
		HBox button_box = new HBox();
		
		button_box.getChildren().addAll(add_task_button, edit_task_button, delete_task_button);
		button_box.setSpacing(50);
		button_box.setPadding(new Insets(20));
		button_box.setAlignment(Pos.BASELINE_CENTER);
		
		vb_task.getChildren().addAll(task_list, task_complete_box, button_box);
		
		contain_pane.setCenter(vb_task);
		
		Scene scene = new Scene(contain_pane, 900, 550);
		stage.setScene(scene);
		stage.setTitle("Task Maestro");
		stage.show();
		
		if(debug_mode) {
			Alert debug_alert = new Alert(Alert.AlertType.WARNING);
			debug_alert.setHeaderText("Alert: Debug Mode Active");
			debug_alert.setContentText("Fields will be populated with sample data to test certain features.");
			debug_alert.show();
		}
		
		//The pop-up for task creation. The 'c_' prefix denotes being used for the creation box.
		VBox task_creation_box = new VBox();
		TextField c_name_field = new TextField();
		DatePicker c_date_picker = new DatePicker();
		c_date_picker.setEditable(false);
		TextField c_duration_field = new TextField();
		
		TextField c_hour_field = new TextField();
		TextField c_min_field = new TextField();
		ChoiceBox<String> c_timetype_box = new ChoiceBox<String>(FXCollections.observableArrayList("AM", "PM"));
		
		HBox c_time_box = new HBox();
		c_time_box.getChildren().addAll(c_hour_field, new Label(":"), c_min_field, new Label(" "), c_timetype_box);
		
		VBox c_reminders_box = new VBox();
		CheckBox c_remind_before_option = new CheckBox("Remind me before the task starts");
		TextField c_reminder_dist_field = new TextField();
		c_reminder_dist_field.setVisible(false);
		c_reminder_dist_field.setPromptText("How long before?");
		HBox c_rm_before_box = new HBox();
		c_rm_before_box.getChildren().addAll(c_remind_before_option, c_reminder_dist_field);
		
		CheckBox c_remind_at_start_option = new CheckBox("Remind me when the task starts");
		CheckBox c_remind_at_end_option = new CheckBox("Remind me when the task ends");
		c_reminders_box.getChildren().addAll(c_rm_before_box, c_remind_at_start_option, c_remind_at_end_option);
		
		Button c_create_button = new Button("Create Task");
		
		task_creation_box.getChildren().addAll(new Label("Name: "), c_name_field, new Label("Date: "), c_date_picker, c_time_box, 
				new Label("Duration in minutes: "), c_duration_field, c_reminders_box, c_create_button);
		
		Scene creation_scene = new Scene(task_creation_box);
		Stage creation_popup = new Stage();
		creation_popup.setScene(creation_scene);
		creation_popup.setTitle("Create a Task");
		
		//Event handlers for task creation pop up
		c_remind_before_option.setOnAction(e ->{
			if(c_remind_before_option.selectedProperty().get()) {
				c_reminder_dist_field.setVisible(true);
			}else {
				c_reminder_dist_field.setVisible(false);
			}
		});
		
		creation_popup.setOnCloseRequest(e ->{
			c_name_field.setText("");
			c_duration_field.setText("");
			c_date_picker.setValue(null);
			c_hour_field.setText("");
			c_min_field.setText("");
			c_timetype_box.getSelectionModel().clearSelection();
			c_remind_before_option.selectedProperty().set(false);
			c_remind_at_start_option.selectedProperty().set(false);
			c_remind_at_end_option.selectedProperty().set(false);
		});
		
		c_create_button.setOnAction(e ->{
			//Verify everything is filled.
			if(c_name_field.getText().isEmpty() || c_date_picker.getValue() == null || c_hour_field.getText().isEmpty() 
					|| c_min_field.getText().isEmpty() 
					|| c_timetype_box.getValue() == null
					|| (c_remind_before_option.selectedProperty().get() && c_reminder_dist_field.getText().isEmpty())) {
				Alert empty_field_err = new Alert(Alert.AlertType.WARNING);
				empty_field_err.setHeaderText("Empty required fields.");
				empty_field_err.setContentText("Please fill all fields and try again.");
				empty_field_err.show();
			}else{ 
				//Save the new task.
				try {
					LocalDate ldate = c_date_picker.getValue();
					int hour = Integer.parseInt(c_hour_field.getText());
					int minute = Integer.parseInt(c_min_field.getText());
					int am_pm = 0;
					
					if(c_timetype_box.getValue().equals("AM")) {
						am_pm = Calendar.AM;
					}else {
						am_pm = Calendar.PM;
					}
										
					LocalDateTime ldt = ldate.atTime(hour, minute);	
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.YEAR, ldt.getYear());
					cal.set(Calendar.DAY_OF_YEAR, ldt.getDayOfYear());
					cal.set(Calendar.HOUR, hour);
					cal.set(Calendar.AM_PM, am_pm);
					cal.set(Calendar.MINUTE, minute);
					cal.set(Calendar.SECOND, 0);
										
					Date startdate = cal.getTime();
										
					MTask t = new MTask(c_name_field.getText(), startdate, (Integer.parseInt(c_duration_field.getText())*60000));
					
					task_set.addTask(t);
					tasks.add(t);
					
					unsaved_changes = true;
					
					if(c_remind_before_option.selectedProperty().get()) {
						Date remind_date = new Date(startdate.getTime() + Integer.parseInt(c_reminder_dist_field.getText())*60000);
						task_set.addNotification(t, remind_date, "Don't forget about ", true);
						notif_list.getItems().add(new MTaskNotification(t, remind_date, "Don't forget about ", true));
					}
					if(c_remind_at_start_option.selectedProperty().get()) {
						task_set.addNotification(t, startdate, "Your task '" + t.getName() + "' has started!", false);
						notif_list.getItems().add(new MTaskNotification(t, startdate, "Your task '" + t.getName() + "' has started!", false));
					}
					if(c_remind_at_end_option.selectedProperty().get()) {
						task_set.addNotification(t, t.getEndDate(), "Your task '" + t.getName() + "' has ended!", false);
						notif_list.getItems().add(new MTaskNotification(t, t.getEndDate(), "Your task '" + t.getName() + "' has ended!", false));
					}
					
				}catch(Exception err) {
					Alert data_format_err = new Alert(Alert.AlertType.ERROR);
					data_format_err.setHeaderText("Data parsing error.");
					data_format_err.setContentText("Some data could not be parsed.");
					data_format_err.show();
					
				}
				creation_popup.close();
			}
		});
		
		//The pop-up for task editing. The 'e_' prefix denotes being used for the editing box.
		VBox task_edit_box = new VBox();
		TextField e_name_field = new TextField();
		DatePicker e_date_picker = new DatePicker();
		TextField e_duration_field = new TextField();
		
		TextField e_hour_field = new TextField();
		TextField e_min_field = new TextField();
		ChoiceBox<String> e_timetype_box = new ChoiceBox<String>(FXCollections.observableArrayList("AM", "PM"));
		
		HBox e_time_box = new HBox();
		e_time_box.getChildren().addAll(e_hour_field, new Label(":"), e_min_field, new Label(" "), e_timetype_box);
		
		Button e_save_button = new Button("Save Changes");
		
		task_edit_box.getChildren().addAll(new Label("Name: "), e_name_field, new Label("Date: "), e_date_picker, 
				e_time_box, new Label("Duration in minutes: "), e_duration_field, e_save_button);
		
		Scene edit_scene = new Scene(task_edit_box);
		Stage edit_popup = new Stage();
		edit_popup.setScene(edit_scene);
		edit_popup.setTitle("Edit Task");
		
		//Event handlers for the editing pop-up
		edit_popup.setOnCloseRequest(e ->{
			e_name_field.setText("");
			e_duration_field.setText("");
			e_date_picker.setValue(null);
			e_hour_field.setText("");
			e_min_field.setText("");
			e_timetype_box.getSelectionModel().clearSelection();
		});
		
		e_save_button.setOnAction(e ->{
			//Verify everything is filled.
			if(e_name_field.getText().isEmpty() || e_date_picker.getValue() == null || e_hour_field.getText().isEmpty() 
					|| e_min_field.getText().isEmpty() 
					|| e_timetype_box.getValue() == null) {
				Alert empty_field_err = new Alert(Alert.AlertType.ERROR);
				empty_field_err.setHeaderText("Empty required fields.");
				empty_field_err.setContentText("Please fill all fields and try again.");
				empty_field_err.show();
			}else {
				//Save the changes to the task.
				try {
					LocalDate ldate = e_date_picker.getValue();
					int hour = Integer.parseInt(e_hour_field.getText());
					int minute = Integer.parseInt(e_min_field.getText());				
					int am_pm = 0;
					
					if(c_timetype_box.getValue().equals("AM")) {
						am_pm = Calendar.AM;
					}else {
						am_pm = Calendar.PM;
					}
										
					LocalDateTime ldt = ldate.atTime(hour, minute);					
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.YEAR, ldt.getYear());
					cal.set(Calendar.DAY_OF_YEAR, ldt.getDayOfYear());
					cal.set(Calendar.HOUR, hour);
					cal.set(Calendar.AM_PM, am_pm);
					cal.set(Calendar.MINUTE, minute);
					cal.set(Calendar.SECOND, 0);
					
					Date startdate = cal.getTime();
										
					MTask t = new MTask(e_name_field.getText(), startdate, (Integer.parseInt(e_duration_field.getText())*60000));
					
					HashSet<MTaskNotification> notifs_for_task = task_set.getNotifications(selected_task);
					
					task_set.removeTask(selected_task);
					tasks.remove(selected_task);
					
					task_set.addTask(t);
					tasks.add(t);
					
					for(MTaskNotification notif : notifs_for_task) {
						notif.setAssignedTask(t);
						task_set.addNotification(t, notif);
					}
					
					unsaved_changes = true;
					
				}catch(Exception err) {
					Alert data_format_err = new Alert(Alert.AlertType.ERROR);
					data_format_err.setHeaderText("Data parsing error.");
					data_format_err.setContentText("Some data could not be parsed. Chanegs were not saved.");
					data_format_err.show();
				}
				edit_popup.close();
				
			}
		});
			
		//Event handlers for task modifying scene
		add_task_button.setOnAction(e->{
			c_name_field.setText("");
			c_duration_field.setText("");
			c_date_picker.setValue(null);
			c_hour_field.setText("");
			c_min_field.setText("");
			c_timetype_box.getSelectionModel().clearSelection();
			c_remind_before_option.selectedProperty().set(false);
			c_remind_at_start_option.selectedProperty().set(false);
			c_remind_at_end_option.selectedProperty().set(false);
			creation_popup.show();
		});
		
		task_complete_box.setOnAction(e->{
		if(selected_task != null){
				selected_task.setCompleted(task_complete_box.isSelected());
				unsaved_changes = true;
			}
		});
		
		task_list.getSelectionModel().selectedItemProperty().addListener(
	            new ChangeListener<MTask>() {
	               @Override
					public void changed(ObservableValue<? extends MTask> ov, MTask old_val, MTask new_val) {
						if(new_val != null) {
							edit_task_button.setDisable(false);
							delete_task_button.setDisable(false);
						}
						selected_task = new_val;
						if(selected_task != null) {
							task_complete_box.setSelected(selected_task.isCompleted());
						}
					}
	        });
		
		edit_task_button.setOnAction(e->{
			//populate fields
			e_name_field.setText(selected_task.getName());
			Calendar cal = Calendar.getInstance();
			cal.setTime(selected_task.getStartDate());
			LocalDate ldt = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
			e_date_picker.setValue(ldt);
			e_hour_field.setText(cal.get(Calendar.HOUR) + "");
			e_min_field.setText(cal.get(Calendar.MINUTE) + "");
			if(cal.get(Calendar.AM_PM) == Calendar.AM) {
				e_timetype_box.getSelectionModel().select("AM");
			}else if(cal.get(Calendar.AM_PM) == Calendar.PM) {
				e_timetype_box.getSelectionModel().select("PM");
			}
			e_duration_field.setText((selected_task.getDuration()/60000)+"");
			edit_popup.show();
		});
		
		delete_task_button.setOnAction(e ->{
			Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
			confirmation.setHeaderText("Delete \"" + selected_task.getName() + "\"?");
			confirmation.setContentText("Are you sure you want to delete the task \"" + selected_task.getName() + "\"?");
			Optional<ButtonType> result = confirmation.showAndWait();
			if(result.isPresent() && result.get().getText().equals("OK")) {
				task_set.removeTask(selected_task);
				tasks.remove(selected_task);
				unsaved_changes = true;
			}
		});
		
		//The notification handling scene.
		VBox vb_notif = new VBox();
				
		ObservableList<MTaskNotification> notifs = FXCollections.observableArrayList(task_set.getAllNotifications());
		notif_list.setItems(notifs);
		
		add_notif_button.setDisable(true);
		cancel_notif_button.setDisable(true);
		
		//set up the table view
		TableColumn<MTaskNotification, String> n_task_col = new TableColumn<MTaskNotification, String>("Assigned Task");
		n_task_col.setMinWidth(300);
		n_task_col.setCellValueFactory(new PropertyValueFactory<MTaskNotification, String>("AssignedTaskName"));
		
		TableColumn<MTaskNotification, String> n_date_col = new TableColumn<MTaskNotification, String>("Date and Time");
		n_date_col.setMinWidth(200);
		n_date_col.setCellValueFactory(new PropertyValueFactory<MTaskNotification, String>("DateToNotify"));
		
		TableColumn<MTaskNotification, String> n_message_col = new TableColumn<MTaskNotification, String>("Message");
		n_message_col.setMinWidth(300);
		n_message_col.setCellValueFactory(new PropertyValueFactory<MTaskNotification, String>("Message"));
		
		notif_list.getColumns().addAll(n_task_col, n_date_col, n_message_col);
		
		HBox n_button_box = new HBox();
		
		n_button_box.getChildren().addAll(add_notif_button, cancel_notif_button);
		n_button_box.setSpacing(50);
		n_button_box.setPadding(new Insets(20));
		n_button_box.setAlignment(Pos.BASELINE_CENTER);
		
		CheckBox n_autocancel_option = new CheckBox("Cancel completed notifications");
		
		vb_notif.getChildren().addAll(notif_list, n_autocancel_option, n_button_box);
		
		//The notification adding pop-up. Prefix a_.
		VBox notif_add_box = new VBox();
		ChoiceBox<MTask> a_task_choose_box = new ChoiceBox<MTask>();
		TextField a_time_field = new TextField();
		TextField a_message_field = new TextField();
		Button a_add_button = new Button("Add");
		RadioButton a_start_option = new RadioButton("Set time relative to task start");
		RadioButton a_end_option = new RadioButton("Set time relative to task end");
		//RadioButton a_custom_option = new RadioButton("Set custom time");
		ToggleGroup start_end_group = new ToggleGroup();
		a_start_option.setToggleGroup(start_end_group);
		a_end_option.setToggleGroup(start_end_group);
		Label a_before_after_label = new Label("Please select one of the two options above.");
		Label task_time_label = new Label("Please choose a task.");
		
		a_task_choose_box.setItems(tasks);
		a_task_choose_box.setConverter(new MTaskNameConverter());
		
		a_time_field.setVisible(false);
		
		notif_add_box.getChildren().addAll(a_task_choose_box, task_time_label, a_start_option, a_end_option, a_before_after_label, a_time_field, new Label("Set the message to display:"), a_message_field, a_add_button);
		
		Scene notif_scene = new Scene(notif_add_box, 450, 400);
		Stage notif_add_popup = new Stage();
		notif_add_popup.setScene(notif_scene);
		
		//Event handlers for notification adding pop-up.
		
		a_task_choose_box.setOnAction(e->{
			if(a_task_choose_box.getSelectionModel().getSelectedItem() != null) {
				task_time_label.setText(a_task_choose_box.getSelectionModel().getSelectedItem().getStartDate() + " - " + a_task_choose_box.getSelectionModel().getSelectedItem().getEndDate());
			}
		});
		
		a_start_option.setOnAction(e->{
			if(a_start_option.isSelected()) {
				a_before_after_label.setText("Time before start, in minutes: ");
			}
			a_time_field.setVisible(true);
		});
		
		a_end_option.setOnAction(e->{
			if(a_end_option.isSelected()) {
				a_before_after_label.setText("Time before end, in minutes: ");
			}
			a_time_field.setVisible(true);
		});
		
		notif_add_popup.setOnCloseRequest(e->{
			a_before_after_label.setText("Please select one of the two options above.");
			a_time_field.setVisible(false);
			a_time_field.setText("");
			a_message_field.setText("");
			a_task_choose_box.getSelectionModel().clearSelection();
			a_start_option.setSelected(false);
			a_end_option.setSelected(false);
		});
		
		a_add_button.setOnAction(e->{
			//Verify inputs are filled.
			if(a_task_choose_box.getSelectionModel().getSelectedItem() == null || a_time_field.getText().isEmpty() 
					|| a_message_field.getText().isEmpty() || !(a_start_option.isSelected() || a_end_option.isSelected())) {
				//Verification failed.
				Alert empty_field_err = new Alert(Alert.AlertType.WARNING);
				empty_field_err.setHeaderText("Empty required fields.");
				empty_field_err.setContentText("Please fill all fields and try again.");
				empty_field_err.show();
				return;
			}
			//Attempt to create a notification. If this fails, blame the user.
			long timeToFire = 0;
			MTask task = a_task_choose_box.getSelectionModel().getSelectedItem();
			try {
				timeToFire = Long.parseLong(a_time_field.getText());
			}catch(Exception err) {
				Alert invalid_err = new Alert(Alert.AlertType.ERROR);
				invalid_err.setHeaderText("Invalid number entered");
				invalid_err.setContentText("A non-number was entered into the time offset field. Please use only integer numbers and try again.");
				invalid_err.show();
				return;
			}
			if(a_start_option.isSelected()) {
				timeToFire = task.getStartDate().getTime() - (timeToFire*60000);
			}else if(a_end_option.isSelected()) {
				timeToFire = task.getEndDate().getTime() - (timeToFire*60000);
			}
			
			MTaskNotification notif = new MTaskNotification(a_task_choose_box.getSelectionModel().getSelectedItem(), timeToFire, a_message_field.getText(), false);
			
			if(timeToFire <= System.currentTimeMillis()) {
				notif.setNotified(true);
				Alert warn_time_before_now = new Alert(Alert.AlertType.WARNING);
				warn_time_before_now.setHeaderText("Time has already occured");
				warn_time_before_now.setContentText("That time has already occured. Therefore, the notification will not go off.");
				warn_time_before_now.show();
			}
			
			task_set.addNotification(task, notif);
			notif_list.getItems().add(notif);
			cancel_notif_button.setDisable(false);
			unsaved_changes = true;
			notif_add_popup.close();
		});
		
		//Event handlers for the notification viewing screen.
		add_notif_button.setOnAction(e->{
			notif_add_popup.show();
		});
		
		notif_list.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<MTaskNotification>() {
               @Override
				public void changed(ObservableValue<? extends MTaskNotification> ov, MTaskNotification old_val, MTaskNotification new_val) {
					selected_notif = new_val;
					cancel_notif_button.setDisable(false);
				}
	    });
		
		cancel_notif_button.setOnAction(e->{
			Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
			confirmation.setHeaderText("Cancel notification?");
			confirmation.setContentText("Are you sure you want to cancel the notification for \"" + selected_notif.getAssignedTaskName() + "\" on " + selected_notif.getDateToNotify() + "?");
			Optional<ButtonType> result = confirmation.showAndWait();
			if(result.isPresent() && result.get().getText().equals("OK")) {
				task_set.cancelNotification(selected_notif);
				notif_list.getItems().remove(selected_notif);
				unsaved_changes = true;
			}
		});
		
		n_autocancel_option.setOnAction(e->{
			notif_autocancel = n_autocancel_option.isSelected();
			if(notif_autocancel) {
				for(MTaskNotification n: task_set.getAllNotifications()) {
					if(n.hasNotified() || (n.getDateToNotify().getTime() < System.currentTimeMillis())) {
						task_set.cancelNotification(n);			
						notif_list.getItems().remove(n);
					}
				}
			}
		});
				
		//Event handlers for the file menu, view menu, and stages
		stage.setOnCloseRequest(e ->{
			if(unsaved_changes) {
				Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
				confirmation.setHeaderText("Quit without saving?");
				confirmation.setContentText("Quit without saving recent changes?");
				Optional<ButtonType> result = confirmation.showAndWait();
				if(!result.isPresent() || result.get().getText().equals("Cancel")) {
					e.consume();
				}
			}
			creation_popup.close();
			edit_popup.close();
		});
		
		close_option.setOnAction(e ->{
			stage.close();
		});
		
		tasks_option.setOnAction(e->{
			if(!contain_pane.getCenter().equals(vb_task)){
				contain_pane.setCenter(vb_task);
			}
		});
		
		notif_option.setOnAction(e->{
			
			notif_list.getItems().setAll(task_set.getAllNotifications());
			
			if(!contain_pane.getCenter().equals(vb_notif)){
				contain_pane.setCenter(vb_notif);
			}
			
			if(notif_autocancel) {
				for(MTaskNotification n: task_set.getAllNotifications()) {
					if(n.hasNotified() || (n.getDateToNotify().getTime() < System.currentTimeMillis())) {
						task_set.cancelNotification(n);
						notif_list.getItems().remove(n);
					}
				}
			}
			
			add_notif_button.setDisable(tasks.isEmpty());
			cancel_notif_button.setDisable(notifs.isEmpty());
		});
		
		save_as_option.setOnAction(e->{
			f_choose_dialog.setTitle("Save as... ");
			ExtensionFilter mtd = new ExtensionFilter("Supported Files", ".mtd");
			f_choose_dialog.setSelectedExtensionFilter(mtd);
			try{
				recentFile = f_choose_dialog.showSaveDialog(stage);
			}catch(Exception err) {
				System.out.println("File does not exist.");
				return;
			}
			if(!recentFile.exists()) {
				try {
					recentFile.createNewFile();
				} catch (IOException e1) {
					System.out.println("File IOException!");
					return;
				}
			}
			if(task_set.save(recentFile)) {
				unsaved_changes = false;
			}
			
		});
		
		save_option.setOnAction(e->{
			if(recentFile == null || !recentFile.exists()) {
				save_as_option.fire();
			}else {
				if(task_set.save(recentFile)) {
					unsaved_changes = false;
				}
			}
		});
		
		load_option.setOnAction(e->{
			f_choose_dialog.setTitle("Load...");
			File file = null;
			try{
				file = f_choose_dialog.showOpenDialog(stage);
			}catch(Exception err) {
				System.out.println("Load failed.");
				return;
			}
			if(file == null) {
				return;
			}
			task_set.load(file);
			recentFile = file;
			task_list.getItems().clear();
			task_list.getItems().addAll(task_set.getTasks());
		});
		
		export_option.setOnAction(e->{
			tsv_choose_dialog.setTitle("Export as TSV...");
			tsv_choose_dialog.setSelectedExtensionFilter(tsv);
			
			if(task_set.getTasks().isEmpty()) {
				Alert data_format_err = new Alert(Alert.AlertType.WARNING);
				data_format_err.setHeaderText("No tasks");
				data_format_err.setContentText("There are no tasks to export.");
				data_format_err.show();
				return;
			}
			
			File tsv_file;
			try{
				tsv_file = tsv_choose_dialog.showSaveDialog(stage);
			}catch(Exception err) {
				System.out.println("File does not exist.");
				return;
			}
			if(tsv_file == null) {
				return;
			}
			if(!tsv_file.exists()) {
				try {
					tsv_file.createNewFile();
				} catch (IOException e1) {
					System.out.println("File IO Exception!");
				}
			}
			task_set.exportAsTSV(tsv_file);
		});
	
		new_option.setOnAction(e->{
			if(unsaved_changes) {
				Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
				confirmation.setHeaderText("Unsaved changes"); //TODO: Improve text
				confirmation.setContentText("Open a new file without saving changes?");
				Optional<ButtonType> result = confirmation.showAndWait();
				if(!result.isPresent() || result.get().getText().equals("Cancel")) {
					e.consume();
					return;
				}
			}
			task_set.clearTasks();
			task_set.cancelAllNotifications();
			task_list.getItems().clear();
			edit_task_button.setDisable(true);
			delete_task_button.setDisable(true);
			unsaved_changes = false;
		});
	}

}
