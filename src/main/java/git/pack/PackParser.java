package git.pack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.InflaterInputStream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PackParser {

	public static final int TYPE_MASK = 0b01110000;
	public static final int SIZE_4_MASK = 0b00001111;
	public static final int SIZE_7_MASK = 0b0111_1111;
	public static final int SIZE_CONTINUE_MASK = 0b1000_0000;

	private final ByteBuffer buffer;

	public void parse() throws IOException {
		parseSignature();
		parseVersion();

		System.out.println(buffer.order());
		final var objectCount = buffer.getInt();
		System.out.println(buffer.position());

		for (var index = 0; index < objectCount; ++index) {
			final var objectHeader = parseObjectHeader();
			System.out.println(objectHeader);

			// Read compressed data
			byte[] compressedData = new byte[objectHeader.size()];
			buffer.get(compressedData);

			final var stream = new InflaterInputStream(new ByteArrayInputStream(compressedData));
			System.out.println(new String(stream.readAllBytes()));
		}

		System.out.println(objectCount);
	}

	public void parseSignature() {
		final var bytes = new byte[4];
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
		System.out.println("xread %d   %8s".formatted(read, Integer.toBinaryString(read)));
		final var type = PackObjectType.valueOf((read & TYPE_MASK) >> 4);
		var size = read & SIZE_4_MASK;
		System.out.println(Integer.toBinaryString(size));

		if ((read & SIZE_CONTINUE_MASK) == 0) {
			return new PackObjectHeader(type, size);
		}

		var offset = 4;
		do {
			read = Byte.toUnsignedInt(buffer.get());
			System.out.println(" read %d   %8s".formatted(read, Integer.toBinaryString(read)));
			size |= (read & SIZE_7_MASK) << offset;
			System.out.println(Integer.toBinaryString(size));
			offset += 7;
		} while ((read & SIZE_CONTINUE_MASK) != 0);

		return new PackObjectHeader(type, size);
	}

	public static void main(String[] args) throws IOException {
		final var bytes = Files.readAllBytes(Paths.get("test.pack"));
		final var buffer = ByteBuffer.wrap(bytes);

		final var parser = new PackParser(buffer);
		parser.parse();
	}

}