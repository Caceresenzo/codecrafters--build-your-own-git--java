package git.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import git.Object;

public interface ObjectSerializer<T extends Object> {

	void serialize(T object, DataOutputStream dataOutputStream) throws IOException;

	T deserialize(DataInputStream dataInputStream) throws IOException;

}