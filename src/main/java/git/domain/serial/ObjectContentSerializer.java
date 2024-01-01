package git.domain.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import git.domain.GitObject;

public interface ObjectContentSerializer<T extends GitObject> {

	void serialize(T object, DataOutputStream dataOutputStream) throws IOException;

	T deserialize(DataInputStream dataInputStream) throws IOException;

}