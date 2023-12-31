package git.pack;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum PackObjectType {

	COMMIT(1),
	TREE(2),
	BLOB(3),
	TAG(4),
	OFS_DELTA(6),
	REF_DELTA(7);

	private static final Map<Integer, PackObjectType> MAPPING = new HashMap<>();

	static {
		for (final var type : values()) {
			MAPPING.put(type.value(), type);
		}
	}

	private final int value;

	public static PackObjectType valueOf(int value) {
		final var type = MAPPING.get(value);

		if (type == null) {
			throw new IllegalArgumentException("invalid value: " + value);
		}

		return type;
	}

}