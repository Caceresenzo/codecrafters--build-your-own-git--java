package git.domain.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import git.domain.AuthorSignature;
import git.domain.Commit;

public class CommitSerializer implements ObjectSerializer<Commit> {

	private static final byte[] SPACE_BYTES = { ' ' };
	private static final byte[] NEW_LINE_BYTES = { '\n' };
	private static final byte[] TREE_BYTES = "tree".getBytes();
	private static final byte[] PARENT_BYTES = "parent".getBytes();
	private static final byte[] AUTHOR_BYTES = "author".getBytes();
	private static final byte[] COMMITTER_BYTES = "committer".getBytes();

	@Override
	public void serialize(Commit commit, DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.write(TREE_BYTES);
		dataOutputStream.write(SPACE_BYTES);
		dataOutputStream.write(commit.treeHash().getBytes());
		dataOutputStream.write(NEW_LINE_BYTES);

		dataOutputStream.write(PARENT_BYTES);
		dataOutputStream.write(SPACE_BYTES);
		dataOutputStream.write(commit.parentHash().getBytes());
		dataOutputStream.write(NEW_LINE_BYTES);

		serializeAuthor(AUTHOR_BYTES, commit.author(), dataOutputStream);
		serializeAuthor(COMMITTER_BYTES, commit.committer(), dataOutputStream);

		dataOutputStream.write(NEW_LINE_BYTES);

		dataOutputStream.write(commit.message().getBytes());
		dataOutputStream.write(NEW_LINE_BYTES);
	}

	public static void serializeAuthor(byte[] keyBytes, AuthorSignature author, DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.write(keyBytes);
		dataOutputStream.write(SPACE_BYTES);
		dataOutputStream.write(author.format().getBytes());
		dataOutputStream.write(NEW_LINE_BYTES);
	}

	@Override
	public Commit deserialize(DataInputStream dataInputStream) throws IOException {
		throw new IllegalStateException();
	}

}