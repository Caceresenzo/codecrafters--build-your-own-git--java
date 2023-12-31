package git.domain;

public sealed interface Object permits Blob, Commit, Tree {}