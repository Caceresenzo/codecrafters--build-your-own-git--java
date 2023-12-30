package git;

import java.util.List;

import git.tree.TreeEntry;

public record Tree(
	List<TreeEntry> entries
) implements Object {}