package com.library.repository;

import com.library.model.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    private SimpleJdbcCall spGetBooks;

    @PostConstruct
    private void init() {
        spGetBooks = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_books_with_availability")
                .returningResultSet("books", (rs, rowNum) -> {
                    Book b = new Book();
                    b.setIsbn(rs.getString("isbn"));
                    b.setName(rs.getString("name"));
                    b.setAuthor(rs.getString("author"));
                    b.setIntroduction(rs.getString("introduction"));
                    b.setPublisher(rs.getString("publisher"));
                    b.setTotalCopies(rs.getInt("total_copies"));
                    b.setAvailableCopies(rs.getInt("available_copies"));
                    var pd = rs.getDate("publish_date");
                    if (pd != null) b.setPublishDate(pd.toLocalDate());
                    b.setCoverUrl(rs.getString("cover_url"));
                    return b;
                });
    }

    @SuppressWarnings("unchecked")
    public List<Book> findAllWithAvailability() {
        Map<String, Object> result = spGetBooks.execute();
        return (List<Book>) result.get("books");
    }
}
