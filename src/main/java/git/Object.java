package git;

public sealed interface Object permits Blob, Commit, Tree {}