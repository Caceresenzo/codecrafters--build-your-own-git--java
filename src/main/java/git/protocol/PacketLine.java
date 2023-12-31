package git.protocol;

import java.io.IOException;
import java.io.OutputStream;

public sealed interface PacketLine permits PacketLine.Data, PacketLine.Flush {

	void serialize(OutputStream outputStream) throws IOException;

	public static record Data(byte[] content) implements PacketLine {

		@Override
		public void serialize(OutputStream outputStream) throws IOException {
			final var size = "%04x".formatted(content.length + 4).getBytes();
			outputStream.write(size);
			outputStream.write(content);
		}

		public boolean isComment() {
			return content.length != 0 && content[0] == '#';
		}

	}

	public static enum Flush implements PacketLine {

		INSTANCE;

		private static final byte[] ZERO_BYTES = "0000".getBytes();

		@Override
		public void serialize(OutputStream outputStream) throws IOException {
			outputStream.write(ZERO_BYTES);
		}

	}

	public static Data data(byte[] content) {
		return new Data(content);
	}

	public static Data data(String content) {
		return new Data(content.getBytes());
	}

	public static Flush flush() {
		return Flush.INSTANCE;
	}

}