import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class bookGenerator {

	public static void main (String[] args){
		
		Random random = new Random();
		
		String isbn,date,condition;
		String[] isbns = {
				"0451205367",
				"1585424331",
				"0671027034",
				"0553562835",
				"0743235533",
				"0671739166",
				"0199291152",
				"0205992129",
				"1578647452",
				"0316219282",
				"0062301233",
				"1451648537",
				"0743264738",
				"0684807610",
				"1594202664"};
		
		Charset charset = Charset.forName("US-ASCII");
		String s;
		Path file = Paths.get("books.txt");
		try (BufferedWriter writer = Files.newBufferedWriter(file, charset)) {
		    
			for (int i = 0; i < 50; i++){
				isbn = isbns[random.nextInt(isbns.length)];
				// date in mm/dd/yyyy format
				date = (random.nextInt(12)+1) + "/" + (random.nextInt(30)+1) + "/" + (random.nextInt(17)+2000);
				condition = Integer.toString(random.nextInt(10)+1);
				s = isbn +","+ date +","+ condition + "\n";
				writer.write(s, 0, s.length());
			}
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}
}
