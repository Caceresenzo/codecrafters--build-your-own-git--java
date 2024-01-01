package git.pack;

import git.pack.DeltaInstruction.Insert;
import git.pack.DeltaInstruction.Copy;

public sealed interface DeltaInstruction permits Copy, Insert {

	public record Copy(
		int offset,
		int size
	) implements DeltaInstruction {}

	public record Insert(
		byte[] data
	) implements DeltaInstruction {}

	public static Copy copy(int offset, int size) {
		return new Copy(offset, size);
	}

	public static Insert insert(byte[] data) {
		return new Insert(data);
	}

}