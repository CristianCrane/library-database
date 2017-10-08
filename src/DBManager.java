import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DBManager {

	private Connection connection;
    private File outputFile;
    private Logger logger;
    private FileHandler fh;
    // database url and login credentials
    private String dbName;
	private String url;
    
	/**
	 * Constructs a new DBManager.
	 * @param url the url of the database to connect to.
	 * @param dbName name of the database at the URL
	 * @param username database login credential
	 * @param password database login credential
	 * @param outputFile the file to create logs to when interacting with the database
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws IOException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
    public DBManager(String url, String dbName, String username, String password) throws Exception {
    	this.url = url;
    	this.dbName = dbName;
    	this.connection = DriverManager.getConnection(url, username, password);
    	this.outputFile = new File("myLogFile.log");
    	setupLogger();
    }
	
    /**
     * Instantiates logger for dbmanager, sets outputfile and formatting for log file.
     * @throws SecurityException
     * @throws IOException
     */
    private void setupLogger() throws SecurityException, IOException {
    	logger = Logger.getLogger("MyLog");    
        fh = new FileHandler(outputFile.toString());  
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);   
    }

    /**
     * Checks database for necessary tables. Creates tables if not found.
     * @return Returns 1 if tables existed, 0 if they didn't.
     * @throws SQLException
     */
    public int checkForTables() throws SQLException {
    	DatabaseMetaData md;
    	ResultSet rs;
    	int result = 1;
    	
    	// check if books table exists
		md = connection.getMetaData();
		rs = md.getTables(null, null, "books", null);
		// if rs isn't empty table exists.
		if (!rs.next()){
			Statement stmt = null;
			try {
				String sql = "CREATE TABLE " + dbName+".books ("+
						"ISBN varchar(30) NOT NULL,"+
						"BOOK_AUTHOR varchar(100) NOT NULL,"+
						"BOOK_TITLE varchar(200) NOT NULL,"+
						"YEAR_PUBLISHED varchar(100) NOT NULL,"+
						"PUBLISHER varchar(100) NOT NULL,"+
						"QUANTITY int NOT NULL,"+
						"BOOK_PRICE double(5,2) NOT NULL,"+
						"PRIMARY KEY (ISBN));";
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);
			} finally {
				if (stmt != null)
					stmt.close();
			}
			result = 0;
		}
		
		// check if book_prices table exists
		md = connection.getMetaData();
		rs = md.getTables(null, null, "book_prices", null);
		// if the table doesn't exist
		if (!rs.next()){
			// create it
			Statement stmt = null;
			try {
				String sql = "CREATE TABLE " + dbName+".book_prices ("+
						"ISBN varchar(30) NOT NULL,"+
						"MIN_PRICE double(5,2) NOT NULL,"+
						"MAX_PRICE double(5,2) NOT NULL,"+
						"AVG_PRICE double(5,2) NOT NULL,"+
						"PRIMARY KEY (ISBN),"+
						"FOREIGN KEY (ISBN) REFERENCES books(ISBN));";
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);
			} finally {
				if (stmt != null)
					stmt.close();
			}
			result = 0;
		}
		
		
		// check if book_instance table exists
		md = connection.getMetaData();
		rs = md.getTables(null, null, "book_instance", null);
		if (!rs.next()){
			Statement stmt = null;
			try {
				String sql = "CREATE TABLE " + dbName+".book_instance ("+
						"BOOK_ID int NOT NULL AUTO_INCREMENT,"+
						"ISBN varchar(30) NOT NULL,"+
						"PURCHASE_DATE varchar(30) NOT NULL,"+
						"BOOK_CONDITION int NOT NULL,"+
						"PRIMARY KEY (BOOK_ID),"+
						"FOREIGN KEY (ISBN) REFERENCES books(ISBN));";
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);
			} finally {
				if (stmt != null)
					stmt.close();
			}
			result = 0;
		}
		return result;
    }
     
    /**
     * Finds book by ISBN. Returns book object. If not found, isbn of returned book object will be null.
     * @param isbn the ISBN of book to find in books table
     * @return the book as a book object
     * @throws SQLException
     */
    public Book findBook(String isbn) throws SQLException {
    	Book b = new Book();
    	Statement stmt = null;
    	ResultSet rs = null;
    	try {
    		stmt = connection.createStatement();
    		rs = stmt.executeQuery("SELECT * FROM "+dbName+".books WHERE ISBN='"+isbn+"';");
    		
    		while(rs.next()){
    			b.setIsbn(rs.getString("ISBN"));
    			b.setAuthor(rs.getString("BOOK_AUTHOR"));
    			b.setTitle(rs.getString("BOOK_TITLE"));
    			b.setYrPublished(rs.getString("YEAR_PUBLISHED"));
    			b.setPublisher(rs.getString("PUBLISHER"));
    			b.setQty(rs.getInt("QUANTITY"));
    			b.setPrice(rs.getDouble("BOOK_PRICE"));
    		}
    		
    	} finally {
    		if (stmt != null)
    			stmt.close();
    	}
    	return b;
    }

    /**
     * Finds bookInstance by BOOK_ID. Returns bookInstance object.
     * If not found, bookID of returned bookInstance object will be -1.
     * @param bID the ID# of bookInstance to find in book_instance table
     * @return the book as a bookInstance object
     * @throws SQLException
     */
    public BookInstance findBookInstance(int bID) throws SQLException {
    	BookInstance b = new BookInstance();
    	Statement stmt = null;
    	ResultSet rs = null;
    	try {
    		stmt = connection.createStatement();
    		rs = stmt.executeQuery("SELECT * FROM "+dbName+".book_instance WHERE BOOK_ID="+bID+";");
    		
    		while(rs.next()){
    			b.setBookID(rs.getInt("BOOK_ID"));
    			b.setISBN(rs.getString("ISBN"));
    			b.setPurchaseDate(rs.getString("PURCHASE_DATE"));
    			b.setCondition(rs.getInt("BOOK_CONDITION"));
    		}
    		
    	} finally {
    		if (stmt != null)
    			stmt.close();
    	}
    	return b;
    }
    
    /**
     * Finds all bookInstances by isbn. Returns all bookInstances in an arrayList
     * @param isbn the isbn of bookInstances to return
     * @return arrayList of bookInstances with given isbn
     * @throws SQLException
     */
    public ArrayList<BookInstance> findBookInstances(String isbn) throws SQLException {
    	ArrayList<BookInstance> books = new ArrayList<BookInstance>();
    	Statement stmt = null;
    	ResultSet rs = null;
    	try {
    		stmt = connection.createStatement();
    		rs = stmt.executeQuery("SELECT * FROM "+dbName+".book_instance WHERE ISBN='"+isbn+"';");
    		
    		while(rs.next()){
    	    	BookInstance b = new BookInstance();
    			b.setBookID(rs.getInt("BOOK_ID"));
    			b.setISBN(rs.getString("ISBN"));
    			b.setPurchaseDate(rs.getString("PURCHASE_DATE"));
    			b.setCondition(rs.getInt("BOOK_CONDITION"));
    			
    			books.add(b);
    		}
    		
    	} finally {
    		if (stmt != null)
    			stmt.close();
    	}
    	return books;
    }
    
    /**
     * Adds new bookInstance to the database.
     * Will automatically generate book info by searching internet if book doesn't exist.
     * @param bi the bookInstance to add
     * @return 1 if add was successful, 0 if book wasn't added.
     * @throws Exception
     */
    public int addBook(BookInstance bi) throws Exception {
    	Statement stmt = null, stmt2 = null;
    	int result;
    	String isbn = bi.getISBN();
		
    	// check if we have book info
    	try {
			Book b = findBook(isbn);
			// if we dont have the book info
			if (!b.exists()){
				
				// get bibliographic info from amazon product page
				URL url = Book.getUrlByISBN(isbn);
				b = UrlHandler.getBook(url);
				
				// get price info from amazon offer-listing page
				url = Book.getPricePageURL(isbn);
				ArrayList<Double> prices = UrlHandler.getPriceInfo(url);
				
				// some books have empty offer-listing page, check if we actually got some prices back
				if (prices.contains(Double.NaN)){
					// get rid of junk values
					prices.clear();
					// set min, max, and avg values to price retrieved from default product page
					for (int i = 0; i < 3; i++) {
						prices.add(b.getPrice());
					}
				}
				
				
				// insert new book into books table
				stmt = connection.createStatement();
				result = stmt.executeUpdate("insert into " + dbName + ".books values('"+
						b.getIsbn() + "','" +
						b.getAuthor() + "','" +
						b.getTitle() + "','" +
						b.getYrPublished() + "','" +
						b.getPublisher() + "'," +
						0 + "," +
						prices.get(2) +");"); // use average price as default price for book (index 2) 		
				
				logger.info("New Book added:\n" + b);	
				stmt.close();
				
				// if book addition successful, update book_prices table
				if (result == 1){
					// insert new price evaluation into book_prices table
					stmt = connection.createStatement();
					result = stmt.executeUpdate("insert into "+dbName+".book_prices values('"+
					b.getIsbn()+"',"+prices.get(0)+","+prices.get(1)+","+prices.get(2)+");");
					stmt.close();
				}
			}
			
			
			// now insert book instance to book_instance table if book exists
			if (b.exists()){
				stmt = connection.createStatement();
				result = stmt.executeUpdate("insert into " + dbName + ".book_instance (BOOK_ID,ISBN,PURCHASE_DATE,BOOK_CONDITION) " +
											"VALUES(NULL,'"+ bi.getISBN()+"','"+bi.getPurchaseDate()+"',"+bi.getCondition()+");");			
				// update quantity in books table because added new book_instance
				if (result == 1){
					logger.info("Added new copy of " + b.getTitle());
					stmt2 = connection.createStatement();
					stmt.executeUpdate("UPDATE " + dbName + ".books SET QUANTITY=QUANTITY+1 WHERE ISBN='"+isbn+"';");   			
				}
			} else {
				result = 0;
			}
    	} finally {
    		if (stmt != null)
    			stmt.close();
    		if (stmt2 != null)
    			stmt2.close();
    	}
    	return result;
    }
    
    /**
     *  Deletes a book instance from the book_instance table. returns 1 for successful delete, 0 otherwise.
     * @param bID the book ID# of the book to delete
     * @return Returns 1 for successful delete, 0 for unsuccessful (not found).
     * @throws SQLException
     */
    public int deleteBook(int bID) throws SQLException {
    	
    	Statement stmt = null,stmt2 = null;
    	BookInstance bi = null;
    	int result;
    	
    	// try to delete book from book_instance table
    	try {
    		
    		// get bookInstance from DB to know its isbn
    		bi = findBookInstance(bID);
    		
    		// now delete it
    		stmt = connection.createStatement();
    		result = stmt.executeUpdate("DELETE FROM "+dbName+".book_instance WHERE BOOK_ID = " + bID + ";");

    		// if deletion successful log the delete and update books table quantity
    		if (result == 1){
    			logger.info("Deleted book #"+bID+" from book_instance table.");
    			String isbn = bi.getISBN();
    			stmt2 = connection.createStatement();
    			result = stmt2.executeUpdate("UPDATE " + dbName + ".books SET QUANTITY=QUANTITY-1 WHERE ISBN='"+isbn+"';");
    		}
    		
    	} finally {
			if (stmt != null)
				stmt.close();
			if (stmt2 != null)
				stmt2.close();
		}

    	return result;
    }
    
    /**
     * Get all books from books table. Returns books found in a list.
     * @return The list of books found as an ArrayList<Book>
     * @throws SQLException
     */
    public ArrayList<Book> getAllBooks() throws SQLException {
    	Statement stmt = null;
    	ResultSet rs = null;
    	ArrayList<Book> books = new ArrayList<Book>();
    	try {
    		stmt = connection.createStatement();
    		rs = stmt.executeQuery("SELECT * FROM "+dbName+".books;");
    		
    		while(rs.next()){
    			Book b = new Book(
    					rs.getString("ISBN"),
    					rs.getString("BOOK_AUTHOR"),
    					rs.getString("BOOK_TITLE"),
    					rs.getString("YEAR_PUBLISHED"),
    					rs.getString("PUBLISHER"),
    					rs.getInt("QUANTITY"),
    					rs.getDouble("BOOK_PRICE"));
    			books.add(b);
    		}
    		
    	} finally {
    		if (stmt != null)
    			stmt.close();
    	}
    	return books;
    }
        
    /**
     * Updates condition of book in book_instance table.
     * @param bID the id# of book in book_instance table.
     * @param updateValue the value to change condition to
     * @return 1 if successful update, 0 if bookInstance not found
     * @throws SQLException
     */
    public int updateBookCondition(int bID, int updateValue) throws SQLException {
    	
		Statement stmt = null;
		int result;
		
		try {
			stmt = connection.createStatement();
			result = stmt.executeUpdate("UPDATE "+dbName+".book_instance SET BOOK_CONDITION="+updateValue+" WHERE BOOK_ID="+bID+";");
			logger.info("Updated condition of book "+ bID +" to "+ updateValue);
		} finally {
			if (stmt != null)
				stmt.close();
		}
		
		return result;
    }

    /**
     * Updates purchase date of book in book_instance table.
     * @param bID the id# of book in book_instance table.
     * @param updateValue the value to change purchase_date to
     * @return 1 if successful update, 0 if bookInstance not found
     * @throws SQLException
     */
    public int updatePurchaseDate(int bID, String updateValue) throws SQLException {
    	
		Statement stmt = null;
		int result;
		
		try {
			stmt = connection.createStatement();
			result = stmt.executeUpdate("UPDATE "+dbName+".book_instance SET PURCHASE_DATE='"+updateValue+"' WHERE BOOK_ID="+bID+";");
			logger.info("Updated purchase date of book "+ bID +" to "+ updateValue);
		} finally {
			if (stmt != null)
				stmt.close();
		}
		
		return result;
    }

    /**
     * Evaluates value of inventory in library.
     * @return ArrayList<Double> containing the min evaluation(index 0), max eval(index 1), and avg eval(index 2)
     */
    public ArrayList<Double> evaluateLibrary() throws Exception {
    	ArrayList<Double> evaluations = new ArrayList<Double>();
    	
    	try (Statement stmt = connection.createStatement()) {
    		String sql = "select sum(bp.min_price * b.quantity), sum(bp.max_price * b.quantity), sum(bp.avg_price * b.quantity) " +
    			    "from "+dbName+".books b, "+dbName+".book_prices bp "+
    			    "where b.isbn = bp.isbn;";
    		ResultSet rs = stmt.executeQuery(sql);
    		
    		while (rs.next()){
    			evaluations.add(rs.getDouble(1));
    			evaluations.add(rs.getDouble(2));
    			evaluations.add(rs.getDouble(3));
    		}
    	}
    	return evaluations;
    }
    /* getters and setters */
    
    public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public FileHandler getFh() {
		return fh;
	}

	public void setFh(FileHandler fh) {
		this.fh = fh;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	
    
    
}
