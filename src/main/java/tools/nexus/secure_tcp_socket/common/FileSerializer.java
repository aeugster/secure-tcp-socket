package tools.nexus.secure_tcp_socket.common;

import tools.nexus.secure_tcp_socket.exceptions.SecureSocketTechnicalException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSerializer<T> {

    private final Path pathToFile;

    /**
     * Generic class which does the serializing and the casting
     */
    public FileSerializer(String file) {
        this.pathToFile = Paths.get(file);
    }

    /**
     * Read object from file system
     *
     * @return the object or null
     */
    @SuppressWarnings("unchecked")
    public T getObj() {

        try (var bis = new BufferedInputStream(new FileInputStream(pathToFile.toFile()));
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            return (T) ois.readUnshared();

        } catch (FileNotFoundException e) {
            // ok
            return null;

        } catch (IOException | ClassNotFoundException e) {
            throw new SecureSocketTechnicalException("Reading of file failed: " + pathToFile, e);
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

        } catch (IOException e) {
            // includes FileNotFound
            throw new SecureSocketTechnicalException("Error while writeObj: " + pathToFile, e);
        }
    }
}
