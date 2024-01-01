package git.domain.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import git.Git;
import git.domain.Tree;
import git.domain.tree.TreeEntry;
import git.domain.tree.TreeEntryMode;
import git.domain.tree.TreeEntryModeType;

public class TreeSerializer implements ObjectContentSerializer<Tree> {

	@Override
	public void serialize(Tree tree, DataOutputStream dataOutputStream) throws IOException {
		for (final var entry : tree.entries()) {
			serializeEntry(entry, dataOutputStream);
		}
	}

	public static void serializeEntry(TreeEntry entry, DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.write(entry.mode().format().getBytes());
		dataOutputStream.write(' ');
		dataOutputStream.write(entry.name().getBytes());
		dataOutputStream.write('\0');
		dataOutputStream.write(Git.HEX.parseHex(entry.hash()));
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

	public static TreeEntry deserializeEntry(DataInputStream dataInputStream) throws IOException {
		final var builder = new StringBuilder();

		int value;
		while ((value = dataInputStream.read()) != -1 && value != ' ') {
			builder.append((char) value);
		}

		if (value == -1) {
			return null;
		}

		final var mode = deserializeEntryMode(builder.toString());

		builder.setLength(0);
		while ((value = dataInputStream.read()) > 0) {
			builder.append((char) value);
		}

		if (value == -1) {
			return null;
		}

		final var name = builder.toString();
		final var hashBytes = dataInputStream.readNBytes(20);
		if (hashBytes.length != 20) {
			return null;
		}

		final var hash = Git.HEX.formatHex(hashBytes);

		return new TreeEntry(mode, name, hash);
	}

	public static TreeEntryMode deserializeEntryMode(String string) {
		final var value = Integer.parseInt(string, 8);

		final var type = TreeEntryModeType.match(value);
		final var permission = value & 0b0_111_111_111;

		return new TreeEntryMode(type, permission);
	}

}