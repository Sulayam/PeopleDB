package com.neutrinosys.peopledb.repository;

import com.neutrinosys.peopledb.annotation.MultiSQL;
import com.neutrinosys.peopledb.annotation.SQL;
import com.neutrinosys.peopledb.exception.UnableToSaveException;
import com.neutrinosys.peopledb.model.CrudOperation;
import com.neutrinosys.peopledb.model.Entity;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import static java.util.stream.Collectors.joining;

abstract class CRUDRepository<T extends Entity> {

    protected Connection connection;

    public CRUDRepository(Connection connection) {
        this.connection = connection;
    }

    public T save(T entity) throws UnableToSaveException {
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.SAVE, this::getSaveSql), Statement.RETURN_GENERATED_KEYS);
            mapForSave(entity, ps);
            int recordsAffected = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while(rs.next()){
                long id = rs.getLong(1);
                entity.setId(id);
                System.out.println(entity);
            }
            System.out.printf("Records affected: %d%n", recordsAffected);
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new UnableToSaveException("Tried to save entity: " + entity);
        }
        return entity;
    }

    public Optional<T> findById(Long id) {
        T entity = null;
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.FIND_BY_ID, this::getFindByIdSql));
            ps.setLong(1,id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entity = extractEntityFromResultSet(rs);
            }
        }  catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println(entity);
        return Optional.ofNullable(entity);
    }

    public List<T> findAll() throws SQLException {
        List<T> entities = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.FIND_ALL, this::getFindAllSql));
            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                entities.add(extractEntityFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entities;
    }

    public long count() {
        long count=0;
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.COUNT,this::getCountSql));
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                count= rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(count);
        return count;
    }

    public void delete(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.DELETE_ONE,this::getDeleteSql));
            ps.setLong(1,entity.getId());
            int affectedRecordCount = ps.executeUpdate();
            System.out.println(affectedRecordCount);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(T...entities) {
        try {
            Statement stmt = connection.createStatement();
            String ids = Arrays.stream(entities).map(T::getId).map(String::valueOf).collect(joining(","));
            int affectedRecordsCount = stmt.executeUpdate(getSqlByAnnotation(CrudOperation.DELETE_MANY,this::getDeleteInSql).replace(":ids", ids));
            System.out.println(affectedRecordsCount);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getSqlByAnnotation(CrudOperation operationType, Supplier<String> sqlGetter) {
        Stream<SQL> multiSqlStream = Arrays.stream(this.getClass().getMethods())
                .filter(m-> m.isAnnotationPresent(MultiSQL.class))
                .map(m -> m.getAnnotation(MultiSQL.class))
                .flatMap(msql -> Arrays.stream(msql.value()));

        Stream<SQL> sqlStream = Arrays.stream(this.getClass().getMethods())
                .filter(m-> m.isAnnotationPresent(SQL.class))
                .map(m -> m.getAnnotation(SQL.class));

        return Stream.concat(sqlStream, multiSqlStream)
                .filter(a->a.operationType().equals(operationType))
                .map(SQL::value)
                .findFirst().orElseGet(sqlGetter);
    }

    /**
     *  @return Should return a SQL string like:
     *  "DELETE FROM PEOPLE WHERE ID IN (:ids)"
     *  Be sure to include the '(:ids)' named parameter and call it 'ids'
     */

    public void update(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.UPDATE, this::getUpdateSql));
            mapForUpdate(entity, ps);
            ps.setLong(5,entity.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    protected String getFindAllSql(){ throw new RuntimeException("sql not defined.");}
    protected String getDeleteInSql(){ throw new RuntimeException("sql not defined.");}
    protected String getDeleteSql(){ throw new RuntimeException("sql not defined.");}
    protected  String getUpdateSql(){ throw new RuntimeException("sql not defined.");}
    protected String getSaveSql(){ throw new RuntimeException("sql not defined.");}
    protected String getCountSql(){ throw new RuntimeException("sql not defined.");}

    /**
     * @return returns a string that represents the SQL needed to retrieve one entity
     *The sql must contain one sql parameter (ie, a "?") that binds to the entity's ID
     */
    protected String getFindByIdSql(){ throw new RuntimeException("sql not defined.");}

    abstract T extractEntityFromResultSet(ResultSet rs) throws SQLException;
    abstract void mapForUpdate(T entity, PreparedStatement ps) throws SQLException;
    abstract void mapForSave(T entity, PreparedStatement ps) throws SQLException;
}