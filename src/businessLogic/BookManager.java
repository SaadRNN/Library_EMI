package businessLogic;

import domainModel.User;
import persistance.LibraryRepository;
import domainModel.Book;

import java.sql.SQLException;
import java.util.List;


public class BookManager {
private LibraryRepository repository;
public BookManager(LibraryRepository repository) {
    this.repository = repository;
}

public List<Book> getAllBooks() throws SQLException {
    return repository.getAllBooks();
}
public void addBook(User currentUser, String title, String author) throws SQLException, ClassNotFoundException {
    if(currentUser.getRole()!=User.Role.ADMIN){
        throw new SecurityException("You are not allowed to add Books !");
    }
    if (title==null|| title.isBlank()) throw new IllegalArgumentException("Title is required");
    if (author==null|| author.isBlank()) throw new IllegalArgumentException("Author is required");
    repository.addBook(title,author);
}
public Book findBookById(int id) throws SQLException, ClassNotFoundException {
    Book book = repository.findBookById(id);
    if (book == null) throw new IllegalArgumentException("Book with id " + id + " not found !");
    return book;
}
}
