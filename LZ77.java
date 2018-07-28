import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LZ77 {

	LZ77() {

	}

	public void Compress(String input_file, String output_file) {

		File file = new File(input_file);
		byte[] byteArray = new byte[(int) file.length()]; // array of all the
															// bytes of the
															// file.

		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(byteArray); // reading all the file into
												// byteArray.

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Reading The File.");
			e1.printStackTrace();
		}

		

		int window = 0; // the sliding window, later will be maximum of 32.
		int Look_A_Head = 8; // the look a head buffer, maximum of 8

		/*
		 * the tuple will be as follows: (j,l,c), j is how far go back to go, l
		 * is how many characters to copy, c is the next character after that.
		 */

		int tmp_j = 0; // variable for finding j.

		int j = 0; // how much going back.

		int tmp_l = 0; // variable for finding l.

		int l = 0; // length of characters to copy.

		for (int i = 0; i < byteArray.length; i++) {
			
			char c = (char) byteArray[i];
			window = i;
			if (window > 32)
				window = 32;

			for (int k = 0; k < window; k++) {
				tmp_l = 0;
				tmp_j = window - k;
				int counter = 0;

				while ((byteArray[i + counter] == byteArray[i - tmp_j + counter])) {
					tmp_l++;
					counter++;
					if ((i + counter >= byteArray.length)
							|| (counter >= Look_A_Head))
						break;
				}

				if (tmp_l > l) {
					l = tmp_l;
					j = tmp_j;
					if (i + counter + 1 < byteArray.length)
						c = (char) byteArray[i + counter];
					else
						c = ' ';
				}

			}
			i = i + l;
			System.out.println("(" + j + "," + l + "," + c + ") ");
			l = 0;
			j = 0;
		}

	}
}
