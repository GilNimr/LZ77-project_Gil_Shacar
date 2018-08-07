import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

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
	
	
		//compressed_content_bytes_to_output_file variable
	final int look_a_head_buffer; // the look a head buffer, maximum of 7
	byte[] content_file_as_bytes; // array  of all the bytes of the file.
	byte[] compressed_content_bytes_to_output_file; //array  of all the bytes in the output file.
	char c;
	
	//upgrade compress definitions
	int[] indexes_of_changes;
	int specific_index_of_changes; // index of the indexes_of_changes int[]
	byte[] letters_to_save;
	boolean upgrade; // true if we going to use the upgrade at compress

	
	
	/*	int step_index_toUpgrade_byte_array; // how many stps each definitions at array of upgrade
	boolean usual_lz77; // true if we continue lz77 as usual
	
	boolean write_to_upgrade_file; // true if we need to write to upgrade file
	String lonelyLetters; // String with all lonely letters
	*/

	

	LZ77() { 
		sliding_window = tmp_d = /*step_index_toUpgrade_byte_array =*/specific_index_of_changes=
				d = tmp_l = l = index_of_compressed_content_bytes_to_output_file = 0;
		look_a_head_buffer = 7;
		upgrade = false;
	/*	write_to_upgrade_file = usual_lz77 =false;
		lonelyLetters ="";*/
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
	
	
	public void CompressWithUpgrade(String input_file_path, String output_file_path, String upgrade_side_file_path) {
		

		File input_file = new File(input_file_path);
		content_file_as_bytes = new byte[(int) input_file.length()]; 
		compressed_content_bytes_to_output_file = new byte[(int) input_file.length() * 2];
		
		indexes_of_changes = new int[(int) input_file.length()];
		
		letters_to_save = new byte[(int) input_file.length()];
		
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
	
		System.out.println("Reading the text...");
		
		for (int j = 0; j < content_file_as_bytes.length; j++) {

			c = (char) content_file_as_bytes[j];
			sliding_window = j;
			if (sliding_window > 31)
				sliding_window = 31;

			System.out.println("we are reading " + c + ", and our sliding window size is " + sliding_window + "\n");
			for (int k = 0; k < sliding_window; k++) {
				tmp_l = 0;
				tmp_d = sliding_window - k;
				int step_forward = 0;

				System.out.println("we try to copy " + content_file_as_bytes[j- tmp_d + step_forward] + " so " + 
						((content_file_as_bytes[j + step_forward] == content_file_as_bytes[j- tmp_d + step_forward])));
				
		
				if (!(content_file_as_bytes[j + step_forward] == content_file_as_bytes[j- tmp_d + step_forward])) {
					
					
					if (j + step_forward+1 <= content_file_as_bytes.length) {
						System.out.println("The next letter is unequal so now we need to check about upgrade,\n the next step of letter is " + 
								(content_file_as_bytes[j + step_forward+1] == content_file_as_bytes[j - tmp_d + step_forward+1]) + " equal");
					
						if ((content_file_as_bytes[j + step_forward+1] == content_file_as_bytes[j - tmp_d + step_forward+1])) {
							System.out.println("And because its equal, we send it to check upgrade");
							
							checkIfUpgrade(tmp_l, step_forward, j);

						}
					}	
				}
				
				
				while ((content_file_as_bytes[j + step_forward] == content_file_as_bytes[j
						- tmp_d + step_forward])) {
					tmp_l++;
					step_forward++;
					if ((j + step_forward >= content_file_as_bytes.length)
							|| (step_forward >= look_a_head_buffer))
						break;
					
						
					if (!(content_file_as_bytes[j + step_forward] == content_file_as_bytes[j- tmp_d + step_forward])) {
						
				
						if (j + step_forward+1 <= content_file_as_bytes.length) {
							System.out.println("The next letter is unequal so now we need to check about upgrade,\n the next step of letter is " + 
									(content_file_as_bytes[j + step_forward+1] == content_file_as_bytes[j - tmp_d + step_forward+1]) + " equal");
						
							if ((content_file_as_bytes[j + step_forward+1] == content_file_as_bytes[j - tmp_d + step_forward+1])) {
								System.out.println("And because its equal, we send it to check upgrade");
								
								checkIfUpgrade(tmp_l, step_forward, j);

							}
						}	
					}	
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
		
		for (int i=0; i< indexes_of_changes.length; i++) {
			System.out.println((i+1) + ". at index: " + indexes_of_changes[i] + ", change to " + (char) letters_to_save[i]);
		}
	}
	
	
	
	
	void checkIfUpgrade(int l_, int step_forward_, int j_) {
		
		
		
		
		int added_to_l = 0;
		
		byte temp_new_char = content_file_as_bytes[j_- tmp_d + step_forward_];
		
		int index_of_upgrade_ = j_+step_forward_;
		
		byte save_the_new_char = content_file_as_bytes[j_ + step_forward_];
		
		content_file_as_bytes[j_ + step_forward_] = temp_new_char;
		
		
		System.out.println("We sent the char " + (char) save_the_new_char + " to check if we will switch her to "
				+ (char) temp_new_char + " at index " + index_of_upgrade_ + " of the source input it will be better then do it as "
						+ "usual lz77");
		
		do {
			added_to_l++;
			step_forward_++;
			
			
				if ((j_ + step_forward_ >= content_file_as_bytes.length)|| (step_forward_ >= look_a_head_buffer)) {
					
					added_to_l=3;
					
					
					System.out.println("we see that the switch was good! because we finished to compress");
					break;
				}
				
		} while ((content_file_as_bytes[j_ + step_forward_] == content_file_as_bytes[j_- tmp_d + step_forward_]));
		
		if (added_to_l > 2) {
			indexes_of_changes[specific_index_of_changes] = index_of_upgrade_;
			letters_to_save[specific_index_of_changes] = save_the_new_char;
			upgrade = true;
			specific_index_of_changes++;
		}
		else {
			upgrade = false;
			content_file_as_bytes[j_ + step_forward_] = save_the_new_char;
		}
			
	}
	
	
	
	

	/*
	void buildLonelyString() {
		
		int[] frequency_of_letter = new int[127]; // as ASCI
		//boolean[] usedLetter = new boolean[127];
		
		for (int i=0; i<127; i++) {
			frequency_of_letter[i] = 0;
			//usedLetter[i] = false;
		}
		
		for (int i=0; i<content_file_as_bytes.length; i++) {
			frequency_of_letter[(int) content_file_as_bytes[i]]++;
		}
		
		for (int i=0; i<content_file_as_bytes.length; i++) {
			if (frequency_of_letter[(int) content_file_as_bytes[i]] == 1) {
				lonelyLetters += (char) content_file_as_bytes[i];
			}
		}
		System.out.println("-------------------lonelyString: " + lonelyLetters);
	}
	
	*/
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
		
		System.out.println("-----------(" + d + ", " + l + ", " + (char) c + ")---------------");
	}
	
/*	
	void addTo_with_upgrade ( byte[] compressed_with_upgrade_content_bytes_to_output_file,int d,int l, char c,
			int j, int step_read, int index_of_compressed_content_bytes_to_output_file_with_upgrade){


		System.out.println("\nIn AddTo with upgrade...\nstep_index= " + step_index_toUpgrade_byte_array + " index_of_compres...: " +
				index_of_compressed_content_bytes_to_output_file_with_upgrade);
		
		int how_many_indexes_to_casting=4, index_of_bit_at_content_text = j+step_read;
		
		if (index_of_bit_at_content_text <= 127) {
			how_many_indexes_to_casting = 1;
		}
		else if (index_of_bit_at_content_text <= 32767) {
			how_many_indexes_to_casting = 2;
		}
		else if (index_of_bit_at_content_text <= 8388607) {
			how_many_indexes_to_casting = 3;
		}
		System.out.print(" we'll add " + how_many_indexes_to_casting + " indexes of numberOfBit\n");
		
		
		
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(index_of_bit_at_content_text);

		System.out.println("the index of this char is " + index_of_bit_at_content_text + ", the byte is:");
		byte c_byte = (byte) c;
		int index=0;
		for (int i=3; index<how_many_indexes_to_casting; i--) {
			compressed_with_upgrade_content_bytes_to_output_file[index_of_compressed_content_bytes_to_output_file_with_upgrade+index] 
					= b.array()[i];
			index++;
			System.out.print((index-1) + ": " +b.array()[i] + " , ");
		}

		compressed_with_upgrade_content_bytes_to_output_file[index_of_compressed_content_bytes_to_output_file_with_upgrade+
		                                                     how_many_indexes_to_casting] = c_byte;
		step_index_toUpgrade_byte_array += (how_many_indexes_to_casting+1);
		System.out.println(" \nstep_index: " + step_index_toUpgrade_byte_array + ", char: " + (char) c + "\nfinish addToWith\n\n");
		write_to_upgrade_file = true;

		
		System.out.println("--------(we added: index: " + index_of_bit_at_content_text + " the char " + (char) c + "-------)");

	}
	
	
	void checkCompress(int j, int step_read, int step_copy) {
		System.out.println("Checking regular compress...");
		if (content_file_as_bytes[j + step_read] == content_file_as_bytes[j - tmp_d + step_copy]){
			usual_lz77 = true;
			upgrade = false;
		}
		else {
			usual_lz77 = false;
		}
		System.out.println("\n" + (j+1+step_read) + "'st letter, so now we read: " + (char) content_file_as_bytes[j + step_read]
				+ ", and we copy " + (char) content_file_as_bytes[j - tmp_d + step_copy] + ", so usual_lzz= "
				+ (content_file_as_bytes[j + step_read] == content_file_as_bytes[j - tmp_d + step_copy]));
	}

	
	void checkCompressWithUpgrade(int j, int step_read, int step_copy){
		boolean isLetterAtLonelyString = false;
		
		System.out.println("Checking upgrade compress...");
		
		for (int i=0; i<lonelyLetters.length(); i++) {
			if (content_file_as_bytes[j + step_read] == lonelyLetters.charAt(i)) {
				isLetterAtLonelyString = true;
			}
		}
		
		if ( (content_file_as_bytes[j + step_read+1] == content_file_as_bytes[j - tmp_d + step_copy+1]) &&
				(isLetterAtLonelyString)) {
			upgrade = true;
		}
		else
			upgrade=false;
		
		System.out.println("After we know that usual_lzz=" +usual_lz77 	+ ",\n we see that the letter after thats letter is " + 
		(char) content_file_as_bytes[j + step_read+1] + ",\n and because we copy the next letter- " +
					(char)content_file_as_bytes[j - tmp_d + step_copy+1] + ", and becuase it " + isLetterAtLonelyString + " that "
							+ "it is in our lonelyString,  so upgrade= " + 	(upgrade) );
	}
	*/
	
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
		
		byte[] returned_original_bytes_to_output_file = new byte[(int) input_file.length() * 500];
		
		int index_of_returned_original_bytes = 0;
		
		for (int j = 0; j < compressed_file_as_bytes.length; j++) {
			d = (int) compressed_file_as_bytes[j];
			d = d << 24;
			d = d >>> 24;
			d = d >>> 3;
			l = (int) compressed_file_as_bytes[j];
			l = l << 29; // 32-3
			l = l >>> 29;//32-3
			System.out.println("d is: "+d+" l is: "+l);

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
	