package git;

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

	public byte[] catFile(String blobSha) throws FileNotFoundException, IOException {
		final var first2 = blobSha.substring(0, 2);
		final var remaining38 = blobSha.substring(2);

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

			final var objectType = builder.toString();
			//			System.out.println(objectType);

			builder.setLength(0);
			while ((value = inflaterInputStream.read()) != -1 && value != 0) {
				builder.append((char) value);
			}

			final var objectLength = Long.parseLong(builder.toString());
			//			System.out.println(objectLength);

			return inflaterInputStream.readAllBytes();
		}
	}

	public String hashFile(File file) throws IOException, NoSuchAlgorithmException {
		try (final var inputStream = new FileInputStream(file)) {
			return hashFile(inputStream.readAllBytes());
		}
	}
	
	public String hashFile(byte[] data) throws IOException, NoSuchAlgorithmException {
		final var hashBytes = MessageDigest.getInstance("SHA-1").digest(data);
		final var hash = HexFormat.of().formatHex(hashBytes);
		
		final var first2 = hash.substring(0, 2);
		final var first2Directory = new File(getObjectsDirectory(), first2);
		first2Directory.mkdirs();
		
		final var remaining38 = hash.substring(2);
		final var file = new File(first2Directory, remaining38);
		
		try (
			final var outputStream = new FileOutputStream(file);
			final var deflaterInputStream = new DeflaterOutputStream(outputStream)
			) {
			deflaterInputStream.write(BLOB_BYTES);
			deflaterInputStream.write(' ');
			deflaterInputStream.write(String.valueOf(hashBytes.length).getBytes());
			deflaterInputStream.write(0);
			deflaterInputStream.write(data);
		}
		
		return hash;
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