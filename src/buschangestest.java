import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class buschangestest
{
    public static void main(String args []){
        try
        {
            //connect and put in user/pass
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/busChangesData","root","DelL!234");
            
            //perform these functions
            List<Integer> tripIds = getMatchingTripIds(con);
            getStopTimesSubtable(con, tripIds);


            //close the connection
            con.close();

        //print out any warning signs
        } catch (Exception e){
            System.out.println(e);
        }
    }


    private static List<Integer> getMatchingTripIds(Connection connection) throws SQLException {
        List<Integer> tripIds = new ArrayList<>();
        String query = "SELECT trip_id FROM trips WHERE service_id = 6 AND route_id = 1 AND direction_id = 0";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int tripId = rs.getInt("trip_id");
                tripIds.add(tripId);
            }
        }
        return tripIds;
    }

    private static void getStopTimesSubtable(Connection connection, List<Integer> tripIds) throws SQLException {
        String tripIdsStr = String.join(",", tripIds.stream().map(Object::toString).toArray(String[]::new));

        String query = "WITH TimeDifferences AS ("+
            "SELECT *," +
            "TIMEDIFF(LEAD(arrival_time) OVER (ORDER BY arrival_time), arrival_time) AS time_difference "+
            "FROM stop_times "+
            "WHERE stop_id = 2204 AND trip_id IN (" + tripIdsStr + ")" +
            ") " +
        "SELECT *, MAX(time_difference) OVER (PARTITION BY stop_id) AS max_time_difference "+
        "FROM TimeDifferences "+
        "ORDER BY arrival_time ASC";

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)){
            System.out.print("trip_id    |    arrival_time    |    departure_time    |    stop_id    |    time_difference | max_time_difference");
            System.out.println();
            while (rs.next()) {
                int tripId = rs.getInt("trip_id");
                String arrivaltime = rs.getString("arrival_time");
                String departuretime = rs.getString("departure_time");
                int stopId = rs.getInt("stop_id");
                String timeDifference = rs.getString("time_difference");
                String maxTimeDifference = rs.getString("max_time_difference");
                System.out.println(tripId + " | " + arrivaltime + " | " + departuretime + " | " +  stopId + " | " + timeDifference + " | " + maxTimeDifference);
            } 
        }
    }
}
