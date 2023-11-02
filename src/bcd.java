import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.sql.Time;

/**
 * This is a program that outputs routes that might need to be checked for map editing based on frequency analysis of GTFS data.
 * Methods include: main (takes in GTFA data), getServiceIds, getRouteIds, getTripIds, getStopIds, and processStops 
 * @author Araika Khokhar, Alexander Davis
 */

public class bcd
{
    public static void main(String[] args){
        try
        {
            //connect and put in user/pass
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/busChangesData","root","DelL!234");
            
            //call relevant methods
            String date = "20230630"; // Change this so it can take in 2 dates as input front end 
            List<Integer> serviceIds = getServiceIds(con, date);
            List<Integer> routeIds = getRouteIds(con, serviceIds);
            Map<Integer, List<Integer>> tripsByRouteMap = new HashMap<> ();
            StringBuilder tripIdsString = getTripIds(con, serviceIds, routeIds, tripsByRouteMap);
            Map<Integer, Map<Integer, List<Integer>>> stopIdsMap = getStopIds(con, tripIdsString, tripsByRouteMap);
            processStops(con, serviceIds, routeIds, stopIdsString, tripIdsString);
           
           // close the connection
            con.close();

            //print out any warning signs
        }   catch (Exception e){
            System.out.println(e);
            }
    }


    /**
    * getServiceIds is a method that outputs distinct service_ids into a list from calendar_dates of GTFS folder where dates = input
    *
    * @param Connection to SQL server, date input 
    * @return Integer List of relevant Service Ids by date 
    */

    private static List<Integer> getServiceIds(Connection connection, String date) throws SQLException {
       // long start = System.currentTimeMillis();
       // System.out.println("Service Ids: ");
        List<Integer> serviceIds = new ArrayList<>();
        String query = "SELECT DISTINCT service_id from calendar_dates where dates =" + date; 
        try(Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query)){

            while (rs.next()){
                int serviceId = rs.getInt("service_id");
                serviceIds.add(serviceId);
                //System.out.print(serviceId + " , ");
            }
        }
        //long end = System.currentTimeMillis();
        //long elapsed = end - start;
       
        //System.out.println("Execution time: " + elapsed +  " milliseconds.");

