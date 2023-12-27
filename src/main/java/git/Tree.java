package git;

import java.util.List;

public record Tree(
	List<TreeEntry> entries
) implements Object {}