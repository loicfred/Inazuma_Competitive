package iecompbot.springboot.data;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

import static my.utilities.json.JSONItem.GSON;

@SuppressWarnings("all")
public abstract class DatabaseObject<T> {
    protected transient static JdbcTemplate jdbcTemplate;

    protected transient final Class<T> entityClass;
    protected transient final List<Field> cachedFields;
    protected transient final RowMapper<T> rowMapper;
    protected transient final String tableName;

    protected DatabaseObject() {
        this.entityClass = (Class<T>) getClass();
        this.cachedFields = new ArrayList<>();
        
        TableName annotation = entityClass.getAnnotation(TableName.class);
        if (annotation != null) tableName = annotation.value().toLowerCase();
        else tableName = entityClass.getSimpleName().toLowerCase();
        
        Class<?> clz = entityClass;
        while (clz != null) {
            this.cachedFields.addAll(Arrays.stream(clz.getDeclaredFields()).filter(f -> !Modifier.isTransient(f.getModifiers())).peek(f -> f.setAccessible(true)).collect(Collectors.toList()));
            clz = clz.getSuperclass();
        }
        this.rowMapper = (rs, rowNum) -> mapResultSetToObject(rs, entityClass);
    }

    public static void setJdbcTemplate(JdbcTemplate template) {
        jdbcTemplate = template;
    }

    protected List<String> IDFields() {
        return List.of("ID");
    }

