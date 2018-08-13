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
	
	private int sliding_window; // the sliding_window, later will be maximum of 31
	private int tmp_d; // variable for finding j.
	private int d; // how much going back.
	private int tmp_l; // variable for finding l.
	private int l; // length of characters to copy.
	private int index_of_compressed_content_bytes_to_output_file=0; // index for appointing bytes to 
	
	
		//compressed_content_bytes_to_output_file variable
	private  final int look_a_head_buffer; // the look a head buffer, maximum of 7
	private byte[] content_file_as_bytes; // array  of all the bytes of the file.
	private byte[] compressed_content_bytes_to_output_file; //array  of all the bytes in the output file.
	private char c;
	
	//upgrade compress definitions
	private int[] indexes_of_changes;	// all the indexes that we will change before decompress
	private byte[] letters_to_save;		// all the chars as byte that we will change before decompress at indexes_of_changes
	private boolean upgrade; // true if we going to save the upgrade - just at compress at specific iterate of k
	private boolean use_the_upgrade;	// true if we finished k iterate and we going to use the upgrade
	
	
	private boolean was_first_change;	//   will be off each j iterate, will be true after first use at upgrade because of saving the 
									// changes if we will not use specific upgrade at that time
	private int number_of_changes;	// how much changes at each j iterate. counter
//	private boolean optimate; // will be on if with upgrade will get to the end of iterate
	
