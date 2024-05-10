import java.io.*;
import java.time.LocalDate;
import java.util.*;

class Member implements Serializable {
    String name;
    String email;
    LocalDate dueDate;
    public ArrayList<String> borrowedBooks = new ArrayList<>();
    public boolean notificationsEnabled;

    public Member(String name, String email) {
        this.name = name;
        this.email = email;
        this.notificationsEnabled = true;
    }

    public String getEmail() {
        return email;
    }
}

class Book implements Serializable {
    private int isbn;
    private String title;
    private String author;
    private LocalDate dueDate;
    private boolean isAvailable;

    public Book(int isbn, String title, String author, boolean isAvailable) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.isAvailable = isAvailable; // Corrected initialization
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}

class Library {
    private Map<Integer, Book> books;
    private Map<String, List<Integer>> borrowedBooks;
    private Map<String, List<Member>> members;
    private final int loanPeriodDays = 5;
    private final double finePerDay = 20.00;

    public Library() {
        books = new HashMap<>();
        borrowedBooks = new HashMap<>();
        members = new HashMap<>();
    }

    public void addBook(Book book) {
        if (books.containsKey(book.getIsbn())) {
            System.out.println("Book with ISBN " + book.getIsbn() + " already exists in the library.");
        } else {
            books.put(book.getIsbn(), book);
            System.out.println("Book added successfully.");
        }
    }

    public void addMember(Member member) {
        members.computeIfAbsent(member.getEmail(), k -> new ArrayList<>()).add(member);
        System.out.println("Member added successfully.");
    }

    public void borrowBook(String userId, int isbn) {
        if (books.containsKey(isbn)) {
            Book book = books.get(isbn);
            if (book.isAvailable()) {
                LocalDate dueDate = LocalDate.now().plusDays(loanPeriodDays);
                book.setDueDate(dueDate);
                book.setAvailable(false);
                borrowedBooks.computeIfAbsent(userId, k -> new ArrayList<>()).add(isbn);
                System.out.println("Book " + isbn + " borrowed successfully by user " + userId + ". Due date: " + dueDate);
            } else {
                System.out.println("Book " + isbn + " is not available for borrowing.");
            }
        } else {
            System.out.println("Book with ISBN " + isbn + " not found in the library.");
        }
    }

    public void returnBook(String userId, int isbn) {
        if (borrowedBooks.containsKey(userId) && borrowedBooks.get(userId).contains(isbn)) {
            Book book = books.get(isbn);
            LocalDate currentDate = LocalDate.now();
            long daysLate = Math.max(0, currentDate.until(book.getDueDate()).getDays());
            double fineAmount = daysLate * finePerDay;
            if (fineAmount > 0) {
                System.out.println("Book " + isbn + " returned late by " + daysLate + " days. Fine amount: $" + fineAmount);
            } else {
                System.out.println("Book " + isbn + " returned successfully by user " + userId + ".");
            }
            book.setAvailable(true);
            borrowedBooks.get(userId).remove(Integer.valueOf(isbn));
        } else {
            System.out.println("Book " + isbn + " not borrowed by user " + userId + ".");
        }
    }

    public void showAllBooks() {
        if (books.isEmpty()) {
            System.out.println("No books available in the library.");
        } else {
            System.out.println("Books available in the library:");
            for (Book book : books.values()) {
                System.out.println("ISBN: " + book.getIsbn() + ", Title: " + book.getTitle() + ", Author: " + book.getAuthor() + ", Due Date: " + book.getDueDate());
            }
        }
    }

