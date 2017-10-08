/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
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
import java.util.StringTokenizer;
import java.util.concurrent.CancellationException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;
 
public class DbGui implements ActionListener, ItemListener {
    JFrame frame;
	JTextArea output;
    JScrollPane scrollPane;
    String newline = "\n";
    DBManager dbmanager;
    JProgressBar progressbar;
    Container contentPane;
    boolean connected = false;
    
    /**
     * Default constructor for DbGui. Called when user doesn't supply any arguments.
     * Sets up the gui and connects to database.
     */
    public DbGui(){
    	// set up gui
		createAndShowGUI();
    	// get login credentials
		output.append("Please enter all database info to connect to the database." + newline);
    	ArrayList<String> loginInfo = getLoginCredentials();
    	
    	try {
        	// connect to db
    		dbmanager = new DBManager(loginInfo.get(0), loginInfo.get(1), loginInfo.get(2), loginInfo.get(3));
    		connected = true;
    		output.append("Connection to " + loginInfo.get(1) + " successfull! You're now connected to the database." + newline);
    		
    		// connection successfull if we got here. check if tables exist here.
    		output.append("Checking if tables exist in the current database..." + newline);
    		int result = dbmanager.checkForTables();
    		if (result == 1) {
    			output.append("Tables found. You're ready to go!" + newline);
    		} else {
    			output.append("Tables not found. Created new tables 'book' and 'book_instance' in " + loginInfo.get(1) + ".\n");
    		}
    		
        	
    	} catch (Exception e) {
    		output.append("Couldn't connect to database. Please check console for details." + newline);
    		e.printStackTrace();
    	}
    	
    }
    
    /**
     * Constructor when inputfile file is provided from command line.
     * Connects to database and automatically imports input file provided.
     * @param inputFile the textfile of isbns to import, one per line.
     */
    public DbGui(String inputFile){
    	// set up gui
		createAndShowGUI();
    	// get login credentials
		output.append("Please enter all database info to connect to the database." + newline);
    	ArrayList<String> loginInfo = getLoginCredentials();
    	
    	try {
        	// connect to db
    		dbmanager = new DBManager(loginInfo.get(0), loginInfo.get(1), loginInfo.get(2), loginInfo.get(3));
    		output.append("Connection to " + loginInfo.get(1) + " successfull! You're now connected to the database." + newline);
    		connected = true;
    		
    		// connection successfull if we got here. check if tables exist here.
    		output.append("Checking if tables exist in the current database..." + newline);
    		int result = dbmanager.checkForTables();
    		
    		if (result == 1) {
    			output.append("Tables found. You're ready to go!" + newline);
    		} else {
    			output.append("Tables not found. Created new tables 'book', 'book_instance', and book_values in " + loginInfo.get(1) + ".\n");
    		}
    		
    		// import from file provided by user
        	output.append("Input file detected. Starting import..."+newline);
        	File inFile = new File(inputFile);
        	importData(inFile);
        	
    	} catch (Exception e) {
    		output.append("Couldn't connect to database. Please check console for details." + newline);
    		e.printStackTrace();
    	}
    	
    	
    }
    
