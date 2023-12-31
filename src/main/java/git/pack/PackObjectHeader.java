package git.pack;

public record PackObjectHeader(
	PackObjectType type,
	int size
) {}