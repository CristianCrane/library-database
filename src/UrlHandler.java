

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class UrlHandler {

	private static Logger logger;
	
	public static void outputURLinfo(URLConnection uc) throws IOException {
        // Display the URL address, and information about it.
		logger.info("\n" + uc.getURL().toExternalForm() + ":\n" +
				"  Content Type: " + uc.getContentType() + "\n" +
				"  Content Length: " + uc.getContentLength() + "\n" +
				"  Last Modified: " + new Date(uc.getLastModified()) + "\n" +
				"  Expiration: " + uc.getExpiration() + "\n" +
				"  Content Encoding: " + uc.getContentEncoding() + "\n");
	}
	
	public static void setOutputFile(String outputFile) throws SecurityException, IOException {
		// setupLogger 
    	logger = Logger.getLogger("MyLog");    
        FileHandler fh = new FileHandler(outputFile);  
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);
	}
	
	/**
	 * Gets a book from given isbn
	 * @param url the url to get the book from
	 * @return book object populated by info from isbn
	 * @throws Exception
	 */
	public static Book getBook(URL url) throws Exception {
		
		// variables to store book information
		String isbn = "";
		String author = "";
		String title = "";
		String yrPublished = "";
		String publisher = "";
		double price = 0;
		int qty = 0;
		
		try (BufferedReader reader = read(url)) {
		
			String line = reader.readLine();
					
			// title pattern will match title and author
			Pattern titlePattern = Pattern.compile("(?<=>)(.+)(?=<)");
			Pattern publisherPattern = Pattern.compile("(?<=</b>)(.+?)(?=[;|\\(])");
			Pattern yearPublishedPattern = Pattern.compile("[0-9]{4}");
			Pattern isbnPattern = Pattern.compile("[0-9]{10}");
			Pattern pricePattern = Pattern.compile("(?<=\\$)(.+)(?=<)");
			
			boolean foundTitle = false; // flag to know when found first h2 tag
			
			while (line != null) {
				line = line.trim();			
				// the first line with productTitle in the html is always the title of the book, only match first one
				if (line.contains("productTitle")  && !foundTitle){
					Matcher matcher = titlePattern.matcher(line);
					if (matcher.find()){
						title = matcher.group().replaceAll("'", "");
						foundTitle = true;
					}				
				} else if (line.contains("ISBN-10:")){ 
					Matcher matcher = isbnPattern.matcher(line);
					if (matcher.find())
						isbn = matcher.group();
				} else if (line.contains("contributorNameID") || line.contains("field-author")){
					Matcher matcher = titlePattern.matcher(line);
					if (matcher.find())
						author = matcher.group().replaceAll("'", "");
				} else if (line.contains("Publisher:")){
					// amazon has publisher and year on same line so we need two matchers here
					// matches publisher info 
					Matcher publisherMatcher = publisherPattern.matcher(line);
					if (publisherMatcher.find())
						publisher = publisherMatcher.group().trim();
					
					// matches year published info
					Matcher yrPubMatcher = yearPublishedPattern.matcher(line);
					if (yrPubMatcher.find())
						yrPublished = yrPubMatcher.group();
				} else if (line.contains("offer-price")){
					Matcher matcher = pricePattern.matcher(line);
					if (matcher.find())
						price = Double.parseDouble(matcher.group());
				}
				line = reader.readLine();
			}
		}

		Book b = new Book(isbn,author,title,yrPublished,publisher,qty,price);
		return b;
	}

	/**
	 * Gets price info for a book from given URL. Must supply proper Amazon URL from
	 * static method of Book class.
	 * @param url the amazon offer-listing url of a book.
	 * @return the minimum price(index 0), maximum price(index 1), and average price(index 2) found for the book in an arraylist of doubles.
	 * @throws Exception
	 */
	public static ArrayList<Double> getPriceInfo(URL url) throws Exception {
		ArrayList<Double> prices = new ArrayList<Double>();
		double minPrice = Double.MAX_VALUE, maxPrice = Double.MIN_VALUE, priceSum = 0.0;
		int priceCount = 0;
		
		try (BufferedReader reader = read(url)) {
			
			String line = reader.readLine();
					
			Pattern pricePattern = Pattern.compile("(?<=\\$)(.+)(?=<)");
						
			while (line != null) {
				line = line.trim();			
				
				if (line.contains("olpOfferPrice")){
					Matcher matcher = pricePattern.matcher(line);
					if (matcher.find()){
						double price = Double.parseDouble(matcher.group().trim()); 
						
						// find minimum price
						if (price < minPrice){
							minPrice = price;
						}
						
						// find maximum price
						if (price > maxPrice){
							maxPrice = price;
						}
						
						// add to sum and # of books we've seen to calc avg
						priceSum += price;
						priceCount++;
					}
				}
				line = reader.readLine();
			}
			
			// finished reading webpage when we get here, go ahead and calculate avg
			double avgPrice = priceSum/priceCount;
			
			// add values of book to arrayList and return
			prices.add(minPrice);
			prices.add(maxPrice);
			prices.add(avgPrice);
		}
		
		return prices;
	}
	
	public static void saveImgToFile(URL url, String fileName) throws Exception {
		BufferedImage image = ImageIO.read(url);
		Path path = Paths.get(fileName);
		File outputImageFile = path.toFile();
		ImageIO.write(image, "jpg", outputImageFile);
		logger.info("Image saved to: " + fileName + "\n");
	}
	
	public static InputStream getURLInputStream(URL url) throws Exception {
        String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6";
        URLConnection oConnection = url.openConnection(); //call the open connection on the url manually 
        oConnection.setRequestProperty("User-Agent", USER_AGENT); // on the connection object you can call the setRequestProperty (user Agent)
        return oConnection.getInputStream(); // the inputStream is what we actually want to return
  } // getURLInputStream

  public static BufferedReader read(URL url) throws Exception {
        InputStream content = (InputStream)getURLInputStream(url);
        return new BufferedReader (new InputStreamReader(content));
  } // read

}