        return serviceIds;

    }

    /**
    * getRouteIds is a method that outputs distinct route_ids into a list from trips of GTFS folder where service_ids 
    are the ones from the prev method
    *
    * @param Connection to SQL server, List of service_ids 
    * @return Integer List of relevant route Ids to check 
    */

    //gets the list of route ids based on service ids in trips (route ids that occur of those service ids we just got)
    private static List<Integer> getRouteIds(Connection connection, List<Integer> serviceIds) throws SQLException{
        //System.out.println("Route Ids: ");
        //long start = System.currentTimeMillis();
        List <Integer> routeIds = new ArrayList<>();
        for(int serviceId : serviceIds){
            String query = "SELECT DISTINCT route_id from trips where service_id = " + serviceId; 

            try(Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query)){
                while(rs.next()){
                    int routeId = rs.getInt("route_id");
                    routeIds.add(routeId);
                    System.out.print(routeId + " , ");
                }
            } 
        }

       // long end = System.currentTimeMillis();
        // long elapsed = end - start;
        //System.out.println("Execution time: " + elapsed +  " milliseconds.");

        return routeIds;
    }


    //NEEDS TO DO THIS FOR 2 DIFFERENT DIRECTIONS

    /**
    * getTripIds is a method that creates a HashMap of key (route_id), value (list of relevant trip_ids) from trips
    *
    * @param Connection to SQL server, List of service_ids, List of route_ids, Map to fill in
    * @return Integer List of relevant trip Ids for stopid map
    */

    private static StringBuilder getTripIds(Connection connection, List<Integer> serviceIds, List<Integer> routeIds, Map<Integer, List<Integer>> tripsByRouteMap) throws SQLException {
       // System.out.println("Trip Ids: ");
       // long start = System.currentTimeMillis();
        List<Integer> tripIds = new ArrayList<>();
        StringBuilder tripIdsString = new StringBuilder();

        //for each serviceId and for each route in each service id
         //for(int serviceId : serviceIds){
            for (int routeId : routeIds){

                String query = "SELECT trip_id FROM trips WHERE (service_id = "+ serviceId + ")" + "AND (" + routeId + ")" + " AND direction_id = 0";
                
                try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
                    while (rs.next()) {
                    int tripId = rs.getInt("trip_id");
                    tripIds.add(tripId);
                    
                    //Add trips to hashmap divided by route
                    if(!tripsByRouteMap.containsKey(routeId)){
                        tripsByRouteMap.put(routeId, new ArrayList<>());
                    }
                    tripsByRouteMap.get(routeId).add(tripId);
                    }

                    for (int i = 0; i < tripIds.size(); i++) {
                        if (i > 0) {
                            tripIdsString.append(", ");
                        }
                        tripIdsString.append(tripIds.get(i));
                    }

                }
            //}
        }

        for(int routeId : tripsByRouteMap.keySet()){
            List<Integer> tripIdsList = tripsByRouteMap.get(routeId);
            System.out.println("RouteId: " + routeId + "TripIds: ");
            
                for (int tripId : tripIdsList) {
                    System.out.print(tripId + " ");
                }
            System.out.println();
        }
        
        return tripIdsString;
        // //long end = System.currentTimeMillis();
        // //long elapsed = end - start;
        // //System.out.println("Execution time: " + elapsed +  " milliseconds.");
     }
    


    /**
    * getStopIds is a method that creates a HashMap of key (stop_id), value (map with key: route_id, value: trip_id) from trips
    *
    * @param Connection to SQL server, Map of tripIdsByRoute
    * @return Map of key: integer, value: routeId, trip
    */

    private static Map<Integer, Map.Entry<Integer, List<Integer>>> getStopIds(Connection connection, StringBuilder tripIdsString, Map<Integer, List<Integer>> tripIdsByRouteMap) throws SQLException {
   // long start = System.currentTimeMillis();

   //Create a map with key - stopId and value - another map with key route_id, value trip_id
    Map<Integer, Map.Entry<Integer, List<Integer>>> stopIdsMap = new HashMap<>();

    //StringBuilder stopIdsString = new StringBuilder();
    boolean first = true;
    // System.out.println("Stop Ids: ");

    String query = "SELECT DISTINCT stop_id, trip_id from stop_times WHERE trip_id IN (" + tripIdsString + ")";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)){
            while (rs.next()) {
                int stopId = rs.getInt("stop_id");
                int tripId = rs.getInt("trip_id");
                
                //if stopIdsMap does not contain that stopId, add it to the map. Then check tripId, if it's in tripsByRoute value set, put that specific key/value pair into the HashMap
                if(!stopIdsMap.containsKey(stopId) && tripIdsByRouteMap.containsValue(tripId)){
                    stopIdsMap.put(stopId, tripIdsByRouteMap.) );
                    
                }
                
                }
            }
        
     
     }
            

       // System.out.print(stopIdsString);
       // long end = System.currentTimeMillis();
      //  long elapsed = end - start;
      //  System.out.println("Execution time: " + elapsed +  " milliseconds.");
        return stopIdsString;
    }


    /**
    * processStops is the method that gets the maximum headway list for each route, for each stop, divided by stop and routeids
    *
    * @param Connection to SQL server, Map of tripIds divided by routeIds, string of stopids to check
    * @return Map of each stop, route max headway 
    */
    private static void processStops(Connection connection, List<Integer> serviceIds, List<Integer> routeIds, StringBuilder stopIdsString, StringBuilder tripIdsString) throws SQLException {
            // Map<Integer, Map<Integer, Time>> max = new HashMap<>();

            System.out.println("Max Times List");
            System.out.println("Formatted: stop | trip | maxTimeDiff");

      
            String query = "WITH TimeDifferences AS ("+
            "SELECT *," +
            "TIMEDIFF(LEAD(arrival_time) OVER (ORDER BY arrival_time), arrival_time) AS time_difference "+
            "FROM stop_times "+
            "WHERE stop_id = 2204 AND trip_id IN (" + tripIdsString + ")" +
            ") " +
            "SELECT *, MAX(time_difference) OVER (PARTITION BY stop_id) AS max_time_difference "+
            "FROM TimeDifferences "+
            "ORDER BY arrival_time ASC";

            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)){
                System.out.println("trip_id | stop_id | max_time_difference");
                System.out.println();
                while (rs.next()) {
                    int tripIdResult = rs.getInt("trip_id");
                    int stopIdResult = rs.getInt("stop_id");
                    String maxTimeDifference = rs.getString("max_time_difference");
                    System.out.println(tripIdResult + "    |    " + stopIdResult + "    |    " + maxTimeDifference);
                } 
            }
        }


    }


// Once we get this max headway list
//Figure out how to do this for the second date
// Compare those two max headway lists where stop is the same via parameters
// Output the stops and trips where there is a flagged changed
// Convert those trips back to routes
//Do the same for the opposite direction
// Convert those trips back to routes
//Output final routes list
//Cull the times outside of 6 am - 9 pm, M-F


/*   
        for(int stopId : stopIdsMap.keySet()){
            if(!first){
                stopIdsString.append(", ");
            } else {
                first = false;
            }
        stopIdsString.append(stopId);
 */


        