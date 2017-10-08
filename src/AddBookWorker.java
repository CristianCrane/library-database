import java.util.List;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class AddBookWorker extends SwingWorker<Book, String> {

	  private BookInstance bi;
	  private DBManager dbmanager;
	  /** The text area where messages are written. */
	  private JTextArea output;

	  /**
	   * Creates an instance of the worker
	   * 
	   * @param directory
	   *          the directory containing the list of isbns
	   * @param messagesTextArea
	   *          The text area where messages are written
	   */
	  public AddBookWorker(final BookInstance bi, final DBManager dbmanager, final JTextArea messagesTextArea) {
	    this.bi = bi;
	    this.dbmanager = dbmanager;
	    this.output = messagesTextArea;
	  }

	  @Override
	  protected Book doInBackground() throws Exception {
		
		  Book b = dbmanager.findBook(bi.getISBN());
		  
		  // if the book isnt in the DB
		  if (!b.exists())
			  // notify user we need to get it from the web
			  publish("ISBN not found. Retrieving info from Amazon...");
		  
		  // dbmanager retrieves book info if necessary
		  dbmanager.addBook(bi);
		  
		  // check if we have the book now
		  b = dbmanager.findBook(bi.getISBN());
		  
		return b;
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

