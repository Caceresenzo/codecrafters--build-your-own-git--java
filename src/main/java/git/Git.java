package git;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import git.util.Platform;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Git {

	public static final HexFormat HEX = HexFormat.of();
	public static final Set<Path> FORBIDDEN_DIRECTORIES = Set.of(
		Paths.get(".git")
	);

	private static final byte[] SPACE_BYTES = { ' ' };
	private static final byte[] NULL_BYTES = { 0 };

	private final Path root;

	public Path getDotGit() {
		return root.resolve(".git");
	}

	public Path getObjectsDirectory() {
		return getDotGit().resolve("objects");
	}

	public Path getRefsDirectory() {
		return getDotGit().resolve("refs");
	}

	public Path getHeadFile() {
		return getDotGit().resolve("HEAD");
	}

	public git.Object readObject(String hash) throws FileNotFoundException, IOException {
		final var first2 = hash.substring(0, 2);
		final var remaining38 = hash.substring(2);

		final var path = getObjectsDirectory().resolve(first2).resolve(remaining38);

		try (
			final var inputStream = new FileInputStream(path.toFile());
			final var inflaterInputStream = new InflaterInputStream(inputStream)
		) {
			final var builder = new StringBuilder();

			int value;
			while ((value = inflaterInputStream.read()) != -1 && value != ' ') {
				builder.append((char) value);
			}

			final var objectType = ObjectType.byName(builder.toString());

			builder.setLength(0);
			while ((value = inflaterInputStream.read()) != -1 && value != 0) {
				builder.append((char) value);
			}

			@SuppressWarnings("unused")
			final var objectLength = Integer.parseInt(builder.toString());

			return objectType.deserialize(new DataInputStream(inflaterInputStream));
		}
	}

	@SuppressWarnings("unchecked")
	public String writeObject(git.Object object) throws IOException, NoSuchAlgorithmException {
		final var objectType = ObjectType.byClass(object.getClass());
		final var objectTypeBytes = objectType.getName().getBytes();

		final var temporaryPath = Files.createTempFile("temp-", ".tmp");

		try {
			try (
				final var outputStream = Files.newOutputStream(temporaryPath);
				final var dataOutputStream = new DataOutputStream(outputStream)
			) {
				objectType.serialize(object, dataOutputStream);
			}

			final var length = Files.size(temporaryPath);
			final var lengthBytes = String.valueOf(length).getBytes();

			final var message = MessageDigest.getInstance("SHA-1");
			message.update(objectTypeBytes);
			message.update(SPACE_BYTES);
			message.update(lengthBytes);
			message.update(NULL_BYTES);

			try (
				final var inputStream = Files.newInputStream(temporaryPath);
			) {
				final var buffer = new byte[1024];

				int read;
				while ((read = inputStream.read(buffer)) > 0) {
					message.update(buffer, 0, read);
				}
			}

			final var hashBytes = message.digest();
			final var hash = HexFormat.of().formatHex(hashBytes);

			final var first2 = hash.substring(0, 2);
			final var first2Directory = new File(getObjectsDirectory().toFile(), first2);
			first2Directory.mkdirs();

			final var remaining38 = hash.substring(2);
			final var file = new File(first2Directory, remaining38);

			try (
				final var outputStream = new FileOutputStream(file);
				final var deflaterInputStream = new DeflaterOutputStream(outputStream);
				final var inputStream = Files.newInputStream(temporaryPath)
			) {
				deflaterInputStream.write(objectTypeBytes);
				deflaterInputStream.write(SPACE_BYTES);
				deflaterInputStream.write(lengthBytes);
				deflaterInputStream.write(NULL_BYTES);
				inputStream.transferTo(deflaterInputStream);
			}

			return hash;
		} finally {
			Files.deleteIfExists(temporaryPath);
		}
	}

	public String writeBlob(Path path) throws IOException, NoSuchAlgorithmException {
		final var bytes = Files.readAllBytes(path);
		final var blob = new Blob(bytes);

		return writeObject(blob);
	}

	public String writeTree(Path root) throws IOException, NoSuchAlgorithmException {
		final var fileNames = Files.list(root)
			.map(Path::getFileName)
			.filter(Predicate.not(FORBIDDEN_DIRECTORIES::contains))
			.toList();

		final var entries = new ArrayList<TreeEntry>();

		for (final var fileName : fileNames) {
			final var path = root.resolve(fileName);

			String hashString;
			TreeEntryMode mode;

			if (Files.isDirectory(path)) {
				hashString = writeTree(path);
				mode = TreeEntryMode.directory();
			} else if (Files.isRegularFile(path)) {
				hashString = writeBlob(path);

				if (Platform.isWindows()) {
					mode = TreeEntryMode.regularFile(0644);
				} else {
					final var attributes = Files.readAttributes(path, PosixFileAttributes.class);
					mode = TreeEntryMode.regularFile(attributes);
				}
			} else {
				continue;
			}

			final var hash = HEX.parseHex(hashString);
			entries.add(new TreeEntry(mode, fileName.toString(), hash));
		}

		final var tree = new Tree(entries);

		return writeObject(tree);
	}

	public static Git init(Path root) throws IOException {
		final var git = new Git(root);

		final var dotGit = git.getDotGit();
		if (Files.exists(dotGit)) {
			throw new FileAlreadyExistsException(dotGit.toString());
		}

		Files.createDirectories(git.getObjectsDirectory());
		Files.createDirectories(git.getRefsDirectory());

		final var head = git.getHeadFile();
		Files.createFile(head);
		Files.write(head, "ref: refs/heads/master\n".getBytes());

		return git;
	}

	public static Git open(Path root) throws IOException {
		final var git = new Git(root);

		final var dotGit = git.getDotGit();
		if (!Files.exists(dotGit)) {
			throw new NoSuchFileException(dotGit.toString());
		}

		return git;
	}

}