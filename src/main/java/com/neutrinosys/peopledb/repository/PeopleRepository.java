package com.neutrinosys.peopledb.repository;

import com.neutrinosys.peopledb.annotation.SQL;
import com.neutrinosys.peopledb.model.CrudOperation;
import com.neutrinosys.peopledb.model.Person;
import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PeopleRepository extends CRUDRepository<Person> {

    public static final String SAVE_PERSON_SQL = "INSERT INTO PEOPLE (FIRST_NAME, LAST_NAME, DOB) VALUES(?,?,?)";
    public static final String SELECT_COUNT_SQL = "SELECT COUNT(*) FROM PEOPLE";
    public static final String DELETE_SQL = "DELETE FROM PEOPLE WHERE ID=?";
    public static final String DELETE_IN_SQL = "DELETE FROM PEOPLE WHERE ID IN (:ids)";
    public static final String UPDATE_SQL = "UPDATE PEOPLE SET FIRST_NAME=?, LAST_NAME=?, DOB=?, SALARY=? WHERE ID=?";
    public static final String FIND_BY_ID_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB, SALARY FROM PEOPLE WHERE ID=?";
    public static final String FIND_ALL_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB, SALARY FROM PEOPLE";

    public PeopleRepository(Connection connection) {
        super(connection);
    }

    @Override
    @SQL(value=UPDATE_SQL, operationType= CrudOperation.UPDATE)
    void mapForUpdate(Person entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.getFirstName());
        ps.setString(2, entity.getLastName());
        ps.setTimestamp(3, convertDobToTimeStamp(entity.getDob()));
        ps.setBigDecimal(4, entity.getSalary());
    }

    @Override
    @SQL(value=SAVE_PERSON_SQL, operationType=CrudOperation.SAVE)
    void mapForSave(Person entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.getFirstName());
        ps.setString(2, entity.getLastName());
        ps.setTimestamp(3, convertDobToTimeStamp(entity.getDob()));
    }

    @Override
    @SQL(value = FIND_BY_ID_SQL, operationType = CrudOperation.FIND_BY_ID)
    @SQL(value = FIND_ALL_SQL, operationType = CrudOperation.FIND_ALL)
    @SQL(value = SELECT_COUNT_SQL, operationType = CrudOperation.COUNT)
    @SQL(value = DELETE_SQL, operationType = CrudOperation.DELETE_ONE)
    Person extractEntityFromResultSet(ResultSet rs) throws SQLException{
        long personId = rs.getLong("ID");
        String firstName = rs.getString("FIRST_NAME");
        String lastName = rs.getString("LAST_NAME");
        ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
        BigDecimal salary = rs.getBigDecimal("SALARY");
        return new Person(personId, firstName, lastName, dob, salary);
    }

//    @Override
//    protected String getFindByIdSql() {
//        return FIND_BY_ID_SQL;
//    }
//
//    @Override
//    protected String getFindAllSql() {
//        return FIND_ALL_SQL;
//    }
//
//    @Override
//    protected String getCountSql() {
//        return SELECT_COUNT_SQL;
//    }
//
//    @Override
//    protected String getDeleteInSql() {
//        return DELETE_IN_SQL;
//    }
//
//    @Override
//    protected String getDeleteSql() {
//        return DELETE_SQL;
//    }

    private static Timestamp convertDobToTimeStamp(ZonedDateTime dob) {
        return Timestamp.valueOf(dob.withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime());
    }
}