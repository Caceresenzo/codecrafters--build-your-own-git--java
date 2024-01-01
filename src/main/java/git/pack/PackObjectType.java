package git.pack;

import java.util.HashMap;
import java.util.Map;

import git.domain.ObjectType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
@SuppressWarnings("rawtypes")
public enum PackObjectType {

	COMMIT(1, ObjectType.COMMIT),
	TREE(2, ObjectType.TREE),
	BLOB(3, ObjectType.BLOB),
	TAG(4, null),
	OFS_DELTA(6, null),
	REF_DELTA(7, null);

	private static final Map<Integer, PackObjectType> MAPPING = new HashMap<>();

	static {
		for (final var type : values()) {
			MAPPING.put(type.value(), type);
		}
	}

	private final int value;
	private final ObjectType nativeType;

	public static PackObjectType valueOf(int value) {
		if (value == 4) {
			throw new UnsupportedOperationException("TAG");
		}

		final var type = MAPPING.get(value);
		if (type == null) {
			throw new IllegalArgumentException("invalid value: " + value);
		}

		return type;
	}

}