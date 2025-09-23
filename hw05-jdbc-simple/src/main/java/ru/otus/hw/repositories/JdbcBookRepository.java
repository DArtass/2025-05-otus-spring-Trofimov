package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final JdbcTemplate jdbc;

    @Override
    public Optional<Book> findById(long id) {
        String sql = """
            SELECT b.id, b.title, 
                   a.id as author_id, a.full_name,
                   g.id as genre_id, g.name
            FROM books b
            LEFT JOIN authors a ON b.author_id = a.id
            LEFT JOIN genres g ON b.genre_id = g.id
            WHERE b.id = ?
            """;

        List<Book> books = jdbc.query(sql, new BookRowMapper(), id);
        return books.isEmpty() ? Optional.empty() : Optional.of(books.get(0));
    }

    @Override
    public List<Book> findAll() {
        String sql = """
            SELECT b.id, b.title, 
                   a.id as author_id, a.full_name,
                   g.id as genre_id, g.name
            FROM books b
            LEFT JOIN authors a ON b.author_id = a.id
            LEFT JOIN genres g ON b.genre_id = g.id
            ORDER BY b.id
            """;

        return jdbc.query(sql, new BookRowMapper());
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM books WHERE id = ?";
        jdbc.update(sql, id);
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();

        String sql = "INSERT INTO books (title, author_id, genre_id) VALUES (?, ?, ?)";

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, book.getTitle());
            ps.setLong(2, book.getAuthor().getId());
            ps.setLong(3, book.getGenre().getId());
            return ps;
        }, keyHolder);

        book.setId(keyHolder.getKeyAs(Long.class));
        return book;
    }

    private Book update(Book book) {
        String sql = "UPDATE books SET title = ?, author_id = ?, genre_id = ? WHERE id = ?";

        int rowsAffected = jdbc.update(sql,
                book.getTitle(),
                book.getAuthor().getId(),
                book.getGenre().getId(),
                book.getId());

        if (rowsAffected == 0) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }

        return book;
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String title = rs.getString("title");

            Author author = new Author(
                    rs.getLong("author_id"),
                    rs.getString("full_name")
            );

            Genre genre = new Genre(
                    rs.getLong("genre_id"),
                    rs.getString("name")
            );

            return new Book(id, title, author, genre);
        }
    }
}