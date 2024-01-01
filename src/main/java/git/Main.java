package git;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.zip.DataFormatException;

import git.domain.AuthorSignature;

public class Main {

	public static final Path HERE = Paths.get(".");

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, DataFormatException {
		final var command = args[0];

		switch (command) {
			case "init" -> init();
			case "cat-file" -> catFile(args[2]);
			case "hash-object" -> hashFile(args[2]);
			case "ls-tree" -> lsTree(args[2]);
			case "write-tree" -> writeTree();
			case "commit-tree" -> commitTree(args[1], args[3], args[5]);
			case "clone" -> clone(args[1], args[2]);
			default -> System.out.println("Unknown command: " + command);
		}
	}

	public static void init() throws IOException {
		Git.init(HERE);
		System.out.println("Initialized git directory");
	}

	public static void catFile(String hash) throws IOException {
		final var git = Git.open(HERE);
		final var blob = git.readBlob(hash);

		System.out.write(blob.data());
	}

	public static void hashFile(String path) throws IOException, NoSuchAlgorithmException {
		final var git = Git.open(HERE);
		final var hash = git.writeBlob(Paths.get(path));

		System.out.println(hash);
	}

	public static void lsTree(String hash) throws IOException, NoSuchAlgorithmException {
		final var git = Git.open(HERE);
		final var tree = git.readTree(hash);

		for (final var entry : tree.entries()) {
			System.out.println(entry.name());
		}
	}

	public static void writeTree() throws IOException, NoSuchAlgorithmException {
		final var git = Git.open(HERE);
		final var hash = git.writeTree(HERE);

		System.out.println(hash);
	}

	public static void commitTree(String treeHash, String parentHash, String message) throws IOException, NoSuchAlgorithmException {
		final var git = Git.open(HERE);

		final var enzo = new AuthorSignature("Caceresenzo", "caceresenzo1502@gmail.com", ZonedDateTime.now());
		final var hash = git.writeCommit(treeHash, parentHash, enzo, message);

		System.out.println(hash);
	}

	public static void clone(String uri, String path) throws IOException, NoSuchAlgorithmException, DataFormatException {
		Git.clone(URI.create(uri), Paths.get(path));

		System.out.println("Cloned git repository");
	}

}