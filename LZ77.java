import java.io.DataOutputStream;
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

			System.out.println("we are reading \"" + c + "\", and our sliding window size is " + sliding_window + "\n");
			for (int k = 0; k < sliding_window; k++) {
				tmp_l = 0;
				tmp_d = sliding_window - k;
				int step_forward = 0;

				System.out.println("we try to copy \"" + (char) content_file_as_bytes[j- tmp_d + step_forward] + "\" so " + 
						((content_file_as_bytes[j + step_forward] == content_file_as_bytes[j- tmp_d + step_forward])));
				
		
				if (!(content_file_as_bytes[j + step_forward] == content_file_as_bytes[j- tmp_d + step_forward])) {
					
					
					if (j + step_forward+1 < content_file_as_bytes.length) { // deleted the = today
						System.out.println("The next letter is \""+(char) content_file_as_bytes[j+step_forward]+
								"\" and it's unequal to \""  + (char) content_file_as_bytes[j- tmp_d + step_forward]
										+"\" so now we need to check about upgrade,\n the next step of letter is: from sliding_window: \"" +
								(char) content_file_as_bytes[j - tmp_d + step_forward+1] + "\" and will read: \"" + (char) content_file_as_bytes[j + step_forward+1] 
										+ "\" so it is " + (content_file_as_bytes[j + step_forward+1] == content_file_as_bytes[j - tmp_d + step_forward+1]) + " equal");
					
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
						
				
						if (j + step_forward+1 < content_file_as_bytes.length) { // deleted the = today
							System.out.println("The next letter is \""+(char) content_file_as_bytes[j+step_forward]+
									"\" and it's unequal to \""  + (char) content_file_as_bytes[j- tmp_d + step_forward]
											+"\" so now we need to check about upgrade,\n the next step of letter is: from sliding_window: \"" +
									(char) content_file_as_bytes[j - tmp_d + step_forward+1] + "\" and will read: \"" + (char) content_file_as_bytes[j + step_forward+1] 
											+ "\" so it is " + (content_file_as_bytes[j + step_forward+1] == content_file_as_bytes[j - tmp_d + step_forward+1]) + " equal");
						
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
			DataOutputStream out = new DataOutputStream(fileOutputStream);
		    out.writeInt(indexes_of_changes.length);
			for (int i = 0; i < indexes_of_changes.length; i++) {
				out.writeInt(indexes_of_changes[i]);
				out.writeByte(letters_to_save[i]);
			}
			//long l=fileOutputStream.getChannel().position();
			fileOutputStream.write(compressed_content_bytes_to_output_file, 0,
					index_of_compressed_content_bytes_to_output_file);

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Writing The File.");
			e1.printStackTrace();
		}
		
		for (int i=0; i< indexes_of_changes.length; i++) {
			System.out.println((i+1) + ". at index: " + indexes_of_changes[i] + ", change to " + (char) letters_to_save[i]);

			// added today
			if ((indexes_of_changes[i] == 0) && (i > 0) ) {
				break;
			}
				// end added today
		}
		
	}
	
	
	
	
	void checkIfUpgrade(int l_, int step_forward_, int j_) {
		
		int added_to_l = 0;
		
		byte temp_new_char = content_file_as_bytes[j_- tmp_d + step_forward_];
		
		int index_of_upgrade_ = j_+step_forward_;
		
		byte save_the_old_char = content_file_as_bytes[j_ + step_forward_];
		
		content_file_as_bytes[j_ + step_forward_] = temp_new_char;
		
		
		System.out.println("We sent the char " + (char) save_the_old_char + " to check if we will switch her to "
				+ (char) temp_new_char + " at index " + index_of_upgrade_ + " of the source input it will be better then do it as "
						+ "usual lz77");
		
		do {
			added_to_l++;
			step_forward_++;

			//adeed today
			System.out.println("Step_forward_+j_  is " + (step_forward_+j_));
			// end added
			
			
				if ((j_ + step_forward_ >= content_file_as_bytes.length)|| (step_forward_ >= look_a_head_buffer)) {
					
					added_to_l=3;
					
					
					System.out.println("we see that the switch was good! because we finished to compress");
					break;
				}
				
				// added today
				
				
				if (!(content_file_as_bytes[j_ + step_forward_] == content_file_as_bytes[j_- tmp_d + step_forward_])) {
					
					if (j_ + step_forward_+1 < content_file_as_bytes.length) {  // deleted the "=" here today
						System.out.println("The next letter is \""+(char) content_file_as_bytes[j_+step_forward_]+
								"\" and it's unequal to \""  + (char) content_file_as_bytes[j_- tmp_d + step_forward_]
										+"\" so now we need to check about upgrade,\n the next step of letter is: from sliding_window: \"" +
								(char) content_file_as_bytes[j_ - tmp_d + step_forward_+1] + "\" and will read: \"" + (char) content_file_as_bytes[j_ + step_forward_+1] 
										+ "\" so it is " + (content_file_as_bytes[j_ + step_forward_+1] == content_file_as_bytes[j_ - tmp_d + step_forward_+1]) + " equal");
					
						if ((content_file_as_bytes[j_ + step_forward_+1] == content_file_as_bytes[j_ - tmp_d + step_forward_+1])) {
							System.out.println("And because its equal, we send it to check upgrade");
							
							checkIfUpgrade(tmp_l, step_forward_, j_);

						}
					}	
					
				}
				
				// end added today
				
		} while ((content_file_as_bytes[j_ + step_forward_] == content_file_as_bytes[j_- tmp_d + step_forward_]));
		
		if (added_to_l > 2) {
			indexes_of_changes[specific_index_of_changes] = index_of_upgrade_;
			letters_to_save[specific_index_of_changes] = save_the_old_char;
			upgrade = true;
			specific_index_of_changes++;
		}
		else {
			upgrade = false;
			content_file_as_bytes[j_ + step_forward_] = save_the_old_char;
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
		
		System.out.println("-----------(" + d + ", " + l + ", " + (char) c + ")---------------");
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
	public void Decompress_Upgraded(String input_file_path, String output_file_path) {

		File input_file = new File(input_file_path);
		
		/*
		 * Array of all the bytes of the file.
		 */
		
		
		byte[] size_of_fixing_info = new byte[4];
		byte[] compressed_file_as_bytes=null;
		byte[] fixing_info=null;
		
		try {
			FileInputStream fileInputStream = new FileInputStream(input_file);
			fileInputStream.read(size_of_fixing_info);
			ByteBuffer wrapped = ByteBuffer.wrap(size_of_fixing_info);
			int int_size_of_fixing_info = wrapped.getInt();
			
			fixing_info = new byte[int_size_of_fixing_info];
			fileInputStream.read(fixing_info);
			wrapped.clear();
			compressed_file_as_bytes = new byte[(int) input_file.length()-int_size_of_fixing_info];
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
		byte[] index_of_char_to_fix=new byte[4];
		for (int i = 0; i <fixing_info.length-4; i=i+5) {
			index_of_char_to_fix[0]=fixing_info[i];
			index_of_char_to_fix[1]=fixing_info[i+1];
			index_of_char_to_fix[2]=fixing_info[i+2];
			index_of_char_to_fix[3]=fixing_info[i+3];
			ByteBuffer wrapped = ByteBuffer.wrap(index_of_char_to_fix);
			int int_index_of_char_to_fix = wrapped.getInt();
			byte original_byte=fixing_info[i+4];
			returned_original_bytes_to_output_file[int_index_of_char_to_fix]=original_byte;
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

	
