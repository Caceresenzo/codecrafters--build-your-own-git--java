package git.domain;

import java.util.List;

import git.domain.tree.TreeEntry;

public record Tree(
	List<TreeEntry> entries
) implements Object {}