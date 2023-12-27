package git;

public record Blob(
	byte[] data
) implements Object {}