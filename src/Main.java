import models.LabRab;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            new LabRab("localhost", 7369).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}