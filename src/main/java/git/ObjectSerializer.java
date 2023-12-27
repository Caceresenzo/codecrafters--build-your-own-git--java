package git;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface ObjectSerializer<T extends Object> {

	void serialize(T object, DataOutputStream dataOutputStream) throws IOException;

	T deserialize(DataInputStream dataInputStream) throws IOException;

}