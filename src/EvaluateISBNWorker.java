import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class EvaluateISBNWorker extends SwingWorker<Integer, String> {

	  private String inputfile;
	  private String outputfile;
	  /** The text area where messages are written. */
	  private JTextArea output;

	  /**
	   * Creates an instance of the worker
	   * @param inputfile textfile containing list of isbns
	   * @param outputfile the textfile to write results to
	   * @param messagesTextArea textarea of the gui to output progress messages
	   */
	  public EvaluateISBNWorker(String inputfile, String outputfile, JTextArea messagesTextArea) {
	    this.inputfile = inputfile;
	    this.outputfile = outputfile;
	    this.output = messagesTextArea;
	  }

	  @Override
	  protected Integer doInBackground() throws Exception {
		
	    // variables needed for I/O
		Hashtable<String,String> isbnToString = new Hashtable<String,String>(); // hashtable to store isbn info so we dont have to look up isbns we've already found
		Hashtable<String,ArrayList<Double>> isbnToPrices = new Hashtable<String,ArrayList<Double>>(); // same as above
		DecimalFormat df = new DecimalFormat("0.00"); // to format prices at end of output
		StringTokenizer t; // need tokenizer to get isbn from line only
		String line = null, isbn, s;
		Charset charset = Charset.forName("US-ASCII");
		Path infile = Paths.get(inputfile), outfile = Paths.get(outputfile);
		
		// variables to keep track of evaluation
		double minSum = 0.0, maxSum = 0.0, avgSum = 0.0;
		int bookCount = 0;
		ArrayList<String> booksToImport = new ArrayList<String>();	
		  
		// get ISBNS from inputfile
		try (InputStream in = Files.newInputStream(infile);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				BufferedWriter writer = Files.newBufferedWriter(outfile, charset)) {
			
			// get all the isbns from input file
			while ((line = reader.readLine()) != null) {
		    	
				// get the isbn from this line only (its first param)
		    	t = new StringTokenizer(line,",");
		    	isbn = t.nextToken();
		    	booksToImport.add(isbn);
			}
			
			// update user with number of isbns found in file and begin import
			publish("Found " + booksToImport.size() + " book(s) to evaluate:");
			
			
			// before we start evaluation, make sure to output column headers first
	    	s = "ISBN | BOOK_TITLE | BOOK_AUTHOR | BOOK_PUBLISHER | YEAR_PUBLISHED | MIN_PRICE | MAX_PRICE | AVG_PRICE\n";
			writer.write(s, 0, s.length());
			
			// start evaluation and output to file
			for (int i = 0; i < booksToImport.size(); i++){
				// check if process was cancelled
				EvaluateISBNWorker.failIfInterrupted();
				
				// update status and indicate which book were evaluating
				isbn = booksToImport.get(i);
				publish("Evaluating book " + (i+1) + "...");
				
				// if we already have info for this isbn, just get it from hashtables
		    	if (isbnToString.containsKey(isbn)){
		    		
		    		// get the string for this book
		    		s = isbnToString.get(isbn);
		    		// write it to file
					writer.write(s, 0, s.length());
					
					// get the prices for this book
					ArrayList<Double> prices = isbnToPrices.get(isbn);
					// update minsum, maxsum, avgsum, and bookcount for evaluation
					minSum += prices.get(0);
					maxSum += prices.get(1);
					avgSum += prices.get(2);
					bookCount++;
		    	}
		    	/* else go to amazon and get book info.
		    	 store book info in hashtable so we dont have to search for books we've already found. */
		    	else {
			    	try {
				    	// now get book info for this isbn
				    	URL url = Book.getUrlByISBN(isbn);
				    	Book b = UrlHandler.getBook(url);
				    	
				    	// now get price info for this isbn
				    	url = Book.getPricePageURL(isbn);
				    	ArrayList<Double> price = UrlHandler.getPriceInfo(url);
				    	
				    	// create output file string
				    	s = b.getIsbn()+" | "+b.getTitle()+" | "+b.getAuthor()+" | "+b.getPublisher()+" | "+b.getYrPublished()+" | $"+df.format(price.get(0))+" | $"+df.format(price.get(1))+" | $"+df.format(price.get(2))+"\n";
				    	// write it to file
						writer.write(s, 0, s.length());
						// save it to hashtable
						isbnToString.put(isbn, s);
						
						// update minsum, maxsum, avgsum, and bookcount for evaluation
						minSum += price.get(0);
						maxSum += price.get(1);
						avgSum += price.get(2);
						bookCount++;
						// save prices for later
						isbnToPrices.put(isbn,price);	
			    	} catch (Exception e) {
			    		e.printStackTrace();
			    	}
				
				// update progress
				setProgress((i+1)*100 / booksToImport.size());
			}
		}
		// after this for loop there are no more isbns to read.
		// so output the evaluation of the file.
		s = "\nBased on prices from Amazon.com, the current library is evaluated at:" + "\n" +
				"   Minimum Value: $" + df.format(minSum) + "\n" +
				"   Maximum Value: $" + df.format(maxSum) + "\n" +
				"   Average Value: $" + df.format(avgSum) + "\n" +
				"   Total Number of books: " + bookCount;
		writer.write(s, 0, s.length());
	}
	return bookCount;
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

