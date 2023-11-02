


//TESTING VIA PRINTING EACH ROW
   // //printing out the rows of the table (limit 25)
    // private static void printTableData(Connection connection, String tableName) {
    //     try (Statement stmt = connection.createStatement();
    //          ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " LIMIT 25")) {

    //         // Get the column names
    //         int columnCount = rs.getMetaData().getColumnCount();
    //         for (int i = 1; i <= columnCount; i++) {
    //             System.out.print(rs.getMetaData().getColumnName(i) + " | ");
    //         }
    //         System.out.println();

    //         // Print the values for each row
    //         while (rs.next()) {
    //             for (int i = 1; i <= columnCount; i++) {
    //                 Object value = rs.getObject(i);
    //                 System.out.print(value + " | ");
    //             }
    //             System.out.println();
    //         }
    //     } catch (Exception e) {
    //         System.out.println("Failed to print table data: " + e.getMessage());
    //     }
    // }

// public class ProcessStopsExample {

//     // Database connection parameters
//     private static final String DB_URL = "jdbc:mysql://localhost:3306/your_database";
//     private static final String DB_USER = "your_username";
//     private static final String DB_PASSWORD = "your_password";

//     public static void main(String[] args) {
//         try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
//             Map<Integer, List<Integer>> stopToTripIdsMap = getStopToTripIdsMap(connection);
//             processStopTimes(connection, stopToTripIdsMap);
//         } catch (SQLException e) {
//             e.printStackTrace();
//         }
//     }

//     private static Map<Integer, List<Integer>> getStopToTripIdsMap(Connection connection) throws SQLException {
//         Map<Integer, List<Integer>> stopToTripIdsMap = new HashMap<>();
//         String query = "SELECT stop_id, trip_id FROM trips WHERE service_id = 6 AND route_id = 1 AND direction_id = 0";
//         try (Statement stmt = connection.createStatement();
//              ResultSet rs = stmt.executeQuery(query)) {

//             while (rs.next()) {
//                 int stopId = rs.getInt("stop_id");
//                 int tripId = rs.getInt("trip_id");
//                 stopToTripIdsMap.computeIfAbsent(stopId, k -> new ArrayList<>()).add(tripId);
//             }
//         }
//         return stopToTripIdsMap;
//     }

//     private static void processStopTimes(Connection connection, Map<Integer, List<Integer>> stopToTripIdsMap) throws SQLException {
//         for (int stopId : stopToTripIdsMap.keySet()) {
//             List<Integer> tripIds = stopToTripIdsMap.get(stopId);
//             List<String> timeDifferences = new ArrayList<>();

//             String tripIdsStr = String.join(",", tripIds.stream().map(Object::toString).toArray(String[]::new));
//             String query = "SELECT *, " +
//                            "TIMEDIFF(LEAD(arrival_time) OVER (ORDER BY arrival_time), arrival_time) AS time_difference, " +
//                            "LEAD(arrival_time) OVER (ORDER BY arrival_time) AS next_arrival_time " +
//                            "FROM stop_times " +
//                            "WHERE stop_id = " + stopId + " AND trip_id IN (" + tripIdsStr + ") " +
//                            "ORDER BY arrival_time ASC";

//             try (Statement stmt = connection.createStatement();
//                  ResultSet rs = stmt.executeQuery(query)) {

//                 System.out.println("Stop ID: " + stopId);
//                 System.out.print("trip_id    |    arrival_time    |    departure_time    |    stop_id    |    time_difference");
//                 System.out.println();

//                 while (rs.next()) {
//                     // Process the stop_times subtable here
//                     int tripId = rs.getInt("trip_id");
//                     String arrivalTime = rs.getString("arrival_time");
//                     String departureTime = rs.getString("departure_time");
//                     String timeDifference = rs.getString("time_difference");

//                     // Add the time difference to the list
//                     timeDifferences.add(timeDifference);

//                     System.out.println(tripId + " | " + arrivalTime + " | " + departureTime + " | " + stopId + " | " + timeDifference);
//                 }
//             }

//             // Find the maximum time difference for this stop
//             String maxTimeDifference = findMaxTimeDifference(timeDifferences);
//             System.out.println("Max Time Difference for Stop ID " + stopId + ": " + maxTimeDifference);
//             System.out.println();
//         }
//     }

