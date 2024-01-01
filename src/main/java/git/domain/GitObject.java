package git.domain;

public sealed interface GitObject permits Blob, Commit, Tree {}