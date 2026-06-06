package persistance;
import domainModel.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
public class LibraryRepository {

    //-----1.Gestion des Users (LOGIN)---
    public void addUser(String username, String email, String password) throws SQLException, ClassNotFoundException {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); // ✅ on hash
        String sql = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, 'USER')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashedPassword); // ✅ on stocke le hash, jamais le mot de passe
            pstmt.executeUpdate();
        }
    }





    public User findUser(String username, String password) throws SQLException, ClassNotFoundException {
        String sql = "select * from users where username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    if (BCrypt.checkpw(password, hashedPassword)) { // ✅ on compare avec BCrypt
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                hashedPassword,
                                rs.getString("role")
                        );
                    }
                }
            }
        }
        return null;
    }
    public List<User> getAllUsers()
            throws SQLException, ClassNotFoundException {

        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                users.add(
                        new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                rs.getString("password"),
                                rs.getString("role")
                        )
                );
            }
        }

        return users;
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
                            rs.getInt("id"), null,
                            findBookById(rs.getInt("book_id")),
                            rs.getDate("loan_date").toLocalDate(), null,
                            rs.getDate("due_date").toLocalDate()
                    ));
                }
            }
        }
        return activeloans;
    }

    //-----------------7.RETOUR DES LOANS
    public void returnLoan(int loanId) throws SQLException, ClassNotFoundException {

        String getLoanSql = "SELECT book_id FROM loans WHERE id=?";
        String returnSql = "UPDATE loans SET return_date = CURDATE() WHERE id=?";
        String availableSql = "UPDATE books SET available = true WHERE id =?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int bookId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(getLoanSql)) {
                    pstmt.setInt(1, loanId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            bookId = rs.getInt("book_id");
                        } else {
                            throw new IllegalArgumentException("Loan not found");
                        }
                    }
                }

                //marqué prêt comme rendu
                try (PreparedStatement pstmt = conn.prepareStatement(returnSql)) {
                    pstmt.setInt(1, loanId);
                    pstmt.executeUpdate();
                }
//remettre le livre disponible
                try (PreparedStatement pstmt = conn.prepareStatement(availableSql)) {
                    pstmt.setInt(1, bookId);
                    pstmt.executeUpdate();
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    public List<Loan> getAllLoans()
            throws SQLException, ClassNotFoundException {

        List<Loan> loans = new ArrayList<>();

        String sql =
                "SELECT * FROM loans";

        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while(rs.next()) {

                User user = findUserById(
                        rs.getInt("user_id")
                );

                Book book = findBookById(
                        rs.getInt("book_id")
                );

                loans.add(
                        new Loan(
                                rs.getInt("id"),
                                user,
                                book,
                                rs.getDate("loan_date").toLocalDate(),
                                rs.getDate("return_date") == null
                                        ? null
                                        : rs.getDate("return_date").toLocalDate(),
                                rs.getDate("due_date").toLocalDate()
                        )
                );
            }
        }

        return loans;
    }
    public User findUserById(int id)
            throws SQLException, ClassNotFoundException {

        String sql =
                "SELECT * FROM users WHERE id=?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt =
                    conn.prepareStatement(sql)) {

            pstmt.setInt(1,id);

            try(ResultSet rs =
                        pstmt.executeQuery()) {

                if(rs.next()) {

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

        return null;
    }
    public void deleteBook(int bookId)
            throws SQLException, ClassNotFoundException {

        String sql =
                "DELETE FROM books WHERE id=?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt =
                    conn.prepareStatement(sql)) {

            pstmt.setInt(1,bookId);

            pstmt.executeUpdate();
        }
    }
    public void deleteUser(int userId)
            throws SQLException, ClassNotFoundException {

        String sql =
                "DELETE FROM users WHERE id=?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt =
                    conn.prepareStatement(sql)) {

            pstmt.setInt(1,userId);

            pstmt.executeUpdate();
        }
    }
}





      /*  int bookId = -1;
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


*/

