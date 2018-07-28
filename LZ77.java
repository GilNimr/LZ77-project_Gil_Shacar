import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LZ77 {

	LZ77() {

	}

	public void Compress(String input_file, String output_file) {

		File file = new File(input_file);
		byte[] byteArray = new byte[(int) file.length()];

		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(byteArray);
			/*
			 * for (int i = 0; i < b.length; i++) { System.out.print((char)
			 * b[i]); }
			 */
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Reading The File.");
			e1.printStackTrace();
		}
		System.out.println("(0,0," + (char) byteArray[0] + ") ");

		int window = 1; // later will be maximum of 32;
		int Look_A_Head = 8;
		int tmp_j = 0;
		int j = 0; // how much going back.
		int tmp_l = 0;
		int l = 0; // how much to take.

		for (int i = 1; i < byteArray.length; i++) {
			char c = (char) byteArray[i];
			window = i;
			if (window > 32)
				window = 32;

			for (int k = 0; k < window; k++) {
				tmp_l = 0;
				tmp_j = window - k;
				int counter = 0;

				while ((byteArray[i + counter] == byteArray[i - tmp_j + counter])
						&& (i + counter < byteArray.length && counter <= Look_A_Head)) {
					tmp_l++;
					counter++;
				}

				if (tmp_l > l) {
					l = tmp_l;
					j = tmp_j;
					c = (char) byteArray[i + counter+1];
				}

			}
			System.out.println("(" + j + "," + l + "," + c + ") ");
			l = 0;
			j = 0;
		}

	}
}
