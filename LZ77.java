import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Savepoint;

import javax.crypto.CipherInputStream;

import org.omg.PortableInterceptor.USER_EXCEPTION;



/*
 * the tuple will be as follows: (d,l,c), d is how far go back to go, l
 * is how many characters to copy, c is the next character after that.
 */

public class LZ77 {
	
	private int sliding_window; // the sliding_window
	private int tmp_d; // variable for finding d.
	private int d; // how much going back.
	private int tmp_l; // variable for finding l.
	private int l; // length of bytes to copy.
	private int index_of_compressed_content_bytes_to_output_file=0; // index for appointing bytes to compressed_content_bytes_to_output_file variable	
	private  final int look_a_head_buffer; // the look a head buffer
	private byte[] content_file_as_bytes; // array  of all the bytes of the file.
	private byte[] compressed_content_bytes_to_output_file; //array  of all the bytes in the output file.
	private char c; // the char we will put at each iterate of compression	
	
	//upgrade compress definitions of global variables
	private boolean upgrade; // true if we going to save the upgrade - just at compress at specific iterate of k.
	private boolean was_first_change;	//   will be off each new letter we will read (at loop), will be true after first use at upgrade because of saving the 								// changes if we will not use specific upgrade after that time
	private int number_of_changes;	// how much changes at each new letter we will read (at loop). counter 

