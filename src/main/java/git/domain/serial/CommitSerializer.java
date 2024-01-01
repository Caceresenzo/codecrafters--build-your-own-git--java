package git.domain.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.regex.Pattern;

import git.domain.AuthorSignature;
import git.domain.Commit;

public class CommitSerializer implements ObjectContentSerializer<Commit> {

	private static final byte[] SPACE_BYTES = { ' ' };
	private static final byte[] NEW_LINE_BYTES = { '\n' };
	public static final String TREE = "tree";
	private static final byte[] TREE_BYTES = TREE.getBytes();
	public static final String PARENT = "parent";
	private static final byte[] PARENT_BYTES = PARENT.getBytes();
	public static final String AUTHOR = "author";
	private static final byte[] AUTHOR_BYTES = AUTHOR.getBytes();
	public static final String COMMITTER = "committer";
	private static final byte[] COMMITTER_BYTES = COMMITTER.getBytes();

	public static final Pattern AUTHOR_PATTERN = Pattern.compile("^(.*?) <(.*?)> (\\d+) ((?:\\+|-)\\d+)$");

	@Override
	public void serialize(Commit commit, DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.write(TREE_BYTES);
		dataOutputStream.write(SPACE_BYTES);
		dataOutputStream.write(commit.treeHash().getBytes());
		dataOutputStream.write(NEW_LINE_BYTES);

		final var parentHash = commit.parentHash();
		if (parentHash != null) {
			dataOutputStream.write(PARENT_BYTES);
			dataOutputStream.write(SPACE_BYTES);
			dataOutputStream.write(parentHash.getBytes());
			dataOutputStream.write(NEW_LINE_BYTES);
		}

		serializeAuthor(AUTHOR_BYTES, commit.author(), dataOutputStream);
		serializeAuthor(COMMITTER_BYTES, commit.committer(), dataOutputStream);

		dataOutputStream.write(NEW_LINE_BYTES);

		dataOutputStream.write(commit.message().getBytes());
		dataOutputStream.write(NEW_LINE_BYTES);
	}

	public static void serializeAuthor(byte[] keyBytes, AuthorSignature author, DataOutputStream dataOutputStream) throws IOException {
		if (author == null) {
			return;
		}
		
		dataOutputStream.write(keyBytes);
		dataOutputStream.write(SPACE_BYTES);
		dataOutputStream.write(author.format().getBytes());
		dataOutputStream.write(NEW_LINE_BYTES);
	}

	@Override
	public Commit deserialize(DataInputStream dataInputStream) throws IOException {
		final var headers = new HashMap<String, String>();

		final var buffer = new StringBuffer();
		int value;
		while ((value = dataInputStream.read()) != -1) {
			if (value == '\n') {
				if (buffer.isEmpty()) {
					break;
				}

				final var parts = buffer.toString().split(" ", 2);
				headers.put(parts[0], parts[1]);

				buffer.setLength(0);
			} else {
				buffer.append((char) value);
			}
		}

		final var messageBytes = dataInputStream.readAllBytes();
		final var message = new String(messageBytes, 0, messageBytes.length - 1);

		final var treeHash = headers.get(TREE);
		final var parentHash = headers.get(PARENT);
		final var author = parseAuthor(headers.get(AUTHOR));
		final var committer = parseAuthor(headers.get(COMMITTER));

		return new Commit(treeHash, parentHash, author, committer, message);
	}

	public AuthorSignature parseAuthor(String content) {
		if (content == null) {
			return null;
		}

		final var matcher = AUTHOR_PATTERN.matcher(content);
		if (!matcher.find()) {
			throw new IllegalArgumentException("invalid author: " + content);
		}

		final var login = matcher.group(1);
		final var email = matcher.group(2);

		final var instant = Instant.ofEpochSecond(Long.parseLong(matcher.group(3)));
		final var zoneId = ZoneId.of(matcher.group(4));

		final var when = ZonedDateTime.ofInstant(instant, zoneId);

		return new AuthorSignature(login, email, when);
	}

}