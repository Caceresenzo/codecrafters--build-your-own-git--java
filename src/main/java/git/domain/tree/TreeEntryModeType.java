package git.domain.tree;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public enum TreeEntryModeType {

	DIRECTORY(0b0100),
	REGULAR_FILE(0b1000),
	SYMBOLIC_LINK(0b1010),
	GITLINK(0b1110);

	private final int mask;

	private TreeEntryModeType(int value) {
		this.mask = value;
	}

	public int shifted() {
		return mask << 12;
	}

	public boolean isPermissionless() {
		return this != REGULAR_FILE;
	}

	public static TreeEntryModeType match(int value) {
		value >>= 12;

		for (final var type : values()) {
			final var mask = type.mask();

			if (value == mask) {
				return type;
			}
		}

		throw new IllegalArgumentException("unknown value: 0b" + Integer.toBinaryString(value));
	}

}