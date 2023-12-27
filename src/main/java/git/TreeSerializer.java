package git;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class TreeSerializer implements ObjectSerializer<Tree> {

	@Override
	public void serialize(Tree tree, DataOutputStream dataOutputStream) throws IOException {
		for (final var entry : tree.entries()) {
			entry.serialize(dataOutputStream);
		}
	}

	public void serializeEntry(TreeEntry entry, DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.write(entry.mode().getBytes());
		dataOutputStream.write(' ');
		dataOutputStream.write(entry.name().getBytes());
		dataOutputStream.write('\0');
		dataOutputStream.write(entry.hash());
	}

	@Override
	public Tree deserialize(DataInputStream dataInputStream) throws IOException {
		final var entries = new ArrayList<TreeEntry>();

		TreeEntry entry;
		while ((entry = deserializeEntry(dataInputStream)) != null) {
			entries.add(entry);
		}

		return new Tree(Collections.unmodifiableList(entries));
	}

	public TreeEntry deserializeEntry(DataInputStream dataInputStream) throws IOException {
		final var builder = new StringBuilder();

		int value;
		while ((value = dataInputStream.read()) != -1 && value != ' ') {
			builder.append((char) value);
		}

		if (value == -1) {
			return null;
		}

		final var mode = builder.toString();

		builder.setLength(0);
		while ((value = dataInputStream.read()) > 0) {
			builder.append((char) value);
		}

		if (value == -1) {
			return null;
		}

		final var name = builder.toString();
		final var hash = dataInputStream.readNBytes(20);
		if (hash.length != 20) {
			return null;
		}

		return new TreeEntry(mode, name, hash);
	}

}