    /**
     * Constructor when inputfile and outputfile are provided from command line.
     * Starts up the gui then evaluates the isbns from the inputfile.
     * Does not connect to the database if this constructor is called.
     * Results written to outputfile.
     * @param inputFile the textfile of isbns to import, one per line.
     * @param outputFile
     */
    public DbGui(String inputFile, String outputFile){
    	createAndShowGUI();
    	// execute this method in new thread
    	File in = new File(inputFile), out = new File(outputFile);
    	evaluateIsbns(in,out);
    }
    
    
    /**
     * Helper method for constructor. Creates menu bar and adds it to frame.
     * @return the created menu bar.
     */
	public JMenuBar createMenuBar() {
        JMenuBar menuBar;
        JMenu menu, submenu;
        JMenuItem menuItem;
 
        //Create the menu bar.
        menuBar = new JMenuBar();
 
        //Build the file menu.
        menu = new JMenu("File");
        menu.getAccessibleContext().setAccessibleDescription(
                "Choose the input and output files for the database");
        menuBar.add(menu);
 
        // JMenuItems for file menu
        menuItem = new JMenuItem("Import textfile to DB");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Evaluate textfile");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        

 
        // build the database menu
        menu = new JMenu("Database");
        menu.getAccessibleContext().setAccessibleDescription(
                "Database interaction options");
        menuBar.add(menu);
        
        // modify database submenu
        submenu = new JMenu("Modify Database");
        menu.add(submenu);
        
        menuItem = new JMenuItem("Add book to database");
        menuItem.addActionListener(this);
        submenu.add(menuItem);
        
        menuItem = new JMenuItem("Delete book from database");
        menuItem.addActionListener(this);
        submenu.add(menuItem);
        
        menuItem = new JMenuItem("Modify book from database");
        menuItem.addActionListener(this);
        submenu.add(menuItem);
        
        
        // Query database submenu
        submenu = new JMenu("Query Database");
        menu.add(submenu);
        
        menuItem = new JMenuItem("List all books in DB");
        menuItem.addActionListener(this);
        submenu.add(menuItem);
        
        menuItem = new JMenuItem("List all copies of book");
        menuItem.addActionListener(this);
        submenu.add(menuItem);
        
        menuItem = new JMenuItem("Find book by ISBN");
        menuItem.addActionListener(this);
        submenu.add(menuItem);
        
        menuItem = new JMenuItem("Evaluate Library");
        menuItem.addActionListener(this);
        submenu.add(menuItem);
        
        return menuBar;
    }
 
	/**
	 * Helper method for constructor. Creates and sets up the content pane for GUI.
	 * @return the content pane for this gui.
	 */
    public Container createContentPane() {
        //Create the content-pane-to-be.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);
 
        //Create a scrolled text area.
        output = new JTextArea(5, 30);
        output.setFont(new Font(Font.MONOSPACED,Font.PLAIN,16));
        output.setEditable(false);
        DefaultCaret caret = (DefaultCaret) output.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPane = new JScrollPane(output);
        new SmartScroller(scrollPane);
 
        //Add the text area to the content pane.
        contentPane.add(scrollPane, BorderLayout.CENTER);
 
        return contentPane;
    }
    
    /**
     * Override method called by actionlistener on the menu. Detects what was clicked
     * and decides what method to call.
     */
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand(), errMessage = "Not connected to a database.";
        
        if (s.equals("Import textfile to DB") && connected)
        		importData(null);
        else if (s.equals("Evaluate textfile"))
        	evaluateIsbns(null,null);
        
        else if (s.equals("Add book to database") && connected)
        	addBook();
        
        else if (s.equals("Delete book from database") && connected)
			deleteBook();
			
		else if (s.equals("Modify book from database") && connected)
			modifyBook();
        
		else if (s.equals("List all books in DB") && connected)
			getAllBooks();
        
		else if (s.equals("Find book by ISBN") && connected)
			findBook();
        
		else if (s.equals("List all copies of book") && connected)
			listAllCopies();
        
