import java.util.*;

class Book {
    private int isbn;
    private String title;
    private String author;
    public Boolean IsAvailable;

    public Book(int isbn, String title, String author , Boolean IsAvailable) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.IsAvailable = true;

    }

    public int getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Boolean IsAvailable() {
        return IsAvailable;
    }
    public void setIsAvailable(Boolean IsAvailable) {
        this.IsAvailable = IsAvailable;
    }
}

class Library {
    private Map<Integer, Book> books;
    private Map<String, List<Integer>> borrowedBooks;
    private Map<String, List<Integer>> users;

    public Library() {
        books = new HashMap<>();
        borrowedBooks = new HashMap<>();
        users = new HashMap<>();
    }

    public void addBook(Book book) {
        if (books.containsKey(book.getIsbn())) {
            System.out.println("Book with ISBN " + book.getIsbn() + " already exists in the library.");
        } else {
            books.put(book.getIsbn(), book);
            System.out.println("Book added successfully.");
        }
    }

    public void addUser(String userId) {
        users.put(userId, new ArrayList<>());
        System.out.println("User " + userId + " added successfully.");
    }

    public void borrowBook(String userId, int isbn, Boolean IsAvailable) {
        if (books.containsKey(isbn)) {
            borrowedBooks.computeIfAbsent(userId, k -> new ArrayList<>()).add(isbn);
            users.computeIfAbsent(userId, k -> new ArrayList<>()).add(isbn);
            System.out.println("Book " + isbn + " borrowed successfully by user " + userId + ".");
        } else {
            System.out.println("Book with ISBN " + isbn + " not found in the library.");
        }
    }

    public void returnBook(String userId, int isbn) {
        if (borrowedBooks.containsKey(userId) && borrowedBooks.get(userId).contains(isbn)) {
            borrowedBooks.get(userId).remove(Integer.valueOf(isbn));
            users.get(userId).remove(Integer.valueOf(isbn));
            System.out.println("Book " + isbn + " returned successfully by user " + userId + ".");
        } else {
            System.out.println("Book " + isbn + " not borrowed by user " + userId + ".");
        }
    }

    public void showAllBooks() {
        for (Book book : books.values()) {
            System.out.println("ISBN: " + book.getIsbn() + ", Title: " + book.getTitle() + ", Author: " + book.getAuthor());
        }
    }

    public static void main(String[] args) {
        Library library = new Library();
        Scanner scanner = new Scanner(System.in);

        // Add some books
        Book book1 = new Book(123456, "Harry Potter", "J.K. Rowling",true);
        Book book2 = new Book(789012, "Lord of the Rings", "J.R.R. Tolkien" , true);
        Book book3 = new Book(345678, "The Hobbit", "J.R.R. Tolkien", true);
        Book book4 = new Book(901234, "The Fellowship of the Ring", "J.R.R. Tolkien" , true);
        Book book5 = new Book(12129, "Zanele not working ", "Junior", true);
        library.addBook(book1);
        library.addBook(book2);
        library.addBook(book3);
        library.addBook(book4);
        library.addBook(book5);

        // Add a user
        String userId = "";

        System.out.println("\n MENU");

        System.out.println("1. Add a book to the library");
        System.out.println("2. Add a user");
        System.out.println("3. Borrow a book");
        System.out.println("4. Return a book");
        System.out.println("5. Show all books");
        System.out.println("6. Exit");

    while (true) {


        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                System.out.print("Enter the ISBN of the book: ");
                int isbn = scanner.nextInt();
                scanner.nextLine();
                System.out.print("Enter the title of the book: ");
                String title = scanner.nextLine();
                System.out.print("Enter the author of the book: ");
                String author = scanner.nextLine();
                Book newBook = new Book(isbn, title, author,true);
                library.addBook(newBook);
                break;
            case 2:
                System.out.print("Enter your user ID: ");
                userId = scanner.nextLine();
                library.addUser(userId);
                break;
            case 3:
                System.out.print("Enter the ISBN of the book you want to borrow: ");
                int borrowIsbn = scanner.nextInt();
                scanner.nextLine();
                library.borrowBook(userId, borrowIsbn, true);
                break;
            case 4:
                System.out.print("Enter the ISBN of the book you are returning: ");
                int returnIsbn = scanner.nextInt();
                scanner.nextLine();
                library.returnBook(userId, returnIsbn);
                break;
            case 5:
                library.showAllBooks();
                break;
            case 6:
                System.out.println("Goodbye!");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                break;

            }
        }
    }
}
