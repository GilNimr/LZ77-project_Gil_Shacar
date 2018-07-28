public class main {
	public static void main(String[] args) {

		String inp = "C:\\\\Users\\\\User\\\\Desktop\\\\Dhisat netunim2\\\\try.txt";
		String compressed = "C:\\\\Users\\\\User\\\\Desktop\\\\Dhisat netunim2\\\\outputtrial.txt";
		String returned = "C:\\\\Users\\\\User\\\\Desktop\\\\Dhisat netunim2\\\\outputtrial2.txt";
		LZ77 lz = new LZ77();
		lz.Compress(inp, compressed);
	}
}
