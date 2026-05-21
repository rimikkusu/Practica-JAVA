package com.bragari.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlHelper {

    private SqlHelper() {}

    @FunctionalInterface
    public interface StatementPreparer {
        void prepare(PreparedStatement ps) throws SQLException;
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public static <T> List<T> queryList(String sql, StatementPreparer preparer, RowMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (preparer != null) preparer.prepare(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapper.map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return results;
    }

    public static <T> Optional<T> queryOne(String sql, StatementPreparer preparer, RowMapper<T> mapper) {
        List<T> list = queryList(sql, preparer, mapper);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public static int update(String sql, StatementPreparer preparer) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (preparer != null) preparer.prepare(ps);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static int insertReturningId(String sql, StatementPreparer preparer) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (preparer != null) preparer.prepare(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return -1;
    }
}
