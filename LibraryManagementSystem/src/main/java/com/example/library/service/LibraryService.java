package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.model.Loan;
import com.example.library.model.Member;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the library's business rules. Data is intentionally kept in memory in this starter version;
 * a repository/database layer can be added later without putting business logic in the UI.
 */
public class LibraryService {
    private final List<Book> books = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    private final List<Loan> loans = new ArrayList<>();

    private int nextBookId = 1;
    private int nextMemberId = 1;
    private int nextLoanId = 1;

    public List<Book> getBooks() { return new ArrayList<>(books); }
    public List<Member> getMembers() { return new ArrayList<>(members); }
    public List<Loan> getLoans() { return new ArrayList<>(loans); }

    public Book findBookById(int id) {
        for (Book book : books) if (book.getId() == id) return book;
        return null;
    }

    public Member findMemberById(int id) {
        for (Member member : members) if (member.getId() == id) return member;
        return null;
    }

    public Loan findLoanById(int id) {
        for (Loan loan : loans) if (loan.getId() == id) return loan;
        return null;
    }

    public Book addBook(String title, String author, String isbn, int totalCopies) {
        title = required(title, "عنوان کتاب");
        author = required(author, "نام نویسنده");
        isbn = required(isbn, "شابک");
        if (totalCopies < 1) throw new IllegalArgumentException("تعداد نسخه باید حداقل ۱ باشد.");
        ensureUniqueIsbn(isbn, -1);

        Book book = new Book(nextBookId++, title, author, isbn, totalCopies);
        books.add(book);
        return book;
    }

    public void updateBook(int id, String title, String author, String isbn, int totalCopies) {
        Book book = requireBook(id);
        title = required(title, "عنوان کتاب");
        author = required(author, "نام نویسنده");
        isbn = required(isbn, "شابک");
        if (totalCopies < 1) throw new IllegalArgumentException("تعداد نسخه باید حداقل ۱ باشد.");
        ensureUniqueIsbn(isbn, id);

        int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
        if (totalCopies < borrowedCopies) {
            throw new IllegalArgumentException("تعداد نسخه نمی‌تواند از تعداد نسخه‌های امانت‌داده‌شده کمتر باشد: " + borrowedCopies);
        }

        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(totalCopies - borrowedCopies);
    }

    public void deleteBook(int id) {
        Book book = requireBook(id);
        for (Loan loan : loans) {
            if (loan.getBookId() == id) {
                throw new IllegalArgumentException("این کتاب سابقه امانت دارد و برای حفظ سوابق قابل حذف نیست.");
            }
        }
        books.remove(book);
    }

    public Member addMember(String fullName, String studentNumber, String phone) {
        fullName = required(fullName, "نام و نام خانوادگی");
        studentNumber = required(studentNumber, "شماره دانشجویی");
        phone = phone == null ? "" : phone.trim();
        ensureUniqueStudentNumber(studentNumber, -1);

        Member member = new Member(nextMemberId++, fullName, studentNumber, phone);
        members.add(member);
        return member;
    }

    public void updateMember(int id, String fullName, String studentNumber, String phone) {
        Member member = requireMember(id);
        fullName = required(fullName, "نام و نام خانوادگی");
        studentNumber = required(studentNumber, "شماره دانشجویی");
        phone = phone == null ? "" : phone.trim();
        ensureUniqueStudentNumber(studentNumber, id);

        member.setFullName(fullName);
        member.setStudentNumber(studentNumber);
        member.setPhone(phone);
    }

    public void deleteMember(int id) {
        Member member = requireMember(id);
        for (Loan loan : loans) {
            if (loan.getMemberId() == id) {
                throw new IllegalArgumentException("این عضو سابقه امانت دارد و برای حفظ سوابق قابل حذف نیست.");
            }
        }
        members.remove(member);
    }

    public Loan borrowBook(int bookId, int memberId, LocalDate dueDate) {
        Book book = requireBook(bookId);
        requireMember(memberId);
        if (book.getAvailableCopies() < 1) {
            throw new IllegalArgumentException("نسخه‌ای از این کتاب برای امانت موجود نیست.");
        }
        if (dueDate == null || dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("تاریخ سررسید نمی‌تواند قبل از امروز باشد.");
        }

        Loan loan = new Loan(nextLoanId++, bookId, memberId, LocalDate.now(), dueDate);
        loans.add(loan);
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return loan;
    }

    public void returnBook(int loanId) {
        Loan loan = findLoanById(loanId);
        if (loan == null) throw new IllegalArgumentException("رکورد امانت پیدا نشد.");
        if (loan.isReturned()) throw new IllegalArgumentException("این کتاب قبلاً بازگردانده شده است.");

        Book book = findBookById(loan.getBookId());
        if (book == null) throw new IllegalStateException("کتاب مرتبط با این امانت پیدا نشد.");
        loan.markReturned(LocalDate.now());
        book.setAvailableCopies(Math.min(book.getTotalCopies(), book.getAvailableCopies() + 1));
    }

    public int getTotalCopiesCount() {
        int total = 0;
        for (Book book : books) total += book.getTotalCopies();
        return total;
    }

    public int getAvailableCopiesCount() {
        int total = 0;
        for (Book book : books) total += book.getAvailableCopies();
        return total;
    }

    public long getActiveLoanCount() {
        return loans.stream().filter(loan -> !loan.isReturned()).count();
    }

    public long getOverdueLoanCount() {
        return loans.stream().filter(loan -> !loan.isReturned() && loan.getOverdueDays() > 0).count();
    }

    private Book requireBook(int id) {
        Book book = findBookById(id);
        if (book == null) throw new IllegalArgumentException("کتاب موردنظر پیدا نشد.");
        return book;
    }

    private Member requireMember(int id) {
        Member member = findMemberById(id);
        if (member == null) throw new IllegalArgumentException("عضو موردنظر پیدا نشد.");
        return member;
    }

    private String required(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("فیلد «" + fieldName + "» نباید خالی باشد.");
        }
        return value.trim();
    }

    private void ensureUniqueIsbn(String isbn, int currentId) {
        for (Book book : books) {
            if (book.getId() != currentId && book.getIsbn().equalsIgnoreCase(isbn)) {
                throw new IllegalArgumentException("این شابک قبلاً برای کتاب دیگری ثبت شده است.");
            }
        }
    }

    private void ensureUniqueStudentNumber(String studentNumber, int currentId) {
        for (Member member : members) {
            if (member.getId() != currentId && member.getStudentNumber().equalsIgnoreCase(studentNumber)) {
                throw new IllegalArgumentException("این شماره دانشجویی قبلاً ثبت شده است.");
            }
        }
    }
}
