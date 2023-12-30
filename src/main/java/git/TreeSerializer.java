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

	public static void serializeEntry(TreeEntry entry, DataOutputStream dataOutputStream) throws IOException {
		System.out.println(entry.mode().format());
		dataOutputStream.write(entry.mode().format().getBytes());
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
		final var hash = dataInputStream.readNBytes(20);
		if (hash.length != 20) {
			return null;
		}

		
		return new TreeEntry(mode, name, hash);
	}
	
	public static TreeEntryMode deserializeEntryMode(String string) {
		final var value = Integer.parseInt(string, 8);
		
		final var type = TreeEntryModeType.match(value);
		final var permission = value & 0b0_111_111_111;
		
		return new TreeEntryMode(type, permission);
	}

}