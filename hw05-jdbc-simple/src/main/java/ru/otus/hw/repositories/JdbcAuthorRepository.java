package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcAuthorRepository implements AuthorRepository {

    private final JdbcTemplate jdbc;

    @Override
    public List<Author> findAll() {
        String sql = "SELECT id, full_name FROM authors ORDER BY id";
        return jdbc.query(sql, new AuthorRowMapper());
    }

    @Override
    public Optional<Author> findById(long id) {
        String sql = "SELECT id, full_name FROM authors WHERE id = ?";
        List<Author> authors = jdbc.query(sql, new AuthorRowMapper(), id);
        return authors.isEmpty() ? Optional.empty() : Optional.of(authors.get(0));
    }

    private static class AuthorRowMapper implements RowMapper<Author> {

        @Override
        public Author mapRow(ResultSet rs, int i) throws SQLException {
            long id = rs.getLong("id");
            String fullName = rs.getString("full_name");
            return new Author(id, fullName);
        }
    }
}