//     private static String findMaxTimeDifference(List<String> timeDifferences) {
//         // Assuming that the time differences are in the format 'HH:mm:ss'
//         String maxTimeDifference = "00:00:00";
//         for (String timeDiff : timeDifferences) {
//             if (timeDiff != null && timeDiff.compareTo(maxTimeDifference) > 0) {
//                 maxTimeDifference = timeDiff;
//             }
//         }
//         return maxTimeDifference;
//     }
// }

// private static List<Integer> getMatchingStopIds(Connection connection) throws SQLException {
//    List<Integer> stopIds = new ArrayList<>();
//    String query = "SELECT DISTINCT stop_id FROM trips WHERE service_id = 6 AND route_id = 1 AND direction_id = 0";
//    try (Statement stmt = connection.createStatement();
//         ResultSet rs = stmt.executeQuery(query)) {

//        while (rs.next()) {
//            int stopId = rs.getInt("stop_id");
//            stopIds.add(stopId);
//        }
//    }
//    return stopIds;
// }

// private static void processStops(Connection connection, List<Integer> stopIds) throws SQLException {
//    for (int stopId : stopIds) {
//        List<String> timeDifferences = new ArrayList<>();

//        String query = "SELECT *, " +
//                       "TIMEDIFF(LEAD(arrival_time) OVER (ORDER BY arrival_time), arrival_time) AS time_difference, " +
//                       "LEAD(arrival_time) OVER (ORDER BY arrival_time) AS next_arrival_time " +
//                       "FROM stop_times " +
//                       "WHERE stop_id = " + stopId + " AND trip_id IN (SELECT trip_id FROM trips WHERE service_id = 6 AND route_id = 1 AND direction_id = 0) " +
//                       "ORDER BY arrival_time ASC";

//        try (Statement stmt = connection.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {

//            System.out.println("Stop ID: " + stopId);
//            System.out.print("trip_id    |    arrival_time    |    departure_time    |    stop_id    |    time_difference");
//            System.out.println();

//            while (rs.next()) {
//                // Process the stop_times subtable here
//                int tripId = rs.getInt("trip_id");
//                String arrivalTime = rs.getString("arrival_time");
//                String departureTime = rs.getString("departure_time");
//                String timeDifference = rs.getString("time_difference");

//                // Add the time difference to the list
//                timeDifferences.add(timeDifference);

//                System.out.println(tripId + " | " + arrivalTime + " | " + departureTime + " | " + stopId + " | " + timeDifference);
//            }
//        }

//        // Find the maximum time difference for this stop
//        String maxTimeDifference = findMaxTimeDifference(timeDifferences);
//        System.out.println("Max Time Difference for Stop ID " + stopId + ": " + maxTimeDifference);
//        System.out.println();
//    }
// }

// private static String findMaxTimeDifference(List<String> timeDifferences) {
//    // Assuming that the time differences are in the format 'HH:mm:ss'
//    String maxTimeDifference = "00:00:00";
//    for (String timeDiff : timeDifferences) {
//        if (timeDiff != null && timeDiff.compareTo(maxTimeDifference) > 0) {
//            maxTimeDifference = timeDiff;
//        }
//    }
//    return maxTimeDifference;
// }
// }

//Cull data for 9-5 
//Output routes via trip id
//Between 2 dates - compare the stop_id and direction_id

/* To make faster: threading, change from array to hashmap, include time line, create a subtable, jupyter notebook java, sort the stop_ids then search for duplicates, (later) convert to c, 
 */
/* hashmap does not allow duplicate keys - values are overriden (stops can be the keys, values can be the trips?) */

    // private static List<Integer> getStopIds(Connection connection, List<Integer> tripIds) throws SQLException {
    //     long start = System.currentTimeMillis();
    //     List<Integer> stopIds = new ArrayList<>();
    //     System.out.println("Stop Ids: ");
    //     for(int tripId : tripIds){

    //         String query = "SELECT DISTINCT stop_id from stop_times WHERE trip_id = " + tripId;
    //         try(Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

    //             while (rs.next()) {
    //             int stopId = rs.getInt("stop_id");
    //             if(!stopIds.contains(stopId)){
    //                 stopIds.add(stopId);
    //                 System.out.print(stopId + " , ");
    //                 }
    //             }
    //         }
    //     } 

    //     long end = System.currentTimeMillis();
    //     long elapsed = end - start;
    //     System.out.println("Execution time: " + elapsed +  " milliseconds.");
    //     return stopIds;
    // }




