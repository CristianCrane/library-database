
public class BookInstance {

	private int bookID;
	private String isbn;
	private String purchaseDate;
	private int condition;
	
	// empty constructor
	public BookInstance(){
		this.bookID = -1;
		this.isbn = null;
		this.purchaseDate = null;
		this.condition = -1;
	}
	
	// non empty constructor
	public BookInstance(int bookID, String isbn, String purchaseDate, int condition){
		this.bookID = bookID;
		this.isbn = isbn;
		this.purchaseDate = purchaseDate;
		this.condition = condition;
	}
	
	//book ID methods
	public int getBookID() {
		return bookID;
	}
	public void setBookID(int bookID) {
		this.bookID = bookID;
	}
	public boolean idIsEqual(int id) {
		return (this.bookID == id);
	}
	
	//isbn methods
	public String getISBN() {
		return isbn;
	}
	public void setISBN(String isbn) {
		this.isbn = isbn;
	}
	public boolean isbnIsEqual(String isbn){
		return this.isbn == isbn;
	}
	
	//date methods
	public String getPurchaseDate() {
		return purchaseDate;
	}
	public void setPurchaseDate(String date) {
		this.purchaseDate = date;
	}
	public boolean dateIsEqual(String date) {
		return this.purchaseDate.equals(date);
	}
	public static boolean isValidDate(String date) {
		return date.matches("(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d)");
	}
	
	// condition methods
	public int getCondition(){
		return condition;
	}
	public void setCondition(int condition){
		this.condition = condition;
	}
	public boolean conditionIsEqual(int condition){
		return this.condition == condition;
	}
	
	public boolean exists(){
		return bookID != -1;
	}
	
	// to string
	public String toString(){
		String s = "";
		s += "Book ID: "+ bookID + "\n";
		s += "Book ISBN: "+ isbn + "\n";
		s += "Purchase Date: "+ purchaseDate + "\n";
		s += "Condition: "+ condition + "/10 \n";
		return s;
	}
}
