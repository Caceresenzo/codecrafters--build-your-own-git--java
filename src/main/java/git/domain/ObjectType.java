package git.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import git.domain.serial.BlobSerializer;
import git.domain.serial.CommitSerializer;
import git.domain.serial.ObjectContentSerializer;
import git.domain.serial.TreeSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
@Getter
public class ObjectType<T extends GitObject> {

	private static final byte[] SPACE_BYTES = { ' ' };
	private static final byte[] NULL_BYTES = { 0 };

	public static final ObjectType<Blob> BLOB = new ObjectType<>("blob", Blob.class, new BlobSerializer());
	public static final ObjectType<Tree> TREE = new ObjectType<>("tree", Tree.class, new TreeSerializer());
	public static final ObjectType<Commit> COMMIT = new ObjectType<>("commit", Commit.class, new CommitSerializer());

	public static final Collection<ObjectType> TYPES = List.of(BLOB, TREE, COMMIT);

	private final String name;
	private final Class<?> objectClass;
	private final ObjectContentSerializer<T> serializer;

	public byte[] serialize(T object) throws IOException {
		final var content = serializeContent(object);
		final var lengthBytes = String.valueOf(content.length).getBytes();

		try (
			final var outputStream = new ByteArrayOutputStream();
			final var dataOutputStream = new DataOutputStream(outputStream)
		) {
			outputStream.write(name.getBytes());
			outputStream.write(SPACE_BYTES);
			outputStream.write(lengthBytes);
			outputStream.write(NULL_BYTES);
			outputStream.write(content);

			return outputStream.toByteArray();
		}
	}

	public byte[] serializeContent(T object) throws IOException {
		try (
			final var outputStream = new ByteArrayOutputStream();
			final var dataOutputStream = new DataOutputStream(outputStream)
		) {
			serializer.serialize(object, dataOutputStream);
			return outputStream.toByteArray();
		}
	}

	public T deserialize(DataInputStream dataInputStream) throws IOException {
		return serializer.deserialize(dataInputStream);
	}

	public T deserialize(byte[] bytes) throws IOException {
		try (
			final var byteInputStream = new ByteArrayInputStream(bytes);
			final var dataInputStream = new DataInputStream(byteInputStream);
		) {
			final var object = deserialize(dataInputStream);

			if (byteInputStream.read() != -1) {
				throw new IllegalStateException("buffer not fully read");
			}

			return object;
		}
	}

	public static ObjectType byName(String name) {
		for (final var type : TYPES) {
			if (type.getName().equalsIgnoreCase(name)) {
				return type;
			}
		}

		throw new IllegalArgumentException("unknown object type: " + name);
	}

	public static ObjectType byClass(Class<? extends GitObject> clazz) {
		for (final var type : TYPES) {
			if (type.getObjectClass().equals(clazz)) {
				return type;
			}
		}

		throw new IllegalArgumentException("unknown object type: " + clazz);
	}

}