		else if (s.equals("Evaluate Library") && connected)
			evaluateLibrary();
		else
    		JOptionPane.showMessageDialog(contentPane, errMessage);
        
    }

    public void itemStateChanged(ItemEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Item event detected."
                   + newline
                   + "    Event source: " + source.getText()
                   + " (an instance of " + getClassName(source) + ")"
                   + newline
                   + "    New state: "
                   + ((e.getStateChange() == ItemEvent.SELECTED) ?
                     "selected":"unselected");
        output.append(s + newline);
        output.setCaretPosition(output.getDocument().getLength());
    }
 
    // Returns just the class name -- no package info.
    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
        return classString.substring(dotIndex+1);
    }
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createAndShowGUI() {
        UIManager.put("Menu.font", new Font(Font.SANS_SERIF,Font.PLAIN,16));

        //Create and set up the window.
        frame = new JFrame("Book Database");
        frame.setSize(1000,800);
        frame.setLocation(500, 100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        frame.setJMenuBar(createMenuBar());
        contentPane = createContentPane();
        frame.setContentPane(contentPane);
        
        //Create and set up the progress bar
        progressbar = new JProgressBar();
        progressbar.setMinimum(0);
        progressbar.setMaximum(100);
        progressbar.setVisible(false);
        contentPane.add(progressbar, BorderLayout.PAGE_START);
 
        //Display the window.
        frame.setVisible(true);
        
    }
  
    /**
     * Helper function for constructor. Sets output file.
     * @return The output file for this gui.
     */
    private File getOutputFile(){
    	File outputFile = null;
    	
    	JFileChooser fc = new JFileChooser();
    	int returnVal = fc.showOpenDialog(scrollPane);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            outputFile = fc.getSelectedFile();
        }  else {
        	JOptionPane.showMessageDialog(scrollPane, "An output file must be set before connecting to the DB. Closing program.");
    		System.exit(1);
        }
        
        return outputFile;
    }
    
    /**
     * Adds a book to the database.
     */
    public void addBook() {
		
    	ArrayList<String> answers = getBookFromUser("Enter new book");
		if(!answers.isEmpty()) {
			try {
				String isbn = answers.get(0);
				String pDate = answers.get(1);
				if (!BookInstance.isValidDate(pDate))
					throw new IllegalArgumentException("Correct date format is MM/DD/YYYY.");
				
				int condition = Integer.parseInt(answers.get(2));
				
				BookInstance bookInstance = new BookInstance(-1,isbn,pDate,condition);
				
				// create new worker thread to do the importing
		        AddBookWorker worker = new AddBookWorker(bookInstance,dbmanager,output);
		        worker.addPropertyChangeListener(new PropertyChangeListener(){
		        	@Override
		            public void propertyChange(final PropertyChangeEvent event) {
		        		switch (event.getPropertyName()) {
		              case "progress":
		                progressbar.setIndeterminate(false);
		                progressbar.setValue((Integer) event.getNewValue());
		                break;
		              case "state":
		                switch ((StateValue) event.getNewValue()) {
		                case DONE:
		                  progressbar.setVisible(false);
		                  //searchCancelAction.putValue(Action.NAME, "Search");
		                  try {
		                    Book b = worker.get();
		                    String message = (b.exists()) ? "Added new copy of:\n"+b.getTitle() : "Couldn't add book.";
		                    JOptionPane.showMessageDialog(DbGui.this.scrollPane, message, "Add Book", JOptionPane.INFORMATION_MESSAGE);
		                  } catch (final CancellationException e) {
		                    JOptionPane.showMessageDialog(DbGui.this.scrollPane, "The import was cancelled", "Add Book",
		                        JOptionPane.WARNING_MESSAGE);
		                  } catch (final Exception e) {
		                    JOptionPane.showMessageDialog(DbGui.this.scrollPane, "Add book failed. Check console for details.", "Add Book",
		                        JOptionPane.ERROR_MESSAGE);
		                    e.printStackTrace();
		                  }

		                  //worker = null;
		                  break;
		                case STARTED:
		                case PENDING:
		                  //searchCancelAction.putValue(Action.NAME, "Cancel");
		                  progressbar.setVisible(true);
		                  progressbar.setIndeterminate(true);
		                  break;
		                }
		                break;
		              }
		            }
		          });
		          worker.execute();

			} catch (Exception e) {
				output.append(e + newline);
				e.printStackTrace();
			}
		}
	}
        
    /**
     * Deletes a book from the database.
     */
    private void deleteBook() {
    	
		String bID = getUserInput("Enter book ID# of book copy to delete from database:");
		
		if (bID != null) {
			int result;
			try {
				// delete it
				result = dbmanager.deleteBook(Integer.parseInt(bID));
				if (result == 1)
					output.append(newline +"Successfully deleted book#" + bID + " from the database." + newline);
				else
					output.append(newline + "Book not found in database." + newline);	
			} catch (Exception e) {
					output.append(e + newline);
					e.printStackTrace();
			}
		}
    }
    
    /**
     * Modifies a book from the database.
     */
    private void modifyBook(){
    	
    	int result;
    	String bID = getUserInput("Enter ID# of book to modify:\n(NOT ISBN)");
    	
    	if (bID != null) {
    		try {
	    		// find out if book_instance table has the book to modify first
	    		BookInstance bi = dbmanager.findBookInstance(Integer.parseInt(bID));
	    		
	    		// if the book exists
	    		if (bi.exists()) { 
	    			output.append(newline + "Book found. Enter modifications..." + newline);
	    			ArrayList<String> answers = getBookModifications("Modifications");
	    			
	    			// and if they gave us some modifications
	    			if (!answers.isEmpty()){
	    				
	    				// go ahead and make changes
	    				int id = Integer.parseInt(bID);
	    				String purchaseDate = answers.get(0);
	    				String condition = answers.get(1);
	    				
	    				// if user wants to change purchaseDate
	    				if (!purchaseDate.equals("")){
	    					
	    					// and the date is valid
	    					if (BookInstance.isValidDate(purchaseDate)){
	    						try {
			    					result = dbmanager.updatePurchaseDate(id, purchaseDate);
			    					String message = (result == 1) ? "Successfully updated purchase date." : "Update failed." ;
			    					output.append(newline + message + newline);
		    					} catch (Exception e) {
		    						output.append(newline + e.toString() + newline);
		    						e.printStackTrace();
		    					}
	    					} else { // else the date is invalid
	    						output.append(newline + "Couldn't update purchase date: invalid date." + newline);
	    					}
	    					
	    				}
	    				
	    				// if user wants to change condition
	    				if (!condition.equals("")){	    					
	    					try {
		    					result = dbmanager.updateBookCondition(id, Integer.parseInt(condition));
		    					String message = (result == 1) ? "Successfully updated condition." : "Update failed." ;
		    					output.append(newline + message + newline);
	    					} catch (Exception e) {
	    						if (e instanceof NumberFormatException)
	    							output.append(newline + "Couldn't update condition: must be a number!");
	    						else
	    							output.append(newline + e.toString() + newline);
	    						e.printStackTrace();
	    					}
	    				}	    			
	    			}
	    			
	    		} else { // else we didn't find the book 
	    			output.append(newline+"Book not found."+newline);
	    		}
	    		
    		} catch (Exception e) {
    			output.append(newline + e.toString() + newline);
    			e.printStackTrace();
    		}
		}
    }

    /**
     * Prints all books from the database.
     */
    private void getAllBooks(){
    	
    	try {
    		ArrayList<Book> books = dbmanager.getAllBooks();
    		if (books.isEmpty())
    			output.append(newline + "There are no books in the database." + newline);
    		else {
    			output.append(newline + "All books in database: " + newline + newline);
    			for (Book b : books)
    				output.append(b + newline);
    		}
    	} catch (Exception e) {
    		output.append(e + newline);
    		e.printStackTrace();
    	}
    }
    
    /**
     * finds a book from the database.
     */
    public void findBook() {
    	
    	String isbn = getUserInput("Enter isbn of book to find");
    	
    	if (isbn == null)
			output.append("No isbn given." + newline);
    	else {
    		try {
    			Book book = dbmanager.findBook(isbn);
    			if (book.exists())
    				output.append(newline + "Book found: " + newline + newline + book + newline);
        		else { 
        			output.append("Item not found in database." + newline);

        		}
        	} catch (Exception e) {
        		output.append(e + newline);
        		e.printStackTrace();
        	}
    	}
    }
    
    /**
     * Prints all bookInstances from the database.
     */
    public void listAllCopies() {
    	// get isbn to check from user
    	String isbn = getUserInput("Enter isbn of book:");
    	
    	if (isbn == null)
			output.append(newline + "No isbn given." + newline);
    	else {
    		// find book instances with that isbn
    		ArrayList<BookInstance> result;
    		try {
    			result = dbmanager.findBookInstances(isbn);
	    		if (result.isEmpty())
	    			output.append(newline + "No books with that isbn in the database." + newline);
	    		else {
	    			// show instances to user
	    			Book b = dbmanager.findBook(isbn);
	    			if (b.exists()){
	    				output.append(newline + result.size() + " copies of "+b.getTitle()+" found:" + newline + newline);
		    			for (BookInstance bi : result)
		    				output.append(bi.toString() + newline);
	    			}
	    				    		} 
    		} catch (Exception e){
    			output.append(e + newline);
    			e.printStackTrace();
    		}
    	}
    }
        
    /**
     * Imports list of books from a textfile to the database in a new thread.
     * @param inputFile the textfile containing each bookInstance, one per line.
     */
    public void importData(File inputFile){
    	
    	// make sure we have a file to work with
    	if (inputFile == null) {
    		output.append("Please choose a file to import..." + newline);
    		JFileChooser fc = new JFileChooser();
        	int returnVal = fc.showOpenDialog(scrollPane);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                inputFile = fc.getSelectedFile();
            }  else {
                return;
            }
    	}
    	
    	// if we get here then we have a file to work with
        output.append("Opening: " + inputFile.getName() + "..." + newline);
        
        
        // create new worker thread to do the importing
        ImportWorker worker = new ImportWorker(inputFile,dbmanager,output);
        
        worker.addPropertyChangeListener(new PropertyChangeListener(){
        	@Override
            public void propertyChange(final PropertyChangeEvent event) {
        		switch (event.getPropertyName()) {
              case "progress":
                progressbar.setIndeterminate(false);
                progressbar.setValue((Integer) event.getNewValue());
                break;
              case "state":
                switch ((StateValue) event.getNewValue()) {
                case DONE:
                  progressbar.setVisible(false);
                  //searchCancelAction.putValue(Action.NAME, "Search");
                  try {
                    final int count = worker.get();
                    JOptionPane.showMessageDialog(DbGui.this.scrollPane, "Imported: " + count + " books", "Import", JOptionPane.INFORMATION_MESSAGE);
                  } catch (final CancellationException e) {
                    JOptionPane.showMessageDialog(DbGui.this.scrollPane, "The import was cancelled", "Import",
                        JOptionPane.WARNING_MESSAGE);
                  } catch (final Exception e) {
                    JOptionPane.showMessageDialog(DbGui.this.scrollPane, "The import process failed", "Import",
                        JOptionPane.ERROR_MESSAGE);
                  }

                  //worker = null;
                  break;
                case STARTED:
                case PENDING:
                  //searchCancelAction.putValue(Action.NAME, "Cancel");
                  progressbar.setVisible(true);
                  progressbar.setIndeterminate(true);
                  break;
                }
                break;
              }
            }
          });
          worker.execute();   
    }

    /**
     * Evaluates list of isbns in a text file. Outputs results to specified output txtfile.
     * @param inputfile the txtfile containing list of isbns, one per line
     * @param outputfile txtfile to output results to
     */
    public void evaluateIsbns(File inputfile, File outputfile) {

    	
    	// make sure we have an inputfile to work with
    	if (inputfile == null) {
    		output.append("Please choose a file to evaluate..." + newline);
    		JFileChooser fc = new JFileChooser();
        	int returnVal = fc.showOpenDialog(scrollPane);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                inputfile = fc.getSelectedFile();
            }  else {
                return;
            }
    	}
    	
    	// make sure we have an outputfile to work with
    	if (outputfile == null) {
    		output.append("Please choose an output file to write results..." + newline);
    		JFileChooser fc = new JFileChooser();
        	int returnVal = fc.showOpenDialog(scrollPane);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                outputfile = fc.getSelectedFile();
            }  else {
                return;
            }
    	}
    	
    	// if we get here then we have files to work with
        output.append("Opening: " + inputfile.getName() + "..." + newline);
        
        
        // create new worker thread to do the evaluation
        EvaluateISBNWorker worker = new EvaluateISBNWorker(inputfile.toString(),outputfile.toString(),output);
        
        worker.addPropertyChangeListener(new PropertyChangeListener(){
        	@Override
            public void propertyChange(final PropertyChangeEvent event) {
        		switch (event.getPropertyName()) {
              case "progress":
                progressbar.setIndeterminate(false);
                progressbar.setValue((Integer) event.getNewValue());
                break;
              case "state":
                switch ((StateValue) event.getNewValue()) {
                case DONE:
                  progressbar.setVisible(false);
                  //searchCancelAction.putValue(Action.NAME, "Search");
                  try {
                    final int count = worker.get();
                    JOptionPane.showMessageDialog(DbGui.this.scrollPane, "Evaluated: " + count + " isbns.\nCheck output file for results.", "Evaluation", JOptionPane.INFORMATION_MESSAGE);
                  } catch (final CancellationException e) {
                    JOptionPane.showMessageDialog(DbGui.this.scrollPane, "The evaluation was cancelled", "Import",
                        JOptionPane.WARNING_MESSAGE);
                  } catch (final Exception e) {
                    JOptionPane.showMessageDialog(DbGui.this.scrollPane, "The evaluation process failed", "Import",
                        JOptionPane.ERROR_MESSAGE);
                  }

                  //worker = null;
                  break;
                case STARTED:
                case PENDING:
                  //searchCancelAction.putValue(Action.NAME, "Cancel");
                  progressbar.setVisible(true);
                  progressbar.setIndeterminate(true);
                  break;
                }
                break;
              }
            }
          });
          worker.execute();
	}
    
    /**
     * Evaluates current value of library contained in Database.
     * Outputs the minimum, maximum, and average evaluation of the library to the gui.
     */
    public void evaluateLibrary(){
    	
    	try {
    		DecimalFormat df = new DecimalFormat("0.00");
    		ArrayList<Double> evaluations = dbmanager.evaluateLibrary();
 
			output.append(newline + "Based on prices from Amazon.com, the current library is evaluated at:" + newline +
					"Minimum Value: $" + df.format(evaluations.get(0)) + newline +
					"Maximum Value: $" + df.format(evaluations.get(1)) + newline +
					"Average Value: $" + df.format(evaluations.get(2)) + newline);
		 
    	} catch (Exception e) {
    		output.append(newline + e.getMessage() + newline);
    		e.printStackTrace();
    	}
    }
    
    /**
     * Helper function for constructor. Gets DB login info from DB.
     * @return The login info to connect to the DB
     */
    public ArrayList<String> getLoginCredentials(){
    	String[] labels = {"URL: ", "Database Name: ", "Username: ", "Password: "};
    	ArrayList<JTextField> textfields = new ArrayList<JTextField>();
    	ArrayList<String> answers = new ArrayList<String>();
    	int numPairs = labels.length;

    	//Create and populate the panel.
    	JPanel p = new JPanel(new SpringLayout());
    	for (int i = 0; i < numPairs; i++) {
    	    JLabel l = new JLabel(labels[i], JLabel.TRAILING);
    	    p.add(l);
    	    JTextField textField = new JTextField(10);
    	    l.setLabelFor(textField);
    	    p.add(textField);
    	    textfields.add(textField);
    	}

    	//Lay out the panel.
    	makeCompactGrid(p,
    	                                numPairs, 2, //rows, cols
    	                                6, 6,        //initX, initY
    	                                6, 6);       //xPad, yPad
    	
    	int result = JOptionPane.showConfirmDialog(scrollPane,p,"DB Login info",JOptionPane.OK_CANCEL_OPTION);
    	if (result == JOptionPane.CANCEL_OPTION) {
    		JOptionPane.showMessageDialog(scrollPane, "No info given. Closing program.");
    		System.exit(1);
    	}
		for (JTextField tf : textfields) {
			answers.add(tf.getText());
    	}
    		
    	return answers;
    }

    /**
     * Method to get input from user.
     * @param message The message to prompt user for input.
     * @return the input from user.
     */
    public String getUserInput(String message){
    	return JOptionPane.showInputDialog(scrollPane, message);
    }
    
    /**
     * Method to get new book from user.
     * Gets isbn, purchase date, and book condition from user.
     * @param displayTitle Title to display.
     * @return isbn, purchase date, book condition as strings in an arrayList
     */
    public ArrayList<String> getBookFromUser(String displayTitle){
    	String[] labels = {"Book ISBN: ", "Purchase date (MM/DD/YYYY): ", "Book Condition (0-10)"};
    	ArrayList<JTextField> textfields = new ArrayList<JTextField>();
    	ArrayList<String> answers = new ArrayList<String>();
    	int numPairs = labels.length;

    	//Create and populate the panel.
    	JPanel p = new JPanel(new SpringLayout());
    	for (int i = 0; i < numPairs; i++) {
    	    JLabel l = new JLabel(labels[i], JLabel.TRAILING);
    	    p.add(l);
    	    JTextField textField = new JTextField(10);
    	    l.setLabelFor(textField);
    	    p.add(textField);
    	    textfields.add(textField);
    	}

    	//Lay out the panel.
    	makeCompactGrid(p,
    	                                numPairs, 2, //rows, cols
    	                                6, 6,        //initX, initY
    	                                6, 6);       //xPad, yPad
    	int result = JOptionPane.showConfirmDialog(scrollPane,p,displayTitle,JOptionPane.OK_CANCEL_OPTION);
    	
    	if (result == JOptionPane.OK_OPTION){
    		for (JTextField tf : textfields) {
    			answers.add(tf.getText());
    		}
    	}
    		
    	return answers;

    }

    /**
     * Gets modifications from user via JPanel.
     * @param displayTitle The title of the Jpanel
     * @return users modifications. if they didn't modify a field it will be empty string
     */
    public ArrayList<String> getBookModifications(String displayTitle){
    	String[] labels = {"Purchase date (MM/DD/YYYY): ", "Book Condition (0-10)"};
    	ArrayList<JTextField> textfields = new ArrayList<JTextField>();
    	ArrayList<String> answers = new ArrayList<String>();
    	int numPairs = labels.length;

    	//Create and populate the panel.
    	JPanel p = new JPanel(new SpringLayout());
    	for (int i = 0; i < numPairs; i++) {
    	    JLabel l = new JLabel(labels[i], JLabel.TRAILING);
    	    p.add(l);
    	    JTextField textField = new JTextField(10);
    	    l.setLabelFor(textField);
    	    p.add(textField);
    	    textfields.add(textField);
    	}

    	//Lay out the panel.
    	makeCompactGrid(p,numPairs, 2,6, 6, 6, 6);
    	int result = JOptionPane.showConfirmDialog(scrollPane,p,displayTitle,JOptionPane.OK_CANCEL_OPTION);
    	
    	if (result == JOptionPane.OK_OPTION){
    		for (JTextField tf : textfields) {
    			answers.add(tf.getText());
    		}
    	}
    		
    	return answers;

    }
    
    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public static void makeCompactGrid(Container parent,
                                       int rows, int cols,
                                       int initialX, int initialY,
                                       int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }
 
        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                                   getConstraintsForCell(r, c, parent, cols).
                                       getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }
 
        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height,
                                    getConstraintsForCell(r, c, parent, cols).
                                        getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }
 
        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
    
    /* Used by makeCompactGrid. */
    private static SpringLayout.Constraints getConstraintsForCell(
                                                int row, int col,
                                                Container parent,
                                                int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }
    
    public void print(String s){
    	output.append(s + newline);
    }
}
