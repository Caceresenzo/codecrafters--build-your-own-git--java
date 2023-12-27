package git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.zip.DeflaterInputStream;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Git {

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
			final var deflaterInputStream = new DeflaterInputStream(inputStream)
		) {
			return deflaterInputStream.readAllBytes();
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