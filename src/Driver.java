public class Driver {
	
	public static void main(String[] args){
		DbGui gui;
		
		/* If two args provided, user wants to directly
		 * evaluate a list of isbns and output to a file. */
		if (args.length == 2)
			gui = new DbGui(args[0],args[1]);
																																																																																																																																																																																																											
		/* If one arg was provided, user wants to load
		 * list of isbns to database. */
		else if (args.length == 1)
			gui = new DbGui(args[0]);
		
		/* Else just start up the gui */
		else
			gui = new DbGui();
	}
}
