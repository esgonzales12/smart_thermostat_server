package org.estefan.dao;

import org.estefan.logging.StaticLogBase;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.estefan.dao.annotations.*;

public abstract class DaoBase extends StaticLogBase {

    private static final int GENERATED_KEY = 1;
    public Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn =  DriverManager.getConnection("jdbc:sqlite://home/ec2-user/iot_server/temps.db");
            return conn;
        } catch (SQLException e) {
            log.severe("UNABLE TO OPEN CONNECTION");
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeIfPresent(AutoCloseable o) {
        if (o != null) {
            try {
                o.close();
            } catch (Exception e) {
                log.warning("ERROR CLOSING RESOURCE");
            }
        }
    }

    public int delete(String tableName, String columnIdentifier, String fieldValue) {
        String sql = String.format("""
                DELETE FROM %s
                WHERE %s.%s = '%s';
                """, tableName, tableName,
                columnIdentifier, fieldValue);
        return delete(sql);
    }

    private int delete(String sql) {
        PreparedStatement statement = null;
        Connection conn = null;
        int rowsAffected = 0;
        try {
            conn = getConnection();
            statement = conn.prepareStatement(sql);
            rowsAffected = statement.executeUpdate();
        } catch (SQLException e) {
            log.severe("SQL EXCEPTION ON SAVE");
            log.severe(e.getMessage());
        } finally {
            closeIfPresent(conn);
            closeIfPresent(statement);
        }
        return rowsAffected;
    }

    public <T> T create(Class<T> c, String sql) {
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet results = null;
        T result = null;
        try {
            conn = getConnection();
            statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.executeUpdate();
            results = statement.getGeneratedKeys();
            if (results.next()) {
                long primaryKey = results.getLong(GENERATED_KEY);
                String query = findEntityQuery(c, primaryKey);
                Optional<T> queryResult = selectOne(c, query);
                if (queryResult.isPresent()) {
                    result = queryResult.get();
                }
            }
        } catch (SQLException e) {
            log.severe("SQL EXCEPTION ON SAVE");
            log.severe(e.getMessage());
        } finally {
            closeIfPresent(conn);
            closeIfPresent(statement);
            closeIfPresent(results);
        }
        return result;
    }

    public int update(String sql) {
        PreparedStatement statement = null;
        Connection conn = null;
        try {
            conn = getConnection();
            statement = conn.prepareStatement(sql);
            return statement.executeUpdate();
        } catch (SQLException e) {
            log.severe("SQL EXCEPTION ON SAVE");
            log.severe(e.getMessage());
        } finally {
            closeIfPresent(conn);
            closeIfPresent(statement);
        }
        return 0;
    }

    public <T> T save(Class<T> c, String sql, long primaryKey) {
        PreparedStatement statement = null;
        Connection conn = null;
        T result = null;
        try {
            conn = getConnection();
            statement = conn.prepareStatement(sql);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected != 0) {
                String query = findEntityQuery(c, primaryKey);
                Optional<T> savedEntity = selectOne(c, query);
                if (savedEntity.isPresent()) {
                    result = savedEntity.get();
                }
            }
        } catch (SQLException e) {
            log.severe("SQL EXCEPTION ON SAVE");
            log.severe(e.getMessage());
        } finally {
            closeIfPresent(conn);
            closeIfPresent(statement);
        }
        return result;
    }

    private <T> String findEntityQuery(Class<T> c, long primaryKey) {
        String tableName = null;
        String primaryKeyColumnIdentifier = null;

        for (Annotation annotation: c.getDeclaredAnnotations()) {
            if (annotation.annotationType() == Table.class) {
                tableName = c.getAnnotation(Table.class).name();
            }
        }
        for (Field f: c.getDeclaredFields()) {
            if (f.isAnnotationPresent(PrimaryKey.class)) {
                primaryKeyColumnIdentifier = f.getAnnotation(PrimaryKey.class).columnIdentifier();
            }
        }

        if (tableName == null ) {
            log.severe("CLASS IS NOT A TABLE ENTITY");
            throw new RuntimeException("TABLE ANNOTATION NOT FOUND");
        } else if (primaryKeyColumnIdentifier == null) {
            throw new RuntimeException("PRIMARY KEY ANNOTATION NOT FOUND");
        }

        return String.format("""
                SELECT * FROM %s
                WHERE %s.%s = '%d';
                """, tableName, tableName,
                primaryKeyColumnIdentifier, primaryKey);

    }

    public <T> List<T> selectMany(Class<T> c, String sql) {
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet results = null;
        try {
            conn = getConnection();
            statement = conn.prepareStatement(sql);
            results = statement.executeQuery();
            return parseMany(c, results);
        } catch (SQLException e) {
            log.severe(e.getMessage());
            return new ArrayList<>();
        } finally {
            closeIfPresent(conn);
            closeIfPresent(statement);
            closeIfPresent(results);
        }
    }

    public <T> Optional<T> selectOne(Class<T> c, String sql) {
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet results = null;
        try {
            conn = getConnection();
            statement = conn.prepareStatement(sql);
            results = statement.executeQuery();
            if (results.next()) {
                T t  = parse(c, results);
                return Optional.of(t);
            }
            return Optional.empty();
        } catch (SQLException e) {
            log.severe("EXCEPTION ON SELECT ONE");
            log.severe(e.getMessage());
            return Optional.empty();
        } finally {
            closeIfPresent(conn);
            closeIfPresent(statement);
            closeIfPresent(results);
        }
    }

    public <T> List<T> parseMany(Class<T> c, ResultSet resultSet) {
        List<T> res = new ArrayList<>();
        try {
            while (resultSet.next()) {
                res.add(parse(c, resultSet));
            }
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T parse(Class<T> c, ResultSet resultSet) throws SQLException {
        T typeObj;
        try {
            typeObj = c.getDeclaredConstructor().newInstance();
            for (Field field: c.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Column.class)) {
                    String label = field
                            .getAnnotation(Column.class)
                            .name();
                    Object o = resultSet.getObject(label);
                    if (o instanceof BigInteger) {
                        o = ((BigInteger) o).longValue();
                    }
                    field.set(typeObj, o);
                }
            }
        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return typeObj;
    }

}