//	private int specific_index_of_changes; 
	

	

	

	LZ77() { 
		sliding_window = tmp_d =
				d = tmp_l = l = index_of_compressed_content_bytes_to_output_file = 0;
		look_a_head_buffer = 7;

	}
	
	
	public void Generate_String(String output_file_path) {
		System.out
				.println("hi this method geneartes strings with repetition with few mismatched symbols \n"
						+ "for example: ababababfbabababgbab");
		StringBuilder str = new StringBuilder();
		
		double probabilty;

		for (int i = 0; i < 10000; i++) {
			probabilty = Math.random();
			if (probabilty > 0.1)
				str.append('a');
			else
				str.append((char)(65+(Math.random()*35)));
			probabilty = Math.random();
			if (probabilty > 0.1)
				str.append('b');
			else
				str.append((char)(65+(Math.random()*35)));
		}
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
		
//		System.out.println("Compress with upgrade");
		File input_file = new File(input_file_path);
		content_file_as_bytes = new byte[(int) input_file.length()]; 
		compressed_content_bytes_to_output_file = new byte[(int) input_file.length() * 2];
	//	specific_index_of_changes=0;
		
		indexes_of_changes = new int[(int) input_file.length()];
		int indexesAtFinito=0;
		int final_number_of_changes;
		letters_to_save = new byte[(int) input_file.length()];
		
		
		try {
			FileInputStream fileInputStream = new FileInputStream(input_file);
			fileInputStream.read(content_file_as_bytes); // reading all the file into content_file_as_bytes.
			
			for(int i=0; i<content_file_as_bytes.length; i++) {
				System.out.println("["+i+"]: " +(char) content_file_as_bytes[i]);
			}

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Reading The File.");
			e1.printStackTrace();
		}
	
	//	System.out.println("Reading the text...");
		
		for (int j = 0; j < content_file_as_bytes.length; j++) {
			
			use_the_upgrade = /*optimate = */was_first_change=false; 
			number_of_changes= final_number_of_changes=0;
			
			c = (char) content_file_as_bytes[j];
			byte[] temp_save_char_after_switch = new byte[look_a_head_buffer+1];
			byte[] save_old_char = new byte[look_a_head_buffer+1];
			int[] temp_index_of_upgrade_= new int[look_a_head_buffer+1]; //all the indexes  we save inside k iterate if use upgrade - will change until end of k loop 	
			byte[] temp_save_old_char= new byte[look_a_head_buffer+1];	// all the chars (before change) we save inside k iterate - will change until end of k loop 
			byte[] save_char_after_twist = new byte[look_a_head_buffer+1];
			int[] save_index_of_upgrade= new int[look_a_head_buffer+1]; // will save indexes about changes, each new letter will restart
//			int temp_specific_index=0;
			
			
			
			for (int i=0; i<look_a_head_buffer+1; i++) {
				save_char_after_twist[i]= temp_save_char_after_switch[i] =
				save_old_char[i] =temp_save_old_char[i] = 0;
				save_index_of_upgrade[i] =temp_index_of_upgrade_[i] = 0;  
			}
				
			sliding_window = j;
			if (sliding_window > 31)
				sliding_window = 31;

			System.out.println("we are reading index [" + j +"], it's \"" + c + "\", and our sliding window size is " + sliding_window + "\n");
			
			for (int k = 0; k < sliding_window; k++) {
				number_of_changes=0;
				for (int i=0; i<look_a_head_buffer+1; i++) {
					temp_save_char_after_switch[i] = temp_save_old_char[i] = 0;
					temp_index_of_upgrade_[i] = 0;  
				}
				
			/*	if (optimate) {
					break;
				}*/
				
				tmp_l = 0;
				tmp_d = sliding_window - k;
				upgrade = false;
				int step_forward = 0;
				System.out.println("Checking copy from index[" + (j- tmp_d + step_forward) +"]");
				System.out.println("we try to copy [" + (j- tmp_d + step_forward) +"]\"" + (char) content_file_as_bytes[j- tmp_d + step_forward] + "\" and we read \"" +
						 (char) content_file_as_bytes[j + step_forward] + "\", so " + 
								((content_file_as_bytes[j + step_forward] == content_file_as_bytes[j- tmp_d + step_forward])));
				
		
				if (!(content_file_as_bytes[j + step_forward] == content_file_as_bytes[j- tmp_d + step_forward])) {
					
					
					if (j + step_forward+1 < content_file_as_bytes.length) { 
						System.out.println("Because \""+(char) content_file_as_bytes[j+step_forward]+
								"\"  unequal to \""  + (char) content_file_as_bytes[j- tmp_d + step_forward]
										+"\" , now we need to check about upgrade,\n The next step of letter will is: copy: \"" +
								(char) content_file_as_bytes[j - tmp_d + step_forward+1] + "\" and read: \"" + 
										(char) content_file_as_bytes[j + step_forward+1] + "\" so it is " + 
										(content_file_as_bytes[j + step_forward+1] == 
										content_file_as_bytes[j - tmp_d + step_forward+1]) + " equal");
					
						if ((content_file_as_bytes[j + step_forward+1] == content_file_as_bytes[j - tmp_d + step_forward+1])) {
							System.out.println("And because its equal, we send it to check upgrade");
							
							boolean saveFirst = was_first_change;
							byte charBeforeChange = content_file_as_bytes[j + step_forward];
							
							checkIfUpgrade(tmp_l, step_forward, j, temp_index_of_upgrade_, temp_save_old_char, temp_save_char_after_switch,  save_old_char, save_index_of_upgrade, save_char_after_twist);
							
							if (upgrade) {
								putNumbersAtUpgrade(temp_index_of_upgrade_, temp_save_old_char, temp_save_char_after_switch, temp_save_old_char, save_index_of_upgrade, save_char_after_twist, charBeforeChange, j, step_forward, saveFirst);
								
							}

						}
					}	
				}
				
				
				while ((content_file_as_bytes[j + step_forward] == content_file_as_bytes[j
						- tmp_d + step_forward])) {
					
					boolean loopWithUpgrade = upgrade;
					tmp_l++;
					step_forward++;
					
					
					
					
					if ((j + step_forward >= content_file_as_bytes.length)
							|| (step_forward >= look_a_head_buffer))
						break;
					
					
					
					// added today
					System.out.println("index: [" + (j + step_forward) + "]: we try to copy \"" + (char) content_file_as_bytes[j- tmp_d + step_forward] +
							"\" (at indexOfSlidingWindow: ["+ (j-tmp_d+step_forward)+"]),  and we read \"" +
					 (char) content_file_as_bytes[j + step_forward] + "\", so " + 
							((content_file_as_bytes[j + step_forward] == content_file_as_bytes[j- tmp_d + step_forward])));
				
					
					// end today
					
						
					if (!(content_file_as_bytes[j + step_forward] == content_file_as_bytes[j- tmp_d + step_forward])) {
				
						
				
						 if (j + step_forward+1 < content_file_as_bytes.length) { 
							System.out.println("The next letter is \""+(char) content_file_as_bytes[j+step_forward]+
									"\" and it's unequal to \""  + (char) content_file_as_bytes[j- tmp_d + step_forward]
											+"\" so now we need to check about upgrade,\n the next step of letter is: from sliding_window: \"" +
									(char) content_file_as_bytes[j - tmp_d + step_forward+1] + "\" and will read: \"" + (char) content_file_as_bytes[j + step_forward+1] 
											+ "\" so it is " + (content_file_as_bytes[j + step_forward+1] == content_file_as_bytes[j - tmp_d + step_forward+1]) + " equal");
						
							if ((content_file_as_bytes[j + step_forward+1] == content_file_as_bytes[j - tmp_d + step_forward+1])) {
								System.out.println("And because its equal, we send it to check upgrade");
								
								byte charBeforeChange = content_file_as_bytes[j + step_forward];
								boolean saveFirst = was_first_change;
								boolean saveIfUpgrade = loopWithUpgrade;
								checkIfUpgrade(tmp_l, step_forward, j, temp_index_of_upgrade_, temp_save_old_char, temp_save_char_after_switch,  save_old_char, save_index_of_upgrade, save_char_after_twist);
							
								  if (upgrade) {
									  
									 putNumbersAtUpgrade(temp_index_of_upgrade_, temp_save_old_char, temp_save_char_after_switch, temp_save_old_char, save_index_of_upgrade, save_char_after_twist, charBeforeChange, j, step_forward, saveFirst);
								}
								  else if (saveIfUpgrade) {
									  upgrade = true;
								  }
							}
						}	
					}
					
				} // end while

				if ( (tmp_l > l) ||   ( (tmp_l==l) && (!upgrade) ) ){
					
					if (upgrade) {
						
						
						//if (was_first_change) {
			//				specific_index_of_changes = temp_specific_index;


						final_number_of_changes = number_of_changes;
	//					temp_specific_index = specific_index_of_changes;
						use_the_upgrade=true;
						for (int i=0; i<number_of_changes; i++) {
							save_old_char[i] = temp_save_old_char[i];
							save_index_of_upgrade[i] = temp_index_of_upgrade_[i];	
							save_char_after_twist[i] = temp_save_char_after_switch[i];
							content_file_as_bytes[temp_index_of_upgrade_[i]] = save_old_char[i];
							
							 
							
							
					/*		System.out.println("upgrading....:  \nold char[" + i +"] "+(char) temp_save_old_char[i]
							+", save_index_of_upgrade[" + i +"] \" = " + temp_index_of_upgrade_[i]);
							
							 deleted today: it was inside print
							+ ", save_char_after_twist[i] = "+
									(char) temp_save_char_after_switch[i]);
							*/
						}
					}
					
					else {
						use_the_upgrade = false;
//						specific_index_of_changes = temp_specific_index;
						
						if (was_first_change) {
							for (int i=0; i<number_of_changes; i++) {							
								content_file_as_bytes[save_index_of_upgrade[i]] = save_old_char[i];
							}
						}
					}
					
					l = tmp_l;
					d = tmp_d;
					if (j + step_forward + 1 <= content_file_as_bytes.length)    /// ------added <=
						c = (char) content_file_as_bytes[j + step_forward];
					else
						c = ' ';
					
					System.out.println("we will save that's k [" + (k+1) +"] as: ("+d+", " + l + ", " + c +")");
					
				}
				
				// added today
				if (step_forward >= look_a_head_buffer) {
					break;
				}
				// ended today
			} // end k
			
			if (use_the_upgrade) {
				System.out.println("We use upgrade:");
				
				for (int i=0; i<final_number_of_changes; i++) {
					content_file_as_bytes[save_index_of_upgrade[i]] =save_char_after_twist[i];	
				}
				
				
				System.out.println("we finish k and we use the upgrade");
				
				for (int i=0; i<final_number_of_changes; i++) {
					
					/*
					indexes_of_changes[specific_index_of_changes] = save_index_of_upgrade[i];
					letters_to_save[specific_index_of_changes] = save_old_char[i];
					*
					*/
					
					indexes_of_changes[indexesAtFinito] = save_index_of_upgrade[i];
					letters_to_save[indexesAtFinito] = save_old_char[i];
					
					System.out.println("we save upgrade: at index [" + indexes_of_changes[indexesAtFinito]
							+ "], its now: \"" + (char) save_char_after_twist[i] + "\", we will change to \"" + (char) letters_to_save[indexesAtFinito] + "\"");
					
					indexesAtFinito++;
					
					
				}
				
				
			}
			else {
		//		System.out.println("we dont use upgrade");
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
			
			/*int count =0;
			for (int i = 0; i < indexes_of_changes.length; i++) {
				if ( (i>0) && (indexes_of_changes[i] == 0)) {
					break;
				}
				count++;
			}*/
			
			System.out.println("Decompress with that changes: ");
		    out.writeInt(indexesAtFinito*5);
			for (int i = 0; i < indexesAtFinito; i++) {
				
				System.out.println("at index: [" + indexes_of_changes[i] + "] change to \"" + (char) letters_to_save[i] + "\"");
				
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

			if ((indexes_of_changes[i] == 0) && (i > 0) ) {
				break;
			}
		}
	}
	
	
	
	
	private void checkIfUpgrade(int l_, int step_forward_, int j_, int[] temp_index_of_upgrade_, byte[] temp_save_old_char, 
			byte[] temp_save_char_after_switch, byte[] save_old_char, int[] save_index_of_upgrade, byte[] save_char_after_twist) {

		byte old_char_of_iterate=content_file_as_bytes[j_ + step_forward_];
		byte char_after_switch_of_iterate = content_file_as_bytes[j_- tmp_d + step_forward_];
		int temp_index_of_iterate = j_+step_forward_;
		
		
		int added_to_l = 0;

		
		if (char_after_switch_of_iterate == 0) {
			System.out.println("here is the 0, we are at function");
		}
		
		content_file_as_bytes[j_ + step_forward_] = char_after_switch_of_iterate;
		
	/*	System.out.println("Checkimg upgrade.....");
		System.out.println("We sent the char \"" + (char) old_char_of_iterate + "\" to check if we will switch her to \""
				+ (char) char_after_switch_of_iterate + "\" at index " + temp_index_of_iterate + " of the source input it will be better then do it as "
						+ "usual lz77");
		*/
		System.out.println("So for temp, at index["+temp_index_of_iterate+"] we get \"" + (char) char_after_switch_of_iterate+"\"");
		
		do {
			added_to_l++;
			step_forward_++;

	//		System.out.println("the length is :" + content_file_as_bytes.length + " and j+step is: " + (j_+step_forward_));
				if ((j_ + step_forward_+1 >= content_file_as_bytes.length)|| (step_forward_ >= look_a_head_buffer)) {
					
			//		added_to_l=3;
					
						// added thirday
					
					/*if (number_of_changes>0) {
						if (temp_index_of_upgrade_[number_of_changes-1] != temp_index_of_iterate) {
							temp_save_old_char[number_of_changes]=old_char_of_iterate;
							temp_index_of_upgrade_[number_of_changes]=temp_index_of_iterate;	
							//deleted thirday temp_save_char_after_switch[number_of_changes]=char_after_switch_of_iterate;
							number_of_changes++;
							
							System.out.println("We saving at upgradeArray \"" + (char) old_char_of_iterate + "\" at index [" + temp_index_of_iterate+"]");
						}
					}*//*
					else {
						temp_save_old_char[number_of_changes]=old_char_of_iterate;
						temp_index_of_upgrade_[number_of_changes]=temp_index_of_iterate;	
						//deleted thirday temp_save_char_after_switch[number_of_changes]=char_after_switch_of_iterate;
						number_of_changes++;
						
						System.out.println("We saving at upgradeArray \"" + (char) old_char_of_iterate + "\" at index [" + temp_index_of_iterate+"]");
					}*/
		
					// end thirday
					
		//			System.out.println("we see that the switch was good! because we finished to compress");
				//	optimate=true;
					break;
				}
				
				System.out.println("(still checking):\n"
						+ "reading index["+(step_forward_+j_)+"], it's \" " +(char) content_file_as_bytes[step_forward_+j_] +
						"\", sliding_window is at index[ "+(j_- tmp_d + step_forward_)+"], so will copy \""+
				(char) content_file_as_bytes[j_- tmp_d + step_forward_] + "\", so "+(content_file_as_bytes[j_ + step_forward_] ==
				content_file_as_bytes[j_- tmp_d + step_forward_]));
			 	

				
				if (!(content_file_as_bytes[j_ + step_forward_] == content_file_as_bytes[j_- tmp_d + step_forward_])) {
					
					if (j_ + step_forward_+1 < content_file_as_bytes.length) {  
						
						System.out.println("The next letter is \""+(char) content_file_as_bytes[j_+step_forward_]+
								"\" and it's unequal to \""  + (char) content_file_as_bytes[j_- tmp_d + step_forward_]
										+"\" so now we need to check about upgrade,\n the next step of letter is: copy: \"" +
								(char) content_file_as_bytes[j_ - tmp_d + step_forward_+1] + "\" and read: \"" + (char) content_file_as_bytes[j_ + step_forward_+1] 
										+ "\" so it is " + (content_file_as_bytes[j_ + step_forward_+1] == content_file_as_bytes[j_ - tmp_d + step_forward_+1]) + " equal");
				
						if ((content_file_as_bytes[j_ + step_forward_+1] == content_file_as_bytes[j_ - tmp_d + step_forward_+1])) {
							System.out.println("And because its equal, we send it to check upgrade");
							
							int temp_step = step_forward_;
							int temp_j = j_;
							
						//	byte savingNewChar = content_file_as_bytes[j_- tmp_d + step_forward_]; // if we change
							byte charBeforeChange = content_file_as_bytes[j_+step_forward_]; // if we will not change
						//	int savingNowIndex = (j_+step_forward_);
							
							
							checkIfUpgrade(tmp_l, temp_step, temp_j, temp_index_of_upgrade_, temp_save_old_char, temp_save_char_after_switch , save_old_char, save_index_of_upgrade, save_char_after_twist);
							
							
							
							if (upgrade) {
								putNumbersAtUpgrade(temp_index_of_upgrade_, temp_save_old_char, temp_save_char_after_switch, save_old_char, save_index_of_upgrade, save_char_after_twist, charBeforeChange, j_, step_forward_, true);
								
							}
						}
					}	
					
				}
				
				// end added today
				
		} while ((content_file_as_bytes[j_ + step_forward_] == content_file_as_bytes[j_- tmp_d + step_forward_]));
		
		if (added_to_l > 2) {
		
			upgrade = true;
			System.out.println("was_first is now true");
			was_first_change=true;
			
			System.out.println("upgrade is true - we change the letters: "
					+ "at index[" +temp_index_of_iterate+"] we get \"" + (char) char_after_switch_of_iterate+"\"");

		
			
		}
		else {
			upgrade = false;
			
			if (old_char_of_iterate ==0) {
				System.out.println("we still at function and it 0 going back to content text");
			}
			
			
			content_file_as_bytes[temp_index_of_iterate] = old_char_of_iterate;
			System.out.println("Upgrade is false. we back the index [" +temp_index_of_iterate+"] to \"" + (char)old_char_of_iterate );
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
		
		byte[] returned_original_bytes_to_output_file = new byte[(int) input_file.length() * 100];
		
		int index_of_returned_original_bytes = 0;
		
		for (int j = 0; j < compressed_file_as_bytes.length; j++) {
			d = (int) compressed_file_as_bytes[j];
			d = d << 24;
			d = d >>> 24;
			d = d >>> 3;
			l = (int) compressed_file_as_bytes[j];
			l = l << 29; // 32-3
			l = l >>> 29;//32-3
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
			d = d >>> 3;
			l = (int) compressed_file_as_bytes[j];
			l = l << 29; // 32-3
			l = l >>> 29;//32-3
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
			
			System.out.println("just decompressed ["+int_index_of_char_to_fix +"], the char\"" +(char) returned_original_bytes_to_output_file[int_index_of_char_to_fix]+"\"");
			
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
					int j, int step_forward, boolean saveFirst) {
		  
		  if(number_of_changes >0) {
			  if (temp_index_of_upgrade_[number_of_changes-1] != (j+step_forward)) {
					temp_save_old_char[number_of_changes]=charBeforeChange;
					temp_index_of_upgrade_[number_of_changes]=j+step_forward;	
					temp_save_char_after_switch[number_of_changes]=content_file_as_bytes[j- tmp_d + step_forward];
				//	content_file_as_bytes[j+step_forward] = content_file_as_bytes[j- tmp_d + step_forward];
					number_of_changes++;
					
				System.out.println("This is the " + number_of_changes + "number of change.\nnew char: " + content_file_as_bytes[j+step_forward]+
							", at index: " + j+step_forward + ". old char: " + charBeforeChange);
				}
		  }
		  else {
			  temp_save_old_char[number_of_changes]=charBeforeChange;
				temp_index_of_upgrade_[number_of_changes]=j+step_forward;	
				temp_save_char_after_switch[number_of_changes]=content_file_as_bytes[j- tmp_d + step_forward];
			//	content_file_as_bytes[j+step_forward] = content_file_as_bytes[j- tmp_d + step_forward];
				number_of_changes++;
				
			System.out.println("This is the " + number_of_changes + "number of change.\nnew char: " + (char)content_file_as_bytes[j+step_forward]+
						", at index: " + j+step_forward + ". old char: " +(char) charBeforeChange);
		 }
		  
		  
			
		 if (!saveFirst) {
			 for (int i=0; i<number_of_changes; i++) {
					save_old_char[i] = temp_save_old_char[i];
					save_index_of_upgrade[i] = temp_index_of_upgrade_[i];	
					save_char_after_twist[i] = temp_save_char_after_switch[i];
			 }
		 }
	}
	
}