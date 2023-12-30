package git.tree;

import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;

public record TreeEntryMode(
	TreeEntryModeType type,
	int permission
) {

	public TreeEntryMode {
		if (type.isPermissionless() && permission != 0) {
			throw new IllegalArgumentException("%s is permissionless but provided: %o".formatted(type, permission));
		}
	}

	public String format() {
		return Integer.toOctalString(type.shifted() + permission);
	}

	public static TreeEntryMode directory() {
		return new TreeEntryMode(TreeEntryModeType.DIRECTORY, 0);
	}

	public static TreeEntryMode regularFile(int permission) {
		return new TreeEntryMode(TreeEntryModeType.REGULAR_FILE, permission);
	}

	public static TreeEntryMode regularFile(PosixFileAttributes attributes) {
		final var permissions = attributes.permissions();

		if (permissions.contains(PosixFilePermission.OWNER_EXECUTE)) {
			return regularFile(0755);
		}

		return regularFile(0644);
	}

}