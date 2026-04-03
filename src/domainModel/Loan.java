package domainModel;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
public class Loan {
    private int id;
    private User user;
    private Book book;
    private LocalDate loanDate;
    private LocalDate returnDate;
    private LocalDate dueDate;

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}

    public Book getBook() {return book;}
    public void setBook(Book book) {this.book = book;}

    public LocalDate getLoanDate() {return loanDate;}
    public void setLoanDate(LocalDate loanDate) {this.loanDate = loanDate;}

    public LocalDate getReturnDate() {return returnDate;}
    public void setReturnDate(LocalDate returnDate) {this.returnDate = returnDate;}

    public LocalDate getDueDate() {return dueDate;}
    public void setDueDate(LocalDate dueDate) {this.dueDate = dueDate;}

    public Loan(int id, User user, Book book, LocalDate loanDate, LocalDate returnDate, LocalDate dueDate) {
        this.id = id;
        this.user = user;
        this.book = book;
        this.loanDate = LocalDate.now();
        this.returnDate = returnDate;
        this.dueDate = loanDate.plusDays(15);
        book.setAvailable(false);

    }
    // Dans ta classe Loan
    public boolean isOverdue() {
        if (this.returnDate != null) {
            return false;
        }
        return LocalDate.now().isAfter(this.dueDate);
    }


    public float calculateFine() {
        // 1. Si pas de retard, pas d'amende
        if (!isOverdue()) {
            return 0;
        }

        // 2. On calcule le retard par rapport à la date limite (dueDate)
        // On compare aujourd'hui (ou la date de retour si elle existe) avec la dueDate
        LocalDate referenceDate = (returnDate != null) ? returnDate : LocalDate.now();

        long daysLate = ChronoUnit.DAYS.between(this.dueDate, referenceDate);

        // 3. Si le résultat est positif, on multiplie par le tarif (ex: 15 DH par jour)
        return (daysLate > 0) ? daysLate * 15.0f : 0.0f;
    }

    public String toString() {
        return user.getUsername() + " a emprunté le livre " + book.getTitle() + " le: " + getLoanDate();
    }
    }





