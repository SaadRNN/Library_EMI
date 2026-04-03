package persistance;
import domainModel.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class LibraryRepository {

    //-----1.Gestion des Users (LOGIN)---
    public User findUser(String username, String password) throws SQLException, ClassNotFoundException {
        String sql = "select * from users where username = ? and password = ? ";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                }
            }
        }
        return null; // Retroune un null si les identifiants sont faux

    }

    //----- 2.GESTION DES LIVRES (AFFICHAGE)
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "Select * from books";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getBoolean("available")
                ));

            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return books;
    }

    //-------3 . ACTION ADMIN (AJOUTER)---
    public void addBook(String title, String author) throws SQLException, ClassNotFoundException {
        String sql = "insert into books (title,author) values (?,?,TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            psmt.setString(1, title);
            psmt.setString(2, author);
            psmt.executeUpdate();
        }
    }

    //-----------4 . GESTION DES LOANS---
    public void addLoan(int userId, int bookId) throws SQLException, ClassNotFoundException {
        String sql = "insert into loans (user_id,book_id,loan_date,due_date)" + "values (?,?,CURDATE(),DATE_ADD(CURDATE(),INTERVAL 15 DAY))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            psmt.setInt(1, userId);
            psmt.setInt(2, bookId);
            psmt.executeUpdate();
        }
    }
//----------5. METHODE SECOURS ------
    public Book findBookById(int bookId) throws SQLException, ClassNotFoundException {
        String sql = "select * from books where book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            psmt.setInt(1, bookId);
            try (ResultSet rs = psmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getBoolean("available"));
                }
            }
        }
        return null;
    }
//--------- 6. GESTION DES LOANS -------
    public List<Loan> getActiveLoansByUser(int userId) throws SQLException, ClassNotFoundException {
        List<Loan> activeloans = new ArrayList<>();
        String sql = "Select * from loans where user_id = ? and return_date is null";
        try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    activeloans.add(new Loan(
                            rs.getInt("id"),null,
                            findBookById(rs.getInt("book_id")),
                            rs.getDate("Loan_date").toLocalDate(),null,
                            rs.getDate("due_date").toLocalDate()
                            ));
                }
            }
        }
        return activeloans;
    }
}




