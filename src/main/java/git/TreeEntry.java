package git;

import java.io.DataOutputStream;
import java.io.IOException;

public record TreeEntry(
	String mode,
	String name,
	byte[] hash
) {

	public void serialize(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.write(mode.getBytes());
		dataOutputStream.write(' ');
		dataOutputStream.write(name.getBytes());
		dataOutputStream.write('\0');
		dataOutputStream.write(hash);
	}
	
}