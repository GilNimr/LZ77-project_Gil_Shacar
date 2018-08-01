import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.omg.PortableInterceptor.USER_EXCEPTION;

/*
 * the tuple will be as follows: (d,l,c), d is how far go back to go, l
 * is how many characters to copy, c is the next character after that.
 */

public class LZ77 {
	int sliding_window; // the sliding_window, later will be maximum of 31
	int tmp_d; // variable for finding j.
	int d; // how much going back.
	int tmp_l; // variable for finding l.
	int l; // length of characters to copy.
	int index_of_compressed_content_bytes_to_output_file=0; // index for appointing bytes to 
	boolean write_to_upgrade_file; // will be true if we use the upgrade
	
		//compressed_content_bytes_to_output_file variable
	final int look_a_head_buffer; // the look a head buffer, maximum of 7
	byte[] content_file_as_bytes; // array  of all the bytes of the file.
	byte[] compressed_content_bytes_to_output_file; //array  of all the bytes in the output file.
	char c;
	
	LZ77() { 
		sliding_window = tmp_d = d = tmp_l = l = index_of_compressed_content_bytes_to_output_file = 0;
		look_a_head_buffer = 7;
		write_to_upgrade_file = false;
	}
	
	public void CompressWithUpgrade(String input_file_path, String output_file_path, String upgrade_side_file_path) {

		char tmp_c=' '; // the char of the twist (the loss char)
		int number_of_bit_to_change=0; // the number of bit
		boolean use_the_upgrade ; // know if we used the upgrade
		int index_of_compressed_content_bytes_to_output_file_with_upgrade=0;// index to array of bytes to upgrade output
		File input_file = new File(input_file_path);

		content_file_as_bytes = new byte[(int) input_file.length()]; 

		try {
			FileInputStream fileInputStream = new FileInputStream(input_file);
			fileInputStream.read(content_file_as_bytes); // reading all the file into content_file_as_bytes.

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Reading The File.");
			e1.printStackTrace();
		}

		compressed_content_bytes_to_output_file = new byte[(int) input_file.length() * 2];
		byte[] compressed_with_upgrade_content_bytes_to_output_file = new byte[(int) input_file.length()];

		for (int j = 0; j < content_file_as_bytes.length; j++) {
			use_the_upgrade = false;
			c = (char) content_file_as_bytes[j];
			sliding_window = j;
			if (sliding_window > 31)
				sliding_window = 31;

			for (int k = 0; k < sliding_window; k++) {
				
				tmp_l = 0;
				tmp_d = sliding_window - k;
				int step_forward = 0;
				
				
				// i added next else-if:
				if ( (content_file_as_bytes[j] != content_file_as_bytes[j- tmp_d]) && 
						(content_file_as_bytes[j+1] == content_file_as_bytes[j-tmp_d]) ) {
					tmp_c = (char) content_file_as_bytes[j];
					number_of_bit_to_change = j;
					use_the_upgrade = true;
					AddTo_compressed_with_upgrade_content_bytes_to_output_file(number_of_bit_to_change, tmp_c, 
						compressed_with_upgrade_content_bytes_to_output_file,
							index_of_compressed_content_bytes_to_output_file_with_upgrade);
					index_of_compressed_content_bytes_to_output_file_with_upgrade+= 2;
					write_to_upgrade_file=true;
					break;				
				}
				else {
				
					while ((content_file_as_bytes[j + step_forward] == content_file_as_bytes[j
							- tmp_d + step_forward])){
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

				
				

			}
			if (!use_the_upgrade) {
				AddTo_compressed_content_bytes_to_output_file(
						compressed_content_bytes_to_output_file, d, l, c,
						index_of_compressed_content_bytes_to_output_file);
				index_of_compressed_content_bytes_to_output_file = index_of_compressed_content_bytes_to_output_file + 2;
			}

			j = j + l;
			l = 0;
			d = 0;

		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					output_file_path);
			fileOutputStream.write(compressed_content_bytes_to_output_file, 0,
					index_of_compressed_content_bytes_to_output_file - 2);
			
			if (write_to_upgrade_file) {
				FileOutputStream fileOutputStream_with_upgrades = new FileOutputStream(upgrade_side_file_path);
				fileOutputStream_with_upgrades.write(compressed_with_upgrade_content_bytes_to_output_file, 0,
						index_of_compressed_content_bytes_to_output_file_with_upgrade);
			}
			

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Writing The File.");
			e1.printStackTrace();
		}

	}
	
	
	
	
	
	

	public void Compress(String input_file_path, String output_file_path) {

		File input_file = new File(input_file_path);

		content_file_as_bytes = new byte[(int) input_file.length()]; 

		try {
			FileInputStream fileInputStream = new FileInputStream(input_file);
			fileInputStream.read(content_file_as_bytes); // reading all the file into content_file_as_bytes.

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Reading The File.");
			e1.printStackTrace();
		}

		compressed_content_bytes_to_output_file = new byte[(int) input_file.length() * 2];

		for (int j = 0; j < content_file_as_bytes.length; j++) {

			c = (char) content_file_as_bytes[j];
			sliding_window = j;
			if (sliding_window > 31)
				sliding_window = 31;

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

	void AddTo_compressed_content_bytes_to_output_file(byte[] compressed_content_bytes_to_output_file, int d, int l,
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

	
	void AddTo_compressed_with_upgrade_content_bytes_to_output_file(int index_of_bit_at_content_text, char c, 
			byte[] compressed_with_upgrade_content_bytes_to_output_file,
				int index_of_compressed_content_bytes_to_output_file_with_upgrade) {
	
				String str_to_compress = Integer.toBinaryString(index_of_bit_at_content_text);
				while (str_to_compress.length() < 8) {
					str_to_compress = "0" + str_to_compress;
				}
				
				int int_from_str_to_compress = Integer.parseUnsignedInt(str_to_compress, 2);
				
				byte byte_to_compress = (byte) int_from_str_to_compress;
				
				byte c_byte = (byte) c;
				
				compressed_with_upgrade_content_bytes_to_output_file[index_of_compressed_content_bytes_to_output_file_with_upgrade]
						= byte_to_compress;
				compressed_with_upgrade_content_bytes_to_output_file[index_of_compressed_content_bytes_to_output_file_with_upgrade+1]
						= c_byte;
	}
	
	public void Decompress(String input_file_path, String output_file_path) {
		File input_file = new File(input_file_path);

		/*
		 * Array of all the bytes of the file.
		 */
		byte[] compressed_file_as_bytes = new byte[(int) input_file.length()];

		try {
			FileInputStream fileInputStream = new FileInputStream(input_file);
			fileInputStream.read(compressed_file_as_bytes); // reading all the
															// file
															// into
															// compressed_file_as_bytes.

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Reading The File.");
			e1.printStackTrace();
		}

		byte[] returned_original_bytes_to_output_file = new byte[(int) input_file
				.length() * 500];
		
		int index_of_returned_original_bytes = 0;
		for (int j = 0; j < compressed_file_as_bytes.length; j++) {
			d = (int) compressed_file_as_bytes[j];
			d = d << 24;
			d = d >>> 24;
			d = d >>> 3;
			l = (int) compressed_file_as_bytes[j];
			l = l << 29; // 32-3
			l = l >>> 29;//32-3
			System.out.println("i is: "+d+" l is: "+l);

			if (d == 0) {
				returned_original_bytes_to_output_file[index_of_returned_original_bytes] = compressed_file_as_bytes[j + 1];
				index_of_returned_original_bytes++;
			} else {
				while (l > 0) {
					returned_original_bytes_to_output_file[index_of_returned_original_bytes] = 
							returned_original_bytes_to_output_file[index_of_returned_original_bytes- d];
					index_of_returned_original_bytes++;
					l--;
				}
				returned_original_bytes_to_output_file[index_of_returned_original_bytes] = compressed_file_as_bytes[j + 1];
				index_of_returned_original_bytes++;
			}
			j++;
		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					output_file_path);
			fileOutputStream.write(returned_original_bytes_to_output_file, 0,
					index_of_returned_original_bytes);

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Writing The File.");
			e1.printStackTrace();
		}
	}
}


