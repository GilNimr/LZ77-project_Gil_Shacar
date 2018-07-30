import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LZ77 {

	LZ77() {

	}

	public void Compress(String input_file_path, String output_file_path) {

		File input_file = new File(input_file_path);

		byte[] content_file_as_bytes = new byte[(int) input_file.length()]; // array
																			// of
																			// all
																			// the
		// bytes of the
		// file.

		try {
			FileInputStream fileInputStream = new FileInputStream(input_file);
			fileInputStream.read(content_file_as_bytes); // reading all the file
															// into
			// content_file_as_bytes.

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Reading The File.");
			e1.printStackTrace();
		}

		byte[] compressed_content_bytes_to_output_file = new byte[(int) input_file
				.length() * 2];
		int sliding_window = 0; // the sliding_window, later will be maximum of
								// 32.
		int look_a_head_buffer = 8; // the look a head buffer, maximum of 8

		/*
		 * the tuple will be as follows: (i,l,c), i is how far go back to go, l
		 * is how many characters to copy, c is the next character after that.
		 */

		int tmp_d = 0; // variable for finding j.

		int d = 0; // how much going back.

		int tmp_l = 0; // variable for finding l.

		int l = 0; // length of characters to copy.

		int index_of_compressed_content_bytes_to_output_file = 0; // index for
																	// appointing
																	// bytes to
																	// compressed_content_bytes_to_output_file

		for (int j = 0; j < content_file_as_bytes.length; j++) {

			char c = (char) content_file_as_bytes[j];
			sliding_window = j;
			if (sliding_window > 32)
				sliding_window = 32;

			for (int k = 0; k < sliding_window; k++) {
				tmp_l = 0;
				tmp_d = sliding_window - k;
				int step_forward = 0;

				while ((content_file_as_bytes[j + step_forward] == content_file_as_bytes[j
						- tmp_d + step_forward])) {
					tmp_l++;
					step_forward++;
					if ((j + step_forward >= content_file_as_bytes.length)
							|| (step_forward >= look_a_head_buffer))
						break;
				}

				if (tmp_l > l) {
					l = tmp_l;
					d = tmp_d;
					if (j + step_forward + 1 < content_file_as_bytes.length)
						c = (char) content_file_as_bytes[j + step_forward];
					else
						c = ' ';
				}

			}

			AddTo_compressed_content_bytes_to_output_file(
					compressed_content_bytes_to_output_file, d, l, c,
					index_of_compressed_content_bytes_to_output_file);
			index_of_compressed_content_bytes_to_output_file = index_of_compressed_content_bytes_to_output_file + 2;
			j = j + l;
			l = 0;
			d = 0;

		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					output_file_path);
			fileOutputStream.write(compressed_content_bytes_to_output_file, 0,
					index_of_compressed_content_bytes_to_output_file - 2);

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Writing The File.");
			e1.printStackTrace();
		}
	}

	void AddTo_compressed_content_bytes_to_output_file(
			byte[] compressed_content_bytes_to_output_file, int d, int l,
			int c, int index) {

		String d_str = Integer.toBinaryString(d);
		while (d_str.length() < 8) {
			d_str = "0" + d_str;
		}
		String l_str = Integer.toBinaryString(l);
		while (l_str.length() < 8) {
			l_str = "0" + l_str;
		}

		String d_l_str = d_str.substring(3, 8) + l_str.substring(5, 8);
		int d_l_int = Integer.parseUnsignedInt(d_l_str, 2);

		byte d_l_byte = (byte) d_l_int;

		byte c_byte = (byte) c;

		compressed_content_bytes_to_output_file[index] = d_l_byte;
		compressed_content_bytes_to_output_file[index + 1] = c_byte;
	}

}
