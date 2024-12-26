package org.example;

import java.io.*;
import java.util.List;
import java.util.Vector;

public interface DB {
    List<User> USERS = new Vector<>();

    static void inport() throws FileNotFoundException {
        try(
                ObjectInputStream os = new ObjectInputStream(new FileInputStream("files"));
                ) {
            List<User> users =(List<User>) os.readObject();
            USERS.addAll(users);
        } catch (IOException | ClassNotFoundException e) {

        }
    }

    static void export() {
        try(
                ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("files"))
                ) {
            os.writeObject(USERS);
        } catch (IOException e) {

        }
    }
}
