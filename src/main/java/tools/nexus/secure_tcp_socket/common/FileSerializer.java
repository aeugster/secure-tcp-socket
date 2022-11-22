package tools.nexus.secure_tcp_socket.common;

import tools.nexus.secure_tcp_socket.exceptions.SecureSocketTechnicalException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FileSerializer<T> {

    private final Path pathToFile;

    /**
     * Generic class which does the serializing and the casts.
     */
    public FileSerializer(String file) {
        this.pathToFile = Paths.get(file);
    }

    /**
     * Read object from file system
     *
     * @return null if some errors happened
     */
    @SuppressWarnings("unchecked")
    public T getObj() {

        try {
            var bis = new BufferedInputStream(new FileInputStream(pathToFile.toFile()));
            ObjInputStream ois = new ObjInputStream(bis);

            T result = (T) ois.readUnshared();
            ois.close();

            return result;
        } catch (Exception e) {
            // This class provides null on error
            return null;
        }
    }

    public void writeObj(T obj) {
        try {
            if (pathToFile.getParent() != null) {
                Files.createDirectories(pathToFile.getParent());
            }

            FileOutputStream fos = new FileOutputStream(pathToFile.toFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeUnshared(obj);
            oos.close();
        } catch (Exception e) {
            throw new SecureSocketTechnicalException("Error while writeObj: " + pathToFile, e);
        }
    }
}
