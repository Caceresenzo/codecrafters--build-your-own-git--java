package git.pack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import git.Git;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class PackParser {

	public static final int TYPE_MASK = 0b01110000;
	public static final int SIZE_4_MASK = 0b00001111;
	public static final int SIZE_7_MASK = 0b0111_1111;
	public static final int SIZE_CONTINUE_MASK = 0b1000_0000;

	private final ByteBuffer buffer;

	@SneakyThrows
	public List<PackObject> parse() throws IOException, DataFormatException {
		parseSignature();
		parseVersion();

		final var objectCount = buffer.getInt();
		final var objects = new ArrayList<PackObject>(objectCount);
		final var objectByOffset = new HashMap<Integer, Object>(objectCount);

		for (var index = 0; index < objectCount; ++index) {
			final var header = parseObjectHeader();
			final var type = header.type();
			final var bufferPosition = buffer.position();

			final PackObject object = switch (type) {
				case COMMIT:
				case TREE:
				case BLOB: {
					final var content = inflate(header.size());

					yield PackObject.undeltified(type.nativeType(), content);
				}

				case TAG: {
					throw new UnsupportedOperationException();
				}

				case OFS_DELTA: {
					throw new UnsupportedOperationException();
				}

				case REF_DELTA: {
					final var hashBytes = new byte[Git.HASH_BYTES_LENGTH];
					buffer.get(hashBytes);

					final var baseHash = Git.HEX.formatHex(hashBytes);

					final var content = inflate(header.size());
					final var contentBuffer = ByteBuffer.wrap(content);

					@SuppressWarnings("unused")
					final var baseObjectSize = parseVariableLengthIntegerLittleEndian(contentBuffer);
					final var newObjectSize = parseVariableLengthIntegerLittleEndian(contentBuffer);

					final var instructions = parseDeltaInstructions(contentBuffer);

					yield PackObject.deltified(baseHash, newObjectSize, instructions);
				}
			};

			if (object != null) {
				objects.add(object);
				objectByOffset.put(bufferPosition, buffer);
			}
		}

		return objects;
	}

	public void parseSignature() {
		final var bytes = new byte[Integer.BYTES];
		buffer.get(bytes);

		final var signature = new String(bytes);
		if (!"PACK".equals(signature)) {
			throw new IllegalStateException("invalid signature: " + signature);
		}
	}

	public void parseVersion() {
		final var version = buffer.getInt();

		if (version != 2) {
			throw new IllegalStateException("invalid version: " + version);
		}
	}

	public PackObjectHeader parseObjectHeader() {
		var read = Byte.toUnsignedInt(buffer.get());

		final var type = PackObjectType.valueOf((read & TYPE_MASK) >> 4);
		var size = read & SIZE_4_MASK;

		if ((read & SIZE_CONTINUE_MASK) == 0) {
			return new PackObjectHeader(type, size);
		}

		size |= parseVariableLengthIntegerLittleEndian(buffer) << 4;
		return new PackObjectHeader(type, size);
	}

	public byte[] inflate(int size) throws DataFormatException {
		final var inflater = new Inflater();
		inflater.setInput(buffer);

		final var inflated = new byte[size];
		inflater.inflate(inflated);

		return inflated;
	}

	public List<DeltaInstruction> parseDeltaInstructions(ByteBuffer buffer) {
		final var instructions = new ArrayList<DeltaInstruction>();

		while (buffer.hasRemaining()) {
			final var first = Byte.toUnsignedInt(buffer.get());

			if (first == 0) {
				throw new IllegalStateException("unsuppoted 00000000 instruction");
			}

			final var command = first & 0b1000_0000;

			if (command == 0) {
				final var size = first & 0b0111_1111;

				final var bytes = new byte[size];
				buffer.get(bytes);

				instructions.add(DeltaInstruction.insert(bytes));
			} else {
				final var hasOffset1 = (first & 0b0000_0001) != 0;
				final var hasOffset2 = (first & 0b0000_0010) != 0;
				final var hasOffset3 = (first & 0b0000_0100) != 0;
				final var hasOffset4 = (first & 0b0000_1000) != 0;
				final var hasSize1 = (first & 0b0001_0000) != 0;
				final var hasSize2 = (first & 0b0010_0000) != 0;
				final var hasSize3 = (first & 0b0100_0000) != 0;

				final var offset = parseVariableLengthInteger(buffer, hasOffset1, hasOffset2, hasOffset3, hasOffset4);
				var size = parseVariableLengthInteger(buffer, hasSize1, hasSize2, hasSize3);
				if (size == 0) {
					size = 0x10000;
				}

				instructions.add(DeltaInstruction.copy(offset, size));
			}
		}

		return instructions;
	}

	public static int parseVariableLengthInteger(ByteBuffer buffer, boolean... enabledStates) {
		var value = 0;
		var offset = 0;

		for (final var state : enabledStates) {
			if (state) {
				final var read = Byte.toUnsignedInt(buffer.get());
				value += read << offset;
			}

			offset += 8;
		}

		return value;
	}

	public static int parseVariableLengthIntegerLittleEndian(ByteBuffer buffer) {
		var value = 0;
		var shift = 0;

		while (true) {
			final var read = Byte.toUnsignedInt(buffer.get());

			value |= (read & SIZE_7_MASK) << shift;
			if ((read & SIZE_CONTINUE_MASK) == 0) {
				break;
			}

			shift += 7;
		}

		return value;
	}

}