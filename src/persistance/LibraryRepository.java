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
                            rs.getString("email"),
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
        String sql = "insert into books (title,author,available) values (?,?,TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            psmt.setString(1, title);
            psmt.setString(2, author);
            psmt.executeUpdate();
        }
    }

    //-----------4 . GESTION DES LOANS---
    public void addLoan(int userId, int bookId) throws SQLException, ClassNotFoundException {
        String sqlLoan = "INSERT INTO loans (user_id, book_id, loan_date, due_date) VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 15 DAY))";
        String sqlUpdateBook = "UPDATE books SET available = false WHERE id = ?";

        // On ouvre UNE SEULE connexion pour les deux opérations
        try (Connection conn = DatabaseConnection.getConnection()) {

            // 1. Insertion du prêt
            try (PreparedStatement psmtLoan = conn.prepareStatement(sqlLoan)) {
                psmtLoan.setInt(1, userId);
                psmtLoan.setInt(2, bookId);
                psmtLoan.executeUpdate();
            }

            // 2. Mise à jour de la disponibilité du livre
            try (PreparedStatement psmtUpdate = conn.prepareStatement(sqlUpdateBook)) {
                psmtUpdate.setInt(1, bookId);
                psmtUpdate.executeUpdate();
            }
        }
    }
//----------5. METHODE SECOURS ------
    public Book findBookById(int bookId) throws SQLException, ClassNotFoundException {
        String sql = "select * from books where id = ?";
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
//-----------------7.RETOUR DES LOANS
public void returnLoan(int loanId) throws SQLException, ClassNotFoundException {

        int bookId = -1;
        String getLoanSql="SELECT book_id FROM loans WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(getLoanSql)) {
            pstmt.setInt(1, loanId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    bookId = rs.getInt("book_id");
                }
                else {
                    throw new IllegalArgumentException("Loan not found");
                }
            }
        }
        String returnSql ="UPDATE loans SET return_date = CURDATE() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(returnSql)) {
            pstmt.setInt(1, loanId);
            pstmt.executeUpdate();
        }
        String availableSql ="UPDATE books SET available = true WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(availableSql)){
            pstmt.setInt(1, bookId);
            pstmt.executeUpdate();
        }
}

}




