package training.official.classic_puzzle_easy;

import java.util.HashMap;
import java.util.Scanner;

class MimeType {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int N = in.nextInt(); // Number of elements which make up the association table.
		int Q = in.nextInt(); // Number Q of file names to be analyzed.

		HashMap<String, String> mimeTypes = new HashMap<>();
		for (int i = 0; i < N; i++) {
			String EXT = in.next(); // file extension
			String MT = in.next(); // MIME type.
			mimeTypes.put(EXT.toLowerCase(), MT);
		}
		in.nextLine();

		for (int i = 0; i < Q; i++) {
			String FNAME = in.nextLine().toLowerCase(); // One file name per line.
			String mimeType = "UNKNOWN";

			String[] splittedName = FNAME.split("\\.", -1);
			if (splittedName.length > 1) {
				String fileExtension = splittedName[splittedName.length - 1];
				mimeType = mimeTypes.get(fileExtension);
			}

			System.out.println(mimeType != null ? mimeType : "UNKNOWN");
		}
	}
}
