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
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Git {

	private static final byte[] BLOB_BYTES = "blob".getBytes();
	private static final byte[] SPACE_BYTES = { ' ' };
	private static final byte[] NULL_BYTES = { 0 };

	private final File root;

	public File getDotGit() {
		return new File(root, ".git");
	}

	public File getObjectsDirectory() {
		return new File(getDotGit(), "objects");
	}

	public File getRefsDirectory() {
		return new File(getDotGit(), "refs");
	}

	public File getHeadFile() {
		return new File(getDotGit(), "HEAD");
	}

	public git.Object getObject(String hash) throws FileNotFoundException, IOException {
		final var first2 = hash.substring(0, 2);
		final var remaining38 = hash.substring(2);

		final var file = Paths.get(getObjectsDirectory().getPath(), first2, remaining38).toFile();

		try (
			final var inputStream = new FileInputStream(file);
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
			message.update(objectType.getName().getBytes());
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
			final var first2Directory = new File(getObjectsDirectory(), first2);
			first2Directory.mkdirs();

			final var remaining38 = hash.substring(2);
			final var file = new File(first2Directory, remaining38);

			try (
				final var outputStream = new FileOutputStream(file);
				final var deflaterInputStream = new DeflaterOutputStream(outputStream);
				final var inputStream = Files.newInputStream(temporaryPath)
			) {
				deflaterInputStream.write(BLOB_BYTES);
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

	public static Git init(File root) throws IOException {
		final var git = new Git(root);

		final var dotGit = git.getDotGit();
		if (dotGit.exists()) {
			throw new FileAlreadyExistsException(dotGit.toString());
		}

		git.getObjectsDirectory().mkdirs();
		git.getRefsDirectory().mkdirs();

		final var head = git.getHeadFile();
		head.createNewFile();
		Files.write(head.toPath(), "ref: refs/heads/master\n".getBytes());

		return git;
	}

	public static Git open(File root) throws IOException {
		final var git = new Git(root);

		final var dotGit = git.getDotGit();
		if (!dotGit.exists()) {
			throw new NoSuchFileException(dotGit.toString());
		}

		return git;
	}

}