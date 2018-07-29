
public class Source {
	
		public static void main(String[] args) {

			String input_file = "C:\\\\Users\\\\User\\\\Desktop\\\\Dhisat netunim2\\\\OnTheOrigin.txt";
			String compressed_file = "C:\\\\Users\\\\User\\\\Desktop\\\\Dhisat netunim2\\\\outputtrial.txt";
			String returned_file = "C:\\\\Users\\\\User\\\\Desktop\\\\Dhisat netunim2\\\\outputtrial2.txt";
			LZ77 lz = new LZ77();
			lz.Compress(input_file, compressed_file);
		}
	}


