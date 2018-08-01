
public class Source {
	
		public static void main(String[] args) {


			String input_file = "C:\\Users\\shach\\Desktop\\Dhisat netunim2\\try.txt";
			String compressed_file = "C:\\Users\\shach\\Desktop\\Dhisat netunim2\\outputtrial.txt";
			String returned_file = "C:\\Users\\shach\\Desktop\\Dhisat netunim2\\outputtrial2.txt";
			
			String upgrade_side_file = "C:\\Users\\shach\\Desktop\\Dhisat netunim2\\upgrade_side_file.txt";
			
			LZ77 lz = new LZ77();
			//lz.Compress(input_file, compressed_file);
			
			lz.CompressWithUpgrade(input_file, compressed_file, upgrade_side_file);
			
			System.out.println("Succeed");

			

		}
	}


