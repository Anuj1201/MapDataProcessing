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
            File locationFile = getFileFromResource("locations.json");
            File metadataFile = getFileFromResource("metadata.json");

            // Read JSON files into Java objects
            Location[] locations = objectMapper.readValue(locationFile, Location[].class);
            Metadata[] metadata = objectMapper.readValue(metadataFile, Metadata[].class);

            // Create Maps for quick lookups
            Map<String, Location> locationMap = Arrays.stream(locations)
                                                      .collect(Collectors.toMap(Location::getId, loc -> loc));

            Set<String> locationIds = locationMap.keySet();  // IDs from locations.json
            Set<String> metadataIds = Arrays.stream(metadata)
                                            .map(Metadata::getId)
                                            .collect(Collectors.toSet());  // IDs from metadata.json

            // Identify invalid locations (IDs in locations.json but not in metadata.json)
            List<Location> invalidLocations = Arrays.stream(locations)
                                                    .filter(loc -> !metadataIds.contains(loc.getId()))
                                                    .collect(Collectors.toList());

            // Type-wise valid count & rating sum
            Map<String, Integer> typeCount = new HashMap<>();
            Map<String, Double> typeRatingSum = new HashMap<>();

            for (Metadata meta : metadata) {
                if (locationIds.contains(meta.getId())) {
                    typeCount.put(meta.getType(), typeCount.getOrDefault(meta.getType(), 0) + 1);
                    typeRatingSum.put(meta.getType(), typeRatingSum.getOrDefault(meta.getType(), 0.0) + meta.getRating());
                }
            }

            // Print valid points per type
            System.out.println("Valid Points per Type:");
            typeCount.forEach((type, count) -> System.out.println(type + ": " + count));

            // Print average rating per type
            System.out.println("\nAverage Rating per Type:");
            typeRatingSum.forEach((type, sum) -> {
                double avg = sum / typeCount.get(type);
                System.out.println(type + ": " + avg);
            });

            // Find the most reviewed location
            Metadata mostReviewed = Arrays.stream(metadata)
                                          .filter(meta -> locationIds.contains(meta.getId()))
                                          .max(Comparator.comparingInt(Metadata::getReviews))
                                          .orElse(null);

            if (mostReviewed != null) {
                Location mostReviewedLocation = locationMap.get(mostReviewed.getId());
                System.out.println("\nLocation with Highest Reviews: " + mostReviewed.getId() +
                                   " (" + mostReviewed.getReviews() + " reviews)" +
                                   " | Latitude: " + mostReviewedLocation.getLatitude() +
                                   " | Longitude: " + mostReviewedLocation.getLongitude());
            }

            // Print invalid locations with latitude & longitude
            System.out.println("\nInvalid Locations (Present only in locations.json, missing in metadata.json):");
            if (invalidLocations.isEmpty()) {
                System.out.println("No invalid locations found.");
            } else {
                for (Location loc : invalidLocations) {
                    System.out.println("Invalid Location ID: " + loc.getId() +
                                       " | Latitude: " + loc.getLatitude() +
                                       " | Longitude: " + loc.getLongitude());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
