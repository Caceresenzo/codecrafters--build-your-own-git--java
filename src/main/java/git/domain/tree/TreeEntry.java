package git.domain.tree;

public record TreeEntry(
	TreeEntryMode mode,
	String name,
	String hash
) implements Comparable<TreeEntry> {

	@Override
	public int compareTo(TreeEntry other) {
		return name.compareTo(other.name);
	}

}