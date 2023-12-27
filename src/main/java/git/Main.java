package git;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main {

	public static final File HERE = new File(".");

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		final var command = args[0];

		switch (command) {
			case "init" -> init();
			case "cat-file" -> catFile(args[2]);
			case "hash-object" -> hashFile(args[2]);
			default -> System.out.println("Unknown command: " + command);
		}
	}

	public static void init() throws IOException {
		Git.init(HERE);
		System.out.println("Initialized git directory");
	}

	public static void catFile(String blobSha) throws IOException {
		final var git = Git.open(HERE);
		final var bytes = git.catFile(blobSha);

		System.out.write(bytes);
	}

	public static void hashFile(String path) throws IOException, NoSuchAlgorithmException {
		final var git = Git.open(HERE);
		final var hash = git.hashFile(new File(path));

		System.out.println(hash);
	}

}