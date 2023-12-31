package git.domain;

public record Blob(
	byte[] data
) implements Object {}