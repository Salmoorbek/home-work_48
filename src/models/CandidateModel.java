package models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CandidateModel {
    private ArrayList<Candidate> candidates;

    public CandidateModel() {
        candidates = new ArrayList<>(List.of(readFile()));
    }

    public ArrayList<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(ArrayList<Candidate> candidates) {
        this.candidates = candidates;
    }

    public static Candidate[] readFile() {
        Path path = Paths.get("./data/json/candidates.json");
        String json = "";
        try {
            json = Files.readString(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(json, Candidate[].class);
    }
    public static void writeFile(ArrayList<Candidate> employee){
        Path path = Paths.get("./data/json/candidates.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Candidate[] result = employee.toArray(Candidate[]::new);
        String json = gson.toJson(result);
        try{
            byte[] jsonBytes = json.getBytes();
            Files.write(path, jsonBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
