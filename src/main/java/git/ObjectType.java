package git;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
@Getter
public class ObjectType<T extends Object> {

	public static final ObjectType<Blob> BLOB = new ObjectType<>("blob", Blob.class, new BlobSerializer());
	public static final ObjectType<Tree> TREE = new ObjectType<>("tree", Tree.class, new TreeSerializer());

	public static final Collection<ObjectType> TYPES = List.of(BLOB, TREE);

	private final String name;
	private final Class<?> objectClass;
	private final ObjectSerializer<T> serializer;

	public void serialize(T object, DataOutputStream dataOutputStream) throws IOException {
		serializer.serialize(object, dataOutputStream);
	}

	public T deserialize(DataInputStream dataInputStream) throws IOException {
		return serializer.deserialize(dataInputStream);
	}

	public static ObjectType byName(String name) {
		for (final var type : TYPES) {
			if (type.getName().equalsIgnoreCase(name)) {
				return type;
			}
		}

		throw new IllegalArgumentException("unknown object type: " + name);
	}

	public static ObjectType byClass(Class<? extends Object> clazz) {
		for (final var type : TYPES) {
			if (type.getObjectClass().equals(clazz)) {
				return type;
			}
		}

		throw new IllegalArgumentException("unknown object type: " + clazz);
	}

}