    public void processFines() {
        Thread fineProcessingThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10 * 1000); // Sleep for 10 seconds
                    for (Map.Entry<String, List<Integer>> entry : borrowedBooks.entrySet()) {
                        String userId = entry.getKey();
                        List<Integer> borrowedIsbns = entry.getValue();
                        double totalFine = 0;
                        for (int isbn : borrowedIsbns) {
                            Book book = books.get(isbn);
                            if (book != null) {
                                LocalDate currentDate = LocalDate.now();
                                long daysLate = Math.max(0, currentDate.until(book.getDueDate()).getDays());
                                double fineAmount = daysLate * finePerDay;
                                totalFine += fineAmount;
                            }
                        }
                        if (totalFine > 0) {
                            System.out.println("Total fine for user " + userId + ": $" + totalFine);
                            // Here you can update the fine in your database or perform any other necessary action
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        fineProcessingThread.setDaemon(true);
        fineProcessingThread.start();
    }

    public class NotificationSender extends Thread {
        private static final long INTERVAL = 35000;//  every 20 seconds the notifications will be checked

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(INTERVAL);
                    sendNotifications();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendNotifications() {
            LocalDate currentDate = LocalDate.now();
            System.out.println("Sending notifications for due and overdue books...");
            boolean notificationsSent = false;
            for (List<Member> memberList : members.values()) {
                for (Member member : memberList) {
                    if (!member.borrowedBooks.isEmpty() && member.notificationsEnabled) {
                        StringBuilder notificationMessage = new StringBuilder();
                        notificationMessage.append("Dear ").append(member.name).append(",\n\n");
                        notificationMessage.append("Here is the status of your borrowed books:\n\n");

                        boolean hasDueOrOverdueBooks = false;
                        for (String bookTitle : member.borrowedBooks) {
                            Book book = getBookByTitle(bookTitle);
                            if (book != null) {
                                LocalDate dueDate = book.getDueDate();
                                if (currentDate.isAfter(dueDate)) {
                                    notificationMessage.append("- ").append(bookTitle).append(" (Overdue)\n");
                                    hasDueOrOverdueBooks = true;
                                } else if (currentDate.plusDays(7).isAfter(dueDate)) {
                                    notificationMessage.append("- ").append(bookTitle).append(" (Due in ").append(dueDate.until(currentDate).getDays()).append(" days)\n");
                                    hasDueOrOverdueBooks = true;
                                } else {
                                    notificationMessage.append("- ").append(bookTitle).append("\n");
                                }
                            }
                        }

                        if (hasDueOrOverdueBooks) {
                            notificationMessage.append("\nThank you for using our library services.\n");
                            System.out.println("Notification sent to " + member.name + ":\n" + notificationMessage);
                            notificationsSent = true;
                        }
                    }
                }
            }
            if (!notificationsSent) {
                System.out.println("No notifications to send.");
            }
            System.out.println();
        }

        private Book getBookByTitle(String title) {
            for (Book book : books.values()) {
                if (book.getTitle().equals(title)) {
                    return book;
                }
            }
            return null;
        }
    }

    public void checkDueDates(String userId) {
        List<Integer> borrowedIsbns = borrowedBooks.getOrDefault(userId, new ArrayList<>());
        if (borrowedIsbns.isEmpty()) {
            System.out.println("You have no borrowed books.");
        } else {
            System.out.println("Due dates for books borrowed by user " + userId + ":");
            for (int isbn : borrowedIsbns) {
                Book book = books.get(isbn);
                if (book != null) {
                    System.out.println("Book ISBN: " + isbn + ", Title: " + book.getTitle() + ", Due Date: " + book.getDueDate());
                }
            }
        }
    }

    public static class LibraryData implements Serializable {
        public List<Book> bookCollection;
        public List<Member> memberCollection;
        public List<Transaction> transactionList;

        public LibraryData() {
            this.bookCollection = new ArrayList<>();
            this.memberCollection = new ArrayList<>();
            this.transactionList = new ArrayList<>();
        }

        public static class Transaction implements Serializable {
            public String memberName;
            public String bookTitle;
            public LocalDate transactionDate;
            public TransactionType transactionType;

            public Transaction(String memberName, String bookTitle, LocalDate transactionDate, TransactionType transactionType) {
                this.memberName = memberName;
                this.bookTitle = bookTitle;
                this.transactionDate = transactionDate;
                this.transactionType = transactionType;
            }
        }

        public enum TransactionType implements Serializable {
            CHECKOUT,
            RETURN
        }

        private static final String DATA_FILE = "library_data.ser";

        public static void saveLibraryData(LibraryData libraryData) {
            try {

                FileOutputStream fileOut = new FileOutputStream(DATA_FILE);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(libraryData);
                out.close();
                fileOut.close();
                System.out.println("Library data saved successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static LibraryData loadLibraryData() {
            LibraryData libraryData = null;
            try {
                FileInputStream fileIn = new FileInputStream(DATA_FILE);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                libraryData = (LibraryData) in.readObject();
                in.close();
                fileIn.close();
                System.out.println("Library data loaded successfully.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("No saved data found. Creating new library data.");
                libraryData = new LibraryData();
            }
            return libraryData;
        }
    }

    public void viewFines(String userId) {
        List<Integer> borrowedIsbns = borrowedBooks.getOrDefault(userId, new ArrayList<>());
        double totalFine = 0;
        for (int isbn : borrowedIsbns) {
            Book book = books.get(isbn);
            if (book != null) {
                LocalDate currentDate = LocalDate.now();
                long daysLate = Math.max(0, currentDate.until(book.getDueDate()).getDays());
                double fineAmount = daysLate * finePerDay;
                totalFine += fineAmount;
            }
        }
        System.out.println("Total fine for user " + userId + ": R" + totalFine);
    }
}

public class Main {
    public static void main(String[] args) {
        Library library = new Library();
        Scanner scanner = new Scanner(System.in);
        String memberName = ""; // Declare and initialize memberName here

        // Add some members
        Member member1 = new Member("John", "john@example.com");
        Member member2 = new Member("Jane", "jane@example.com");

        library.addMember(member1);
        library.addMember(member2);

        // Add some books
        Book book1 = new Book(123456, "Harry Potter", "J.K. Rowling", true);
        Book book2 = new Book(789012, "Lord of the Rings", "J.R.R. Tolkien", true);
        Book book3 = new Book(345678, "The Hobbit", "J.R.R. Tolkien", true);
        Book book4 = new Book(901234, "The Fellowship of the Ring", "J.R.R. Tolkien", true);
        Book book5 = new Book(12129, "Zanele not working ", "Junior", true);
        library.addBook(book1);
        library.addBook(book2);
        library.addBook(book3);
        library.addBook(book4);
        library.addBook(book5);

        System.out.println("\n MENU");

        System.out.println("1. Add a book to the library");
        System.out.println("2. Add a user");
        System.out.println("3. Borrow a book");
        System.out.println("4. Return a book");
        System.out.println("5. Show all books");
        System.out.println("6. Process fines");
        System.out.println("7. Send notifications");
        System.out.println("8. Check due dates for a user");
        System.out.println("9. View fines for a user");
        System.out.println("10. Exit");

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
                    Book newBook = new Book(isbn, title, author, true);
                    library.addBook(newBook);
                    break;
                case 2:
                    System.out.print("Enter your email: ");
                    String email = scanner.nextLine();
                    library.addMember(new Member(memberName, email));
                    break;
                case 3:
                    System.out.print("Enter the ISBN of the book you want to borrow: ");
                    int borrowIsbn = scanner.nextInt();
                    scanner.nextLine();
                    library.borrowBook(memberName, borrowIsbn);
                    break;
                case 4:
                    System.out.print("Enter the ISBN of the book you are returning: ");
                    int returnIsbn = scanner.nextInt();
                    scanner.nextLine();
                    library.returnBook(memberName, returnIsbn);
                    break;
                case 5:
                    library.showAllBooks();
                    break;
                case 6:
                    library.processFines();
                    break;
                case 7:
                    Library.NotificationSender sender = library.new NotificationSender();
                    sender.start();
                    break;
                case 8:
                    library.checkDueDates(memberName);
                    break;
                case 9:
                    library.viewFines(memberName);
                    break;
                case 10:
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }
    }
}
