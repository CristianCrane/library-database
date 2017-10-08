import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

public class Book {
	
	private String isbn;
	private String author;
	private String title;
	private String yrPublished;
	private String publisher;
	private int qty;
	private double price;
	
	//empty constructor
	public Book(){
		isbn = null;
		author = null;
		title = null;
		yrPublished = null;
		publisher = null;
		qty = 0;
		price = 0;
	}
	
	//args constructor
	public Book(String isbn,String author,String title,String yrPublished,String publisher,int qty,double price){
		this.isbn = isbn;
		this.author = author;
		this.title = title;
		this.yrPublished = yrPublished;
		this.publisher = publisher;
		this.qty = qty;
		this.price = price;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	
	public boolean exists(){
		return this.isbn != null;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getYrPublished() {
		return yrPublished;
	}

	public void setYrPublished(String yrPublished) {
		this.yrPublished = yrPublished;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}
	
	public double getPrice(){
		return price;
	}
	
	public void setPrice(double price){
		this.price = price;
	}
	
	public static URL getUrlByISBN(String isbn) throws MalformedURLException {
		String urlstring = "https://www.amazon.com/dp/"+ isbn;
		return new URL(urlstring);
	};
	
	/**
	 * Gets the amazon offer-listing page URL for this book
	 * @param isbn the isbn of the book to search
	 * @return returns the amazon offer-listing page url
	 * @throws MalformedURLException
	 */
	public static URL getPricePageURL(String isbn) throws MalformedURLException {
		String urlstring = "https://www.amazon.com/gp/offer-listing/"+ isbn +"/ref=olp_page_1?ie=UTF8&f_used=true&f_usedLikeNew=true&f_usedVeryGood=true#nav-top";
		return new URL(urlstring);
	}
	
	// to string
		public String toString(){
			String s = "";
			s += title + "\n";
			s += "ISBN: "+ isbn + "\n";
			s += "Author: "+ author + "\n";
			s += "Publisher: "+ publisher + "\n";
			s += "Year Published: "+ yrPublished + "\n";
			s += "Quantity: " + qty + "\n";
			s += "Price: $" + new DecimalFormat("#.00").format(price) + "\n";
			return s;
		}
}