    private static <T> T mapResultSetToObject(ResultSet rs, Class<T> clazz) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            T item = ctor.newInstance();
            Class<?> clz = clazz;
            while (clz != null) {
                for (Field f : clz.getDeclaredFields()) {
                    if (Modifier.isTransient(f.getModifiers()) || Modifier.isStatic(f.getModifiers())) continue;
                    f.setAccessible(true);
                    Object value = rs.getObject(f.getName());
                    if (value != null) {
                        if (value instanceof java.sql.Date D) {
                            f.set(item, D.toLocalDate());
                        } else {
                            f.set(item, value);
                        }
                    }
                }
                clz = clz.getSuperclass();
            }
            return item;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to map ResultSet to " + clazz.getSimpleName(), e);
        }
    }

    public int Write() {
        Result result = getResult(false);
        String sql = "INSERT INTO " + tableName + " (" + result.columns() + ") VALUES (" + result.placeholders() + ")";
        return jdbcTemplate.update(sql, result.values());
    }
    public Optional<T> WriteThenReturn() {
        try {
            Result result = getResult(false);
            String sql = "INSERT INTO " + tableName + " (" + result.columns() + ") VALUES (" + result.placeholders() + ") RETURNING *";
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapResultSetToObject(rs, entityClass), result.values()).stream().findFirst();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write object", e);
        }
    }

    public int Upsert() {
        Result result = getResult(true);
        String sql = "INSERT INTO " + tableName + " (" + result.columns() + ") VALUES (" + result.placeholders() + ") ON DUPLICATE KEY UPDATE " + result.updateClause();
        return jdbcTemplate.update(sql, result.values());
    }
    public Optional<T> UpsertThenReturn() {
        try {
            Result result = getResult(true);
            String sql = "INSERT INTO " + tableName + " (" + result.columns() + ") VALUES (" + result.placeholders() + ") ON DUPLICATE KEY UPDATE " + result.updateClause() + " RETURNING *";
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapResultSetToObject(rs, entityClass), result.values()).stream().findFirst();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write object", e);
        }
    }

    public int Update() {
        try {
            String setClause = cachedFields.stream().map(f -> f.getName() + " = ?").collect(Collectors.joining(", "));
            List<Object> setValues = cachedFields.stream()
                    .map(f -> {
                        try { return f.get(this); }
                        catch (IllegalAccessException e) { throw new RuntimeException(e); }
                    }).collect(Collectors.toList());

            String whereClause = IDFields().stream().map(ID -> ID + " = ?").collect(Collectors.joining(" AND "));
            List<Object> whereValues = new ArrayList<>();
            for (String ID : IDFields()) whereValues.add(cachedFields.stream().filter(f -> f.getName().equalsIgnoreCase(ID)).findFirst().orElseThrow().get(this));

            List<Object> finalValues = new ArrayList<>();
            finalValues.addAll(setValues);
            finalValues.addAll(whereValues);

            String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + whereClause;
            SQLCleaner C = new SQLCleaner(sql, finalValues);
            return jdbcTemplate.update(C.newSQL, C.newParams);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("No ID field found in " + tableName + ".");
        }
    }
    public int UpdateOnly(String... columns) {
        try {
            String setClause = cachedFields.stream().filter(f -> Arrays.stream(columns).anyMatch(c -> c == f.getName())).map(f -> f.getName() + " = ?").collect(Collectors.joining(", "));
            List<Object> setValues = cachedFields.stream().filter(f -> Arrays.stream(columns).anyMatch(c -> c == f.getName()))
                    .map(f -> {
                        try { return f.get(this); }
                        catch (IllegalAccessException e) { throw new RuntimeException(e); }
                    }).collect(Collectors.toList());

            String whereClause = IDFields().stream().map(ID -> ID + " = ?").collect(Collectors.joining(" AND "));
            List<Object> whereValues = new ArrayList<>();
            for (String ID : IDFields()) whereValues.add(cachedFields.stream().filter(f -> f.getName().equalsIgnoreCase(ID)).findFirst().orElseThrow().get(this));

            List<Object> finalValues = new ArrayList<>();
            finalValues.addAll(setValues);
            finalValues.addAll(whereValues);

            String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + whereClause;
            SQLCleaner C = new SQLCleaner(sql, finalValues);
            return jdbcTemplate.update(C.newSQL, C.newParams);
        } catch (Exception e) {
            throw new RuntimeException("No ID field found in " + tableName + ".");
        }

    }
    public int Delete() {
        try {
            List<Object> values = new ArrayList<>();
            for (String ID : IDFields()) values.add(cachedFields.stream().filter(f -> f.getName().equalsIgnoreCase(ID)).findFirst().orElseThrow().get(this));
            String sql = "DELETE FROM " + tableName + " WHERE " + IDFields().stream().map(ID -> ID + " = ?").collect(Collectors.joining(" AND "));

            SQLCleaner C = new SQLCleaner(sql, values);
            return jdbcTemplate.update(C.newSQL, C.newParams);
        } catch (Exception e) {
            throw new RuntimeException("No ID field found in " + tableName + ".");
        }
    }
    public static <T> int Count(Class<T> clazz) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + getTableName(clazz), Integer.class);
    }
    public static <T> int Count(Class<T> clazz, String whereClause, Object... args) {
        SQLCleaner C = new SQLCleaner(whereClause, args);
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + getTableName(clazz) + " WHERE " + C.newSQL, Integer.class, C.newParams);
    }
    public static <T> T getRandom(Class<T> clazz) {
        try {
            List<T> Items = getAll(clazz);
            return Items.get(new Random().nextInt(Items.size()));
        } catch (Exception ignored) {
            return null;
        }
    }
    public static <T> T getRandom(Class<T> clazz, String whereClause, Object... args) {
        try {
            List<T> Items = getAllWhere(clazz, whereClause, args);
            return Items.get(new Random().nextInt(Items.size()));
        } catch (Exception ignored) {
            return null;
        }
    }

    public int IncrementColumn(String column, int amount) {
        try {
            String setClause = column + " = " + column + " + ?";
            List<Object> setValues = List.of(amount);

            String whereClause = IDFields().stream().map(ID -> ID + " = ?").collect(Collectors.joining(" AND "));
            List<Object> whereValues = new ArrayList<>();
            for (String ID : IDFields()) whereValues.add(cachedFields.stream().filter(f -> f.getName().equalsIgnoreCase(ID)).findFirst().orElseThrow().get(this));

            List<Object> finalValues = new ArrayList<>();
            finalValues.addAll(setValues);
            finalValues.addAll(whereValues);

            String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + whereClause;
            SQLCleaner C = new SQLCleaner(sql, finalValues);
            return jdbcTemplate.update(C.newSQL, C.newParams);
        } catch (Exception e) {
            throw new RuntimeException("No ID field found in " + tableName + ".");
        }
    }
    public int IncrementColumns(Map<String, Object> parameters) {
        try {
            String setClause = parameters.entrySet().stream().map(f -> f.getKey() + " = " + f.getKey() + " + ?").collect(Collectors.joining(", "));
            List<Object> setValues = parameters.entrySet().stream().map(f -> f.getValue()).collect(Collectors.toList());

            String whereClause = IDFields().stream().map(ID -> ID + " = ?").collect(Collectors.joining(" AND "));
            List<Object> whereValues = new ArrayList<>();
            for (String ID : IDFields()) whereValues.add(cachedFields.stream().filter(f -> f.getName().equalsIgnoreCase(ID)).findFirst().orElseThrow().get(this));

            List<Object> finalValues = new ArrayList<>();
            finalValues.addAll(setValues);
            finalValues.addAll(whereValues);

            String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + whereClause;
            SQLCleaner C = new SQLCleaner(sql, finalValues);
            return jdbcTemplate.update(C.newSQL, C.newParams);
        } catch (Exception e) {
            throw new RuntimeException("No ID field found in " + tableName + ".");
        }
    }

    private record Result(String columns, String placeholders, Object[] values, String updateClause) {}
    private Result getResult(boolean update) {
        List<Field> nonNullFields = cachedFields.stream().filter(f -> {
            try {
                return f.get(this) != null;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        String columns = nonNullFields.stream().map(Field::getName).collect(Collectors.joining(", "));
        String placeholders = nonNullFields.stream().map(p -> "?").collect(Collectors.joining(", "));

        List<Object> values = nonNullFields.stream().map(f -> {
            try { return f.get(this); }
            catch (IllegalAccessException e) { throw new RuntimeException(e); }
        }).toList();

        if (!update) new Result(columns, placeholders, values.toArray(), null);
        String updateClause = cachedFields.stream()
                .map(f -> f.getName() + " = VALUES(" + f.getName() + ")")
                .collect(Collectors.joining(", "));
        return new Result(columns, placeholders, values.toArray(), updateClause);
    }

    public static <T> Optional<T> getById(Class<T> clazz, Object id) {
        return getWhere(clazz, "ID = ?", id);
    }
    public static <T> Optional<T> getWhere(Class<T> clazz, String whereClause, Object... args) {
        try {
            String sql = "SELECT * FROM " + getTableName(clazz) + " WHERE " + whereClause + " LIMIT 1;";
            SQLCleaner C = new SQLCleaner(sql, args);
            return Optional.ofNullable(jdbcTemplate.queryForObject(C.newSQL, (rs, rowNum) -> mapResultSetToObject(rs, clazz), C.newParams));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public static <T> List<T> getAll(Class<T> clazz) {
        String sql = "SELECT * FROM " + getTableName(clazz);
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapResultSetToObject(rs, clazz));
    }
    public static <T> List<T> getAllWhere(Class<T> clazz, String whereClause, Object... args) {
        String sql = "SELECT * FROM " + getTableName(clazz) + " WHERE " + whereClause;
        SQLCleaner C = new SQLCleaner(sql, args);
        return jdbcTemplate.query(C.newSQL, (rs, rowNum) -> mapResultSetToObject(rs, clazz), C.newParams);
    }

    public static int doUpdate(String sql, Object... args) {
        SQLCleaner C = new SQLCleaner(sql, args);
        return jdbcTemplate.update(C.newSQL, C.newParams);
    }

    public static <T> Optional<T> doQueryValue(Class<T> clazz, String sql, Object... args) {
        try {
            SQLCleaner C = new SQLCleaner(sql, args);
            return Optional.ofNullable(jdbcTemplate.queryForObject(C.newSQL, clazz, C.newParams));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }


    public static Optional<DatabaseObject.Row> doQuery(String sql, Object... args) {
        try {
            SQLCleaner C = new SQLCleaner(sql, args);
            return Optional.ofNullable(new Row(jdbcTemplate.queryForMap(C.newSQL, C.newParams)));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
    public static <T> Optional<T> doQuery(Class<T> clazz, String sql, Object... args) {
        try {
            SQLCleaner C = new SQLCleaner(sql, args);
            return Optional.ofNullable(jdbcTemplate.queryForObject(C.newSQL, (rs, rowNum) -> mapResultSetToObject(rs, clazz), C.newParams));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
    public static List<Row> doQueryAll(String sql, Object... args) {
        SQLCleaner C = new SQLCleaner(sql, args);
        return jdbcTemplate.queryForList(C.newSQL, C.newParams).stream().map(DatabaseObject.Row::new).collect(Collectors.toList());
    }
    public static <T> List<T> doQueryAll(Class<T> clazz, String sql, Object... args) {
        SQLCleaner C = new SQLCleaner(sql, args);
        return jdbcTemplate.query(C.newSQL, (rs, rowNum) -> mapResultSetToObject(rs, clazz), C.newParams);
    }

    public String toJSON() {
        return GSON.toJson(this);
    }

    private static String getTableName(Class<?> clazz) {
        TableName annotation = clazz.getAnnotation(TableName.class);
        if (annotation != null) return annotation.value().toLowerCase();
        return clazz.getSimpleName().toLowerCase();
    }

    public static class Row {
        public transient Map<String, Object> rows;

        public Row(Map<String, Object> qp) {
            this.rows = qp;
        }

        public <T> T get(Class<T> clazz, String fieldName) {
            return clazz.cast(get(fieldName));
        }

        public Object get(String fieldName) {
            return rows.get(fieldName);
        }

        public String getAsString(String fieldName) {
            try {
                return get(fieldName).toString();
            } catch (Exception ignored) {
                return null;
            }
        }

        public int getAsInt(String fieldName) {
            try {
                return Integer.parseInt(getAsString(fieldName));
            } catch (Exception ignored) {
                return 0;
            }
        }

        public long getAsLong(String fieldName) {
            try {
                return Long.parseLong(getAsString(fieldName));
            } catch (Exception ignored) {
                return 0;
            }
        }

        public double getAsDouble(String fieldName) {
            try {
                return Double.parseDouble(getAsString(fieldName));
            } catch (Exception ignored) {
                return 0;
            }
        }

        public short getAsShort(String fieldName) {
            try {
                return get(fieldName) == null ? 0 : Short.parseShort(getAsString(fieldName));
            } catch (Exception ignored) {
                return 0;
            }
        }

        public float getAsFloat(String fieldName) {
            try {
                return Float.parseFloat(getAsString(fieldName));
            } catch (Exception ignored) {
                return 0;
            }
        }

        public boolean getAsBoolean(String fieldName) {
            try {
                return Boolean.parseBoolean(getAsString(fieldName));
            } catch (Exception ignored) {
                return false;
            }
        }

        public byte getAsByte(String fieldName) {
            return Byte.parseByte(getAsString(fieldName));
        }
    }

    public static class SQLCleaner {
        public String newSQL;
        public Object[] newParams;

        public SQLCleaner(String sql, List<Object> params) {
            fixNullParams(sql, params);
        }
        public SQLCleaner(String sql, Object[] params) {
            fixNullParams(sql, Arrays.asList(params));
        }

        public void fixNullParams(String sql, List<Object> params) {
            sql = sql.replaceAll("\\s*=\\s*\\?", "=?");

            StringBuilder newSql = new StringBuilder();
            List<Object> newParams = new ArrayList<>();

            int paramIndex = 0;
            int pos = 0;

            int wherePos = -1;
            {
                String upperSql = sql.toUpperCase();
                wherePos = upperSql.indexOf("WHERE ");
                if (wherePos == -1)
                    wherePos = upperSql.indexOf("WHERE\n");
                if (wherePos == -1)
                    wherePos = upperSql.indexOf("WHERE\t");
                if (wherePos == -1)
                    wherePos = upperSql.indexOf("WHERE"); // fallback
            }

            while (pos < sql.length()) {
                int qIndex = sql.indexOf("?", pos);
                if (qIndex == -1 || paramIndex >= params.size()) {
                    newSql.append(sql.substring(pos));
                    break;
                }

                newSql.append(sql, pos, qIndex);
                Object value = params.get(paramIndex);
                boolean isEqualsParam = false;

                // Only check for "=" before the "?" and if we are after WHERE
                int check = qIndex - 1;
                while (check >= 0 && Character.isWhitespace(sql.charAt(check))) check--;
                if (check >= 0 && sql.charAt(check) == '=') {
                    isEqualsParam = (qIndex > wherePos && wherePos != -1);
                }

                if (isEqualsParam && value == null) {
                    // Replace "= ?" with "IS NULL"
                    int lastEq = newSql.lastIndexOf("=");
                    if (lastEq != -1) newSql.deleteCharAt(lastEq);
                    newSql.append(" IS NULL");
                } else {
                    newSql.append("?");
                    newParams.add(value);
                }

                pos = qIndex + 1;
                paramIndex++;
            }

            this.newSQL = newSql.toString();
            this.newParams = newParams.toArray();
        }
    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TableName {
        String value();
    }
}