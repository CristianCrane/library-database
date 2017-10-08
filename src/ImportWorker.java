import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class ImportWorker extends SwingWorker<Integer, String> {

	  private final File txtFile;
	  private final DBManager dbmanager;
	  /** The text area where messages are written. */
	  private final JTextArea output;

	  /**
	   * Creates an instance of the worker
	   * 
	   * @param directory
	   *          the directory containing the list of isbns
	   * @param messagesTextArea
	   *          The text area where messages are written
	   */
	  public ImportWorker(final File directory, final DBManager dbmanager, final JTextArea messagesTextArea) {
	    this.txtFile = directory;
	    this.dbmanager = dbmanager;
	    this.output = messagesTextArea;
	  }

	  @Override
	  protected Integer doInBackground() throws Exception {
		int importCount = 0;
		Path filePath = txtFile.toPath();
	    ArrayList<BookInstance> booksToImport = new ArrayList<BookInstance>();	
		  
		try (InputStream in = Files.newInputStream(filePath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));){
		
			String line = null,isbn,purchaseDate,condition;
			
			// get books from file
			while ((line = reader.readLine()) != null) {
				try {
			    	StringTokenizer t = new StringTokenizer(line,",");
					// book isbn
			        isbn = t.nextToken();
			        // purchase date
					purchaseDate = t.nextToken();
					// condition
					condition = t.nextToken();
					int bCondition = Integer.parseInt(condition);
					
					// first param bookID is 0 because it wont be used anyway. DB auto assigns ID
					BookInstance bi = new BookInstance(0,isbn,purchaseDate,bCondition);
					booksToImport.add(bi);
					
			    } catch (NumberFormatException e) {
			    	System.err.println(e);
			    }    	
			}
		}
		
		// update user with number of books found in file and begin import
		publish("Found " + booksToImport.size() + " book(s) to import:");
		
		for (int i = 0; i < booksToImport.size(); i++){
			// check if process was cancelled
			ImportWorker.failIfInterrupted();
			
			// update status and indicate which book were trying to import
			BookInstance b = booksToImport.get(i);
			publish("Importing book " + (i+1) + "...");
			
			try {
				dbmanager.addBook(b);
				importCount++;
			} catch (SQLException e){
				e.printStackTrace();
			}
			
			// update progress
			setProgress((i+1)*100 / booksToImport.size());
		}
		
		return importCount;
	  }

	  @Override
	  protected void process(final List<String> chunks) {
	    // Updates the messages text area
	    for (final String string : chunks) {
	      output.append(string);
	      output.append("\n");
	    }
	  }
	  
	  private static void failIfInterrupted() throws InterruptedException {
		    if (Thread.currentThread().isInterrupted()) {
		      throw new InterruptedException("Interrupted while importing file");
		    }
	  }
}

