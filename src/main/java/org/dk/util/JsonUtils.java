package org.dk.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.dk.DivCardService;
import org.dk.model.CardValueOverride;
import org.dk.model.DivinationCard;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class JsonUtils {

    public static void writeListToJsonFile(List<Object> list, String fileName) {
        // Create Gson object
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter("src/main/resources/" + fileName)) {
            // Convert list to JSON string
            String json = gson.toJson(list);

            // Write JSON string to file
            writer.write(json);
            System.out.println("Successfully wrote JSON to file: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeListOfStringToJson(List<String> dataList, String fileName) {
        // Create Gson instance with pretty printing
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

        // Convert list to JSON string
        String jsonString = gson.toJson(dataList);

        // Write JSON string to file
        try (FileWriter writer = new FileWriter("src/main/resources/" + fileName)) {
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> readListOfStringJson(String fileName) {

        // Get the input stream for the file
        InputStream inputStream = DivCardService.class.getResourceAsStream("/" + fileName);

        // Check if the file exists
        if (inputStream == null) {
            System.err.println("File not found: " + fileName);
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            Gson gson = new Gson();
            String[] jsonArray = gson.fromJson(reader, String[].class);
            System.out.println("# maps read: " + jsonArray.length);
            return Arrays.asList(jsonArray);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<CardValueOverride> readAllDivinationCardOverridesJson(String fileName) {
        // Get the input stream for the file
        InputStream inputStream = DivCardService.class.getResourceAsStream("/" + fileName);

        // Check if the file exists
        if (inputStream == null) {
            System.err.println("File not found: " + fileName);
            return null;
        }

        List<CardValueOverride> valueOverrides = null;
        try {
            // Read the file
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Convert JSON to List of DivinationCard using Gson
            Gson gson = new Gson();
            Type divinationCardListType = new TypeToken<List<CardValueOverride>>() {}.getType();
            valueOverrides = gson.fromJson(reader, divinationCardListType);

            // Close the reader
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return valueOverrides;
    }

    public static void readAllDivinationCardsJson(String fileName) {
        // Get the input stream for the file
        InputStream inputStream = DivCardService.class.getResourceAsStream("/" + fileName);

        // Check if the file exists
        if (inputStream == null) {
            System.err.println("File not found: " + fileName);
            return;
        }

        List<DivinationCard> divinationCards = null;
        try {
            // Read the file
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Convert JSON to List of DivinationCard using Gson
            Gson gson = new Gson();
            Type divinationCardListType = new TypeToken<List<DivinationCard>>() {}.getType();
            divinationCards = gson.fromJson(reader, divinationCardListType);

            // Close the reader
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        divinationCards.forEach(card -> {
            DivCardService.divinationCards.put(card.getName(), card);
        });
    }
}
