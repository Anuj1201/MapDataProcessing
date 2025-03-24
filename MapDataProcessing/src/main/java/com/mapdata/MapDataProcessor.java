package main.java.com.mapdata;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class MapDataProcessor {
    public static void main(String[] args) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Read JSON files from resources folder
            File locationFile = getFileFromResource("locations.json");
            File metadataFile = getFileFromResource("metadata.json");

            // Parse JSON into Java objects
            Location[] locations = objectMapper.readValue(locationFile, Location[].class);
            Metadata[] metadata = objectMapper.readValue(metadataFile, Metadata[].class);

            // Convert to Maps for easy lookup
            Map<String, Location> locationMap = Arrays.stream(locations)
                                                      .collect(Collectors.toMap(Location::getId, loc -> loc));

            Map<String, Metadata> metadataMap = Arrays.stream(metadata)
                                                      .collect(Collectors.toMap(Metadata::getId, meta -> meta));

            // Merge Data & Process
            Map<String, Integer> typeCount = new HashMap<>();
            Map<String, Double> typeRatingSum = new HashMap<>();

            for (Metadata meta : metadata) {
                typeCount.put(meta.getType(), typeCount.getOrDefault(meta.getType(), 0) + 1);
                typeRatingSum.put(meta.getType(), typeRatingSum.getOrDefault(meta.getType(), 0.0) + meta.getRating());
            }

            // Print the number of valid points per type
            System.out.println("Valid Points per Type:");
            typeCount.forEach((type, count) -> System.out.println(type + ": " + count));

            // Calculate and print average rating per type
            System.out.println("\nAverage Rating per Type:");
            typeRatingSum.forEach((type, sum) -> {
                double avg = sum / typeCount.get(type);
                System.out.println(type + ": " + avg);
            });

            // Find the location with the highest number of reviews
            Metadata mostReviewed = Arrays.stream(metadata)
                                          .max(Comparator.comparingInt(Metadata::getReviews))
                                          .orElse(null);

            if (mostReviewed != null) {
                System.out.println("\nLocation with Highest Reviews: " + mostReviewed.getId() + " (" + mostReviewed.getReviews() + " reviews)");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to read files from src/main/resources
    private static File getFileFromResource(String fileName) throws URISyntaxException {
        ClassLoader classLoader = MapDataProcessor.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        } else {
            return Paths.get(resource.toURI()).toFile();
        }
    }
}
