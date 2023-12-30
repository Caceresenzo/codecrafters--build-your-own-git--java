package git;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class Main {

	public static final Path HERE = Paths.get(".");

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		final var command = args[0];

		switch (command) {
			case "init" -> init();
			case "cat-file" -> catFile(args[2]);
			case "hash-object" -> hashFile(args[2]);
			case "ls-tree" -> lsTree(args[2]);
			case "write-tree" -> writeTree();
			default -> System.out.println("Unknown command: " + command);
		}
	}

	public static void init() throws IOException {
		Git.init(HERE);
		System.out.println("Initialized git directory");
	}

	public static void catFile(String hash) throws IOException {
		final var git = Git.open(HERE);
		final var blob = (Blob) git.readObject(hash);

		System.out.write(blob.data());
	}

	public static void hashFile(String path) throws IOException, NoSuchAlgorithmException {
		final var git = Git.open(HERE);
		final var hash = git.writeBlob(Paths.get(path));

		System.out.println(hash);
	}

	public static void lsTree(String hash) throws IOException, NoSuchAlgorithmException {
		final var git = Git.open(HERE);
		final var tree = (Tree) git.readObject(hash);

		for (final var entry : tree.entries()) {
			System.out.println(entry.name());
		}
	}

	public static void writeTree() throws IOException, NoSuchAlgorithmException {
		final var git = Git.open(HERE);
		final var hash = git.writeTree(HERE);

		System.out.println(hash);
	}

}