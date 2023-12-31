package git.domain;

public record Reference(
	String name,
	String hash
) {}