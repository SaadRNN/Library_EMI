package businessLogic;

import domainModel.Book;
import domainModel.Loan;
import domainModel.User;
import persistance.LibraryRepository;

import java.sql.SQLException;
import java.util.List;

public class LoanManager {
private LibraryRepository repository;
private UserManager userManager;

public LoanManager(LibraryRepository repository , UserManager userManager){
    this.repository=repository;
    this.userManager=userManager;
}
public void borrowBook(User user , int Bookid) throws SQLException , ClassNotFoundException {
    if (!userManager.canBorrow(user)){
        throw new IllegalArgumentException("Sorry"+user.getUsername()+" you can't borrow");
    }
    Book book=repository.findBookById(Bookid);
    if (book==null) throw new IllegalArgumentException("Book Not Found");
    if(!book.isAvailable()) throw new IllegalStateException("Book Not Available");

    repository.addLoan(user.getId(),Bookid);
}
public void returnBook(int loanId) throws SQLException, ClassNotFoundException {
    repository.returnLoan(loanId);
}
public List<Loan> getActiveLoans(User user) throws SQLException, ClassNotFoundException {
    return repository.getActiveLoansByUser(user.getId());
}

}
