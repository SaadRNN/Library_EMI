package persistance;
import java.sql.Connection;
import persistance.DatabaseConnection;
public class TestDatabase {
    public void main(String [] args){
        try{
            Connection conn = DatabaseConnection.getConnection();
            if(conn!= null){
                System.out.println("Connected to database.");
            }
        }
        catch(Exception e){
            System.out.println("Error connecting to database.");
            e.printStackTrace();
        }
    }
}
