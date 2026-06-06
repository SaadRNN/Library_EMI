package domainModel;

public class Book {

    private int id;
    private String title;
    private String author;
    private boolean available = true;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        if (id <= 0) {throw new IllegalArgumentException("ID invalide");}
        this.id = id;}
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Book(int id, String title, String author, boolean available) {

        setId(id);
        setTitle(title);
        setAuthor(author);
        this.available = available;
    }


}