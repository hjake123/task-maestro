import javafx.util.StringConverter;

public class MTaskNameConverter extends StringConverter<MTask> {
	
	//This class is only used to allow a choice box to display MTasks as names. 
	//Therefore, the fromString method will not be well-implemented.
	//Don't use this to create MTasks from Strings.
	
	@Override
	public String toString(MTask mt) {
		return mt.getName();
	}

	@Override
	public MTask fromString(String str) {
		return new MTask(str, null, 0);
	}

}