	LZ77() { 
		// default values to global variables:
		sliding_window = tmp_d =
				d = tmp_l = l = index_of_compressed_content_bytes_to_output_file = 0;
		look_a_head_buffer = 31;

	}
	
	
	public void Generate_String(String output_file_path) {
		System.out
				.println("hi this method geneartes strings with repetition with few mismatched symbols \n"
						+ "for example: ababababfbabababgbab");
		StringBuilder str = new StringBuilder();
		
		double probabilty;

		for (int i = 0; i < 5000; i++) {
			probabilty = Math.random();
			if (probabilty > 0.1)
				str.append('a');
			else
				str.append((char)(65+(Math.random()*35)));
			probabilty = Math.random();
			if (probabilty > 0.1)
				str.append('b');
		/*	else
				str.append((char)(65+(Math.random()*35)));
	*/	}
		//System.out.println(str.toString());
		String str_=str.toString();
		byte [] generated_str=str_.getBytes();
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					output_file_path);
			fileOutputStream.write(generated_str);

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
			if (sliding_window > 7)
				sliding_window = 7;

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
					index_of_compressed_content_bytes_to_output_file);

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Writing The File.");
			e1.printStackTrace();
		}

	}
	
	
	public void CompressWithUpgrade(String input_file_path, String output_file_path) {
		
		//definition the local variables
		 int[] indexes_of_changes;	// all the indexes that we will change before decompress
		byte[] letters_to_save;		// all the bytes that we will fix before decompress at indexes from indexes_of_changes
		boolean use_the_upgrade;	// true if we finished k iterate and we going to use the upgrade
		File input_file = new File(input_file_path);	// the input
		content_file_as_bytes = new byte[(int) input_file.length()];	 
		compressed_content_bytes_to_output_file = new byte[(int) input_file.length() * 2];
		indexes_of_changes = new int[(int) input_file.length()];
		letters_to_save = new byte[(int) input_file.length()];
		int indexesAtFinito=0; // counter of all final changes at all file
		int final_number_of_changes; // counter of all changes at specific iterate of j (when reading new char)
		
		
		// reading the input:
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
	

		// j loop :  each j will be a new letter we read
		for (int j = 0; j < content_file_as_bytes.length; j++) {
			
			// default values to the variables that need to at that loop: 
			use_the_upgrade = was_first_change=false; 
			number_of_changes= final_number_of_changes=0;
			c = (char) content_file_as_bytes[j];
			
			// definition local variables of loop:
			
			/* 	we have 3 doubles of arrays - temporary and save of each: indexes, old chars and new bytes after each upgrade.
			 	temporary - will change each new upgrade
			 	save - will change if we see that last upgrade was optimal
			 	the size will be the size of look_a_head_buffer because we want the size of the arrays will be
			 	a little bit more then the maximum of change we can do at same compress 
			*/
			
			byte[] temp_save_char_after_switch = new byte[look_a_head_buffer]; // all the bytes (after changes)
			int[] temp_index_of_upgrade_= new int[look_a_head_buffer]; //all the indexes  we save 	
			byte[] temp_save_old_char= new byte[look_a_head_buffer];	// all the bytes (before changes) we save inside  
			byte[] save_old_char = new byte[look_a_head_buffer]; 
			byte[] save_char_after_twist = new byte[look_a_head_buffer];
			int[] save_index_of_upgrade= new int[look_a_head_buffer]; 
			
			// maybe we will need to change c (the variable) also, so we will save it with that variables:
			int tempOfFinalIndex=0; // c index
			byte tempOfFinalOldChar=0; // char before change
			byte tempOfFinalCharAfterTwist=0;  // char after change
			boolean changeTheExtra=false; // true if we need to use change
			
			
			// default values to the save (no temporary) arrays:
			for (int i=0; i<look_a_head_buffer; i++) {
				save_char_after_twist[i]= 0;
				save_old_char[i] =0;
				save_index_of_upgrade[i] =0;  
			}
				
			// definition the sliding window respectively to j
			sliding_window = j;
			if (sliding_window > 7)
				sliding_window = 7;

			//after all definitions, we are reading index [j], it's c, and we start loop on sliding window:
			
			for (int k = 0; k < sliding_window; k++) {
				
				//default values to local variables of checking optimal copy with sliding window 
				
				number_of_changes=0;
				tmp_l = 0;
				tmp_d = sliding_window - k;
				upgrade = false;
				int step_forward = 0; // variable of steps after the first letter of sliding window that we can copy
				
				// default values to the temporary arrays:
				for (int i=0; i<look_a_head_buffer; i++) {
					temp_save_char_after_switch[i] =0;
					temp_save_old_char[i] = 0;
					temp_index_of_upgrade_[i] = 0;  
				}
				
				/* so now we checking copy from index[j- tmp_d + step_forward]
				 * we need to check if (char) content_file_as_bytes[j- tmp_d + step_forward], its equal to
						 (char) content_file_as_bytes[j + step_forward] 
				 */
		
				if (!(content_file_as_bytes[j + step_forward] == content_file_as_bytes[j- tmp_d + step_forward])) { // if its not equal	
					if (j + step_forward+1 < content_file_as_bytes.length) {  // if we are not in the last letter of file
						/*
						 *  if we here it's mean that (char) content_file_as_bytes[j+step_forward]
								is unequal to (char) content_file_as_bytes[j- tmp_d + step_forward]
										so now we need to check about upgrade:
										we will check the next step of letter:
										so we need to check if the char we will copy:
										 (char) content_file_as_bytes[j - tmp_d + step_forward+1] is equal to the char we  
										are reading (char) content_file_as_bytes[j + step_forward+1] 
						 */
						
					
						if ((content_file_as_bytes[j + step_forward+1] == content_file_as_bytes[j - tmp_d + step_forward+1])) {
							// if its equal, we send it to check upgrade
							
							byte charBeforeChange = content_file_as_bytes[j + step_forward];  // we save the char we read
							
							// we send it to check copy:
							checkIfUpgrade(tmp_l, step_forward, j, temp_index_of_upgrade_, temp_save_old_char, temp_save_char_after_switch,  save_old_char, save_index_of_upgrade, save_char_after_twist);
							
							// if upgrade is now true, we save it at our arrays:
							if (upgrade) {
								putNumbersAtUpgrade(temp_index_of_upgrade_, temp_save_old_char, temp_save_char_after_switch, save_old_char, save_index_of_upgrade, save_char_after_twist, charBeforeChange, j, step_forward);							
							}
						}
					}	
				}
				
				/*  important ! -also if upgrade is true, it's just say to computer that we may use it, we stilln't compressed
				*	nothing! 
				*	if upgrade was true, so until the end of iteration, there is a change at content-file, so the next while-loop use it.
				*/		
				
				while ((content_file_as_bytes[j + step_forward] == content_file_as_bytes[j
						- tmp_d + step_forward])) {
					
					// while the byte we read is equal to the byte we cop
					
					boolean loopWithUpgrade = upgrade; // save if was some change
					tmp_l++;						   // adding one to tmp_l that may change l 
					step_forward++;						// adding one to step_forward to check our next copy
					
					// if we finished the look_a_head_buffer or finished reading the bytes
					if ((j + step_forward >= content_file_as_bytes.length)  
							|| (step_forward >= look_a_head_buffer))
						break;
					
					/*
						we are checking if the byte we read: content_file_as_bytes[j + step_forward],
						is equal to the byte we try to copy: content_file_as_bytes[j- tmp_d + step_forward]
					*/
					
					if (!(content_file_as_bytes[j + step_forward] == content_file_as_bytes[j- tmp_d + step_forward])) { // if its not equal		
						 if (j + step_forward+1 < content_file_as_bytes.length) { // if we are not in the last byte of file
							 
							 /* if we are here, it's mean that The next byte we read is content_file_as_bytes[j+step_forward],
							  *  and it's unequal to content_file_as_bytes[j- tmp_d + step_forward],
							  *  so now we need to check about upgrade, 
							  *  the next step of letter is: copy: content_file_as_bytes[j - tmp_d + step_forward+1], 
							  *  							read: content_file_as_bytes[j + step_forward+1]    
							  */
						
							if ((content_file_as_bytes[j + step_forward+1] == content_file_as_bytes[j - tmp_d + step_forward+1])) {								
								 // if we are here its mean that the next step of bytes are equals, so we need to check upgrade
								
								byte charBeforeChange = content_file_as_bytes[j + step_forward];  // save the char we read
								boolean saveIfUpgrade = loopWithUpgrade; // save if we are in loop with some upgrades already
								
								//send to check if it will br good to use upgrafe:
								checkIfUpgrade(tmp_l, step_forward, j, temp_index_of_upgrade_, temp_save_old_char, temp_save_char_after_switch,  save_old_char, save_index_of_upgrade, save_char_after_twist);
							
								  if (upgrade) { // if upgrade is true, we send it to save the values at the arrays:
									  
									 putNumbersAtUpgrade(temp_index_of_upgrade_, temp_save_old_char, temp_save_char_after_switch, save_old_char, save_index_of_upgrade, save_char_after_twist, charBeforeChange, j, step_forward);
								}
								  else if (saveIfUpgrade) { // if upgrade is false, but we are in loop that used once upgrade
									  upgrade = true;
								  }
							}
						}	
					}
					
				} // end while
				
				if ( (tmp_l > l) || ( (tmp_l==l) && (!upgrade) && (tmp_l>0) ) || ((tmp_l==l) && (upgrade) && (tmp_l>0) && (final_number_of_changes>number_of_changes) ) ){

					/* if we found optimal compressed at that iteration: tmp_l bigger then l.
						--	or tmp_l equal to l but not use the upgrade - we prefer use the usual lz_77 if it the same 
								l because the upgrade are use more bytes at compressed file
					 	--	or we prefer use compression with much less changes because each change is more size of compressed file
					 	*/
					if (j+step_forward<content_file_as_bytes.length) { // if we still not finished to read
						// we save the values of the next char, that going to be at c variable
						tempOfFinalIndex = j+step_forward;
						tempOfFinalOldChar = content_file_as_bytes[j+step_forward];
						tempOfFinalCharAfterTwist =  content_file_as_bytes[j - tmp_d + step_forward];
					}
					
					if (j + step_forward + 1 <= content_file_as_bytes.length)    {
						if ( content_file_as_bytes[j - tmp_d + step_forward]  != content_file_as_bytes[j + step_forward]) {
							// we are here if we need to use upgrade on c - to change the next byte to the next byte we would copy 
							
							changeTheExtra=true; // we say to computer that we used upgrade to c , so we will save all 
													//the values at the array of upgrade at the end of compress
							c = (char) content_file_as_bytes[j - tmp_d + step_forward] ; // we save c as the next byte we would copy	
						}
						
						else { // else c will take char as usual lz_77
							c = (char) content_file_as_bytes[j + step_forward];	
						}				
					}	
					
					else	// if we finished read the file
						c = ' ';
					
					if (upgrade) {
						// if we found optimal compress and upgrade was in use:
						
						final_number_of_changes = number_of_changes; // we save the number of changes
						use_the_upgrade=true; 						// we turn on use_the_upgrade so the computer know to use it at the end of loop

						for (int i=0; i<number_of_changes; i++) {
							// we save the values that was at temp-arrays to save-arrays:
							save_old_char[i] = temp_save_old_char[i];
							save_index_of_upgrade[i] = temp_index_of_upgrade_[i];	
							save_char_after_twist[i] = temp_save_char_after_switch[i];
							
							// because we stilln't finished checking optimal compress, we back the content-file to how it was before last changes
							content_file_as_bytes[temp_index_of_upgrade_[i]] = save_old_char[i];  
						}
					}
					
					else { // if we found optimal compress and upgrade was not in use:
						use_the_upgrade = false; // we turn off use_the_upgrade so the computer not use it at the end

						if (was_first_change) { // if we changed already someting at conten-file, we return it:
							for (int i=0; i<number_of_changes; i++) {							
								content_file_as_bytes[temp_index_of_upgrade_[i]] = temp_save_old_char[i];
							}
						}
					}
				
					l = tmp_l;
					d = tmp_d;	
					// and for now we have optimal (d, l ,c) 
				}
				
				
				
				else if (was_first_change) { // if we finished iteration, we changed something but at the end we are not use it, we return it:
					for (int i=0; i<number_of_changes; i++) {							
						content_file_as_bytes[temp_index_of_upgrade_[i]] = temp_save_old_char[i];
					}
				}
				
				if (step_forward >= look_a_head_buffer) { // if step_forward used all the look_a_head_buffer we will not continue loop
					break;
				}	
			} // end k (loop of sliding window)
			
			if (use_the_upgrade) { 
				/* 
				 * if the optimal compressed we found was with upgrade, we will change the 
				 *  content_file as upgrade and save values at the final array of upgrade 
				*/ 
					
				for (int i=0; i<final_number_of_changes; i++) {
					content_file_as_bytes[save_index_of_upgrade[i]] =save_char_after_twist[i];	
				}
				
				for (int i=0; i<final_number_of_changes; i++) {
					
					indexes_of_changes[indexesAtFinito] = save_index_of_upgrade[i];
					letters_to_save[indexesAtFinito] = save_old_char[i];
					
					/*
					 * we save upgrade: at index [indexes_of_changes[indexesAtFinito]] its now: save_char_after_twist[i]
					 * and the decompressed will change it at the specific index to letters_to_save[indexesAtFinito]. 
					 */
					indexesAtFinito++;
				}
			}
			
			if (changeTheExtra) { 
				/* if we used upgrade also for c, we will change the content_file as upgrade 
				 * and save values at the final array of upgrade 
				 */
				content_file_as_bytes[tempOfFinalIndex] =tempOfFinalCharAfterTwist;	
				indexes_of_changes[indexesAtFinito] = tempOfFinalIndex;
				letters_to_save[indexesAtFinito] = tempOfFinalOldChar;

				/* we save upgrade: at index [indexes_of_changes[indexesAtFinito]]
				 * its now: tempOfFinalCharAfterTwist, we will change later to tempOfFinalOldChar 
				 * at specific index
				 */
				indexesAtFinito++;
				changeTheExtra=false; // we turn back the changeTheExtra
			}

			// we send the values to compress
			AddTo_compressed_content_bytes_to_output_file(
					compressed_content_bytes_to_output_file, d, l, c,
					index_of_compressed_content_bytes_to_output_file);
			index_of_compressed_content_bytes_to_output_file = index_of_compressed_content_bytes_to_output_file + 2;
			
			// and we will continue to the next iteration with refresh vaues:
			j = j + l;
			l = 0;
			d = 0;

		} // end of j loop - the chars we read
		
		try {
			// after we finished reading the file, write the compress file
			FileOutputStream fileOutputStream = new FileOutputStream(output_file_path);
			DataOutputStream out = new DataOutputStream(fileOutputStream);
			
			// write the upgrade changes:
		    out.writeInt(indexesAtFinito*3);
			for (int i = 0; i < indexesAtFinito; i++) {
				
				//"at index: [indexes_of_changes[i]] we will change to letters_to_save[i]
				
				out.writeShort(indexes_of_changes[i]);
				out.writeByte(letters_to_save[i]);
			}
			fileOutputStream.write(compressed_content_bytes_to_output_file, 0,
					index_of_compressed_content_bytes_to_output_file);

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Writing The File.");
			e1.printStackTrace();
		}
	}  // finished method
	
	private void checkIfUpgrade(int l_, int step_forward_, int j_, int[] temp_index_of_upgrade_, byte[] temp_save_old_char, 
			byte[] temp_save_char_after_switch, byte[] save_old_char, int[] save_index_of_upgrade, byte[] save_char_after_twist) {

		/*
		 * that function called when computer recognized option to upgrade
		 * we will put true in "upgrade" if we will recognize that it is optimal to use upgrade and change the content
		 * else, we put false 
		 */
		
		// save the variables we going to change
		byte old_char_of_iterate=content_file_as_bytes[j_ + step_forward_];
		byte char_after_switch_of_iterate = content_file_as_bytes[j_- tmp_d + step_forward_];
		int temp_index_of_iterate = j_+step_forward_;
		int added_to_l = 0; // check how much we will add to l if we weill make the change
		content_file_as_bytes[j_ + step_forward_] = char_after_switch_of_iterate; // change the content
		
			/* so we sent the char old_char_of_iterate to check if we will switch her to
			 *  char_after_switch_of_iterate, at index [temp_index_of_iterate] of the source input it will be better then do it as 
			 *  usual_lzz
			 * So for temp, at index[temp_index_of_iterate] we get char_after_switch_of_iterate  
			 */

		do {
			// becuase we know that the change of content-file makes one true copy, we use do-while loop for checking the next bytes
			added_to_l++;
			step_forward_++;
			
				if ((j_ + step_forward_+1 >= content_file_as_bytes.length)|| (step_forward_ >= look_a_head_buffer)) {
					// if we finished read the file or finished look_a_buffer
					break;
				}
				
				/* 	the next lines will do the same as at the compressWithUpgrade method, that check if we need to use
				  	upgrade again - at recursion at the upgrade that we are in it
				  */
				
				if (!(content_file_as_bytes[j_ + step_forward_] == content_file_as_bytes[j_- tmp_d + step_forward_])) {
					if (j_ + step_forward_+1 < content_file_as_bytes.length) {  
						if ((content_file_as_bytes[j_ + step_forward_+1] == content_file_as_bytes[j_ - tmp_d + step_forward_+1])) {
							
							// saving values..
							int temp_step = step_forward_;
							int temp_j = j_;
							byte charBeforeChange = content_file_as_bytes[j_+step_forward_]; // if we will not change
							boolean saveIfUpgraded = upgrade;
							
							// recursion:
							checkIfUpgrade(tmp_l, temp_step, temp_j, temp_index_of_upgrade_, 
									temp_save_old_char, temp_save_char_after_switch , save_old_char, save_index_of_upgrade, save_char_after_twist);
							
							if (upgrade) {
								putNumbersAtUpgrade(temp_index_of_upgrade_, temp_save_old_char, temp_save_char_after_switch, save_old_char,
										save_index_of_upgrade, save_char_after_twist, charBeforeChange, j_, step_forward_);
							}
							
							else if (saveIfUpgraded) {
								upgrade=true;
							}
						}
					}						
				}
				
		} while ((content_file_as_bytes[j_ + step_forward_] == content_file_as_bytes[j_- tmp_d + step_forward_]));
		
		if (added_to_l > 2){  
			/*
			 * if added_to_l was 1, we didn't do anything, if it was 2, so with the upgrade array we will compress - for sure
			 * it will be not optimal (1 we changed, and 1 is equal either way, but if it more then 2, it can be optimal
			 */
		
			upgrade = true;
			was_first_change=true;
			
			// upgrade is true - we change the letters: at index[temp_index_of_iterate] we get char_after_switch_of_iterate
		}
		
		else { 
			upgrade = false;
			content_file_as_bytes[temp_index_of_iterate] = old_char_of_iterate; // we return the content file
		}
	}
	
	private void AddTo_compressed_content_bytes_to_output_file(byte[] compressed_content_bytes_to_output_file, int d, int l,
														int c, int index) {

		String d_str = Integer.toBinaryString(d);
		while (d_str.length() < 8) {
			d_str = "0" + d_str;
		}
		String l_str = Integer.toBinaryString(l);
		while (l_str.length() < 8) {
			l_str = "0" + l_str;
		}
		String d_l_str = d_str.substring(5, 8) + l_str.substring(3, 8);
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
		
		byte[] returned_original_bytes_to_output_file = new byte[(int) input_file.length() * 100];
		
		int index_of_returned_original_bytes = 0;
		
		for (int j = 0; j < compressed_file_as_bytes.length; j++) {
			d = (int) compressed_file_as_bytes[j];
			d = d << 24;
			d = d >>> 24;
			d = d >>> 5;
			l = (int) compressed_file_as_bytes[j];
			l = l << 27; // 32-3
			l = l >>> 27;//32-3
		//	System.out.println("d is: "+d+" l is: "+l);

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
		System.out.println("Decompress succes");
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
		
		
		
		byte[] returned_original_bytes_to_output_file = new byte[(int) input_file.length() * 100];
		
		int index_of_returned_original_bytes = 0;
		
		for (int j = 0; j < compressed_file_as_bytes.length; j++) {
			d = (int) compressed_file_as_bytes[j];
			d = d << 24;
			d = d >>> 24;
			d = d >>> 5;
			l = (int) compressed_file_as_bytes[j];
			l = l << 27; // 32-3
			l = l >>> 27;//32-3
	//		System.out.println("d is: "+d+" l is: "+l);

			if (d == 0) {
				returned_original_bytes_to_output_file[index_of_returned_original_bytes] = compressed_file_as_bytes[j + 1];
				System.out.println("just decompressed ["+index_of_returned_original_bytes +"], the char\"" +(char) returned_original_bytes_to_output_file[index_of_returned_original_bytes]+"\"");
				index_of_returned_original_bytes++;
			} else {
				while (l > 0) {
					returned_original_bytes_to_output_file[index_of_returned_original_bytes] = 
							returned_original_bytes_to_output_file[index_of_returned_original_bytes- d];
					System.out.println("just decompressed ["+index_of_returned_original_bytes +"], the char\"" +(char) returned_original_bytes_to_output_file[index_of_returned_original_bytes]+"\"");
					index_of_returned_original_bytes++;
					l--;
				}
				returned_original_bytes_to_output_file[index_of_returned_original_bytes] = compressed_file_as_bytes[j + 1];
				System.out.println("just decompressed ["+index_of_returned_original_bytes +"], the char\"" +(char) returned_original_bytes_to_output_file[index_of_returned_original_bytes]+"\"");
				index_of_returned_original_bytes++;
			}
			j++;
		}
		byte[] index_of_char_to_fix=new byte[2];
		for (int i = 0; i <fixing_info.length-2; i=i+3) {
			index_of_char_to_fix[0]=fixing_info[i];
			index_of_char_to_fix[1]=fixing_info[i+1];
		//	index_of_char_to_fix[2]=fixing_info[i+2];
		//	index_of_char_to_fix[3]=fixing_info[i+3];
			ByteBuffer wrapped = ByteBuffer.wrap(index_of_char_to_fix);
			short int_index_of_char_to_fix = wrapped.getShort();
			byte original_byte=fixing_info[i+2];
			returned_original_bytes_to_output_file[int_index_of_char_to_fix]=original_byte;
			
			System.out.println("just changed ["+int_index_of_char_to_fix +"],to the char\"" +(char) returned_original_bytes_to_output_file[int_index_of_char_to_fix]+"\"");
			
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


	private void putNumbersAtUpgrade(int[] temp_index_of_upgrade_, byte[] temp_save_old_char, byte[] temp_save_char_after_switch,
				byte[] save_old_char, int[] save_index_of_upgrade, byte[] save_char_after_twist, byte charBeforeChange,
					int j, int step_forward) {
		/*
		 * That's method called when we have new temporary upgrade and we need to save at the arrays the new values
		 */
				temp_save_old_char[number_of_changes]=charBeforeChange;
				temp_index_of_upgrade_[number_of_changes]=j+step_forward;	
				temp_save_char_after_switch[number_of_changes]=content_file_as_bytes[j- tmp_d + step_forward];
				/*
			 			we change at index [number_of_changes] we put at 
			 			temp_save_char_after_switch: content_file_as_bytes[j- tmp_d + step_forward],
						at temp_index_of_upgrade_: (j+step_forward), and at temp-old-char:  charBeforeChange 
				 */
				number_of_changes++; // for the next change
	}
	
}