package com.neutrinosys.peopledb.repository;
import com.neutrinosys.peopledb.model.Person;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

public class PeopleRepositoryTests {

    private Connection connection;
    private PeopleRepository repo;

    @BeforeEach
    void setUp() throws SQLException {
//        connection = DriverManager.getConnection("jdbc:h2:~\\peopletest".replace("~", System.getProperty("user.home")));
        connection = DriverManager.getConnection("jdbc:h2:D:\\DBeaver\\peopletest");
        connection.setAutoCommit(false);
        repo = new PeopleRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void canSaveOnePerson() throws SQLException {
        Person john  = new Person( "John", "Smith", ZonedDateTime.of(1980,11,12,15,15,0,0, ZoneId.of("-6")));
        Person savedPerson = repo.save(john);
        assertThat(savedPerson.getId()).isGreaterThan(0);
    }

    @Test
    public void canSaveTwoPeople(){
        Person john = new Person( "John", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6")));
        Person bobby = new Person( "Bobby", "Smith", ZonedDateTime.of(1982,12,10,13,15,0,0, ZoneId.of("-8")));
        Person savedPerson1 = repo.save(john);
        Person savedPerson2 = repo.save(bobby);
        assertThat(savedPerson1.getId()).isNotEqualTo(savedPerson2.getId());
    }

    @Test
    public void canFindPersonById(){
        Person savedPerson = repo.save(new Person( "test", "jackson", ZonedDateTime.now()));
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(foundPerson).isNotEqualTo(savedPerson);
    }

    @Test
    public void testPersonIdNotFound(){
        Optional<Person> foundPerson = repo.findById(-1L);
        assertThat(foundPerson).isEmpty();
    }

    @Test
    public void canGetCount(){
        long startCount = repo.count();
        repo.save(new Person( "John", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        repo.save(new Person( "John1", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount+2);
    }
    
    @Test
    public void canDelete(){
        Person savedPerson = repo.save(new Person("John1", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        long startCount = repo.count();
        repo.delete(savedPerson);
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount-1);
    }

    @Test
    public void canDeleteMultiplePeople() {
        Person p1=repo.save(new Person( "John", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        Person p2=repo.save(new Person( "John1", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        long startCount = repo.count();
        repo.delete(p1,p2);
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount-2);
    }

    @Test
    public void findAll() throws SQLException {
        repo.save(new Person( "John", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        repo.save(new Person( "John1", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        repo.save(new Person( "John2", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        repo.save(new Person( "John3", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        repo.save(new Person( "John4", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        repo.save(new Person( "John5", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        repo.save(new Person( "John6", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        repo.save(new Person( "John7", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        repo.save(new Person( "John8", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));

        List<Person> people = repo.findAll();
        assertThat(people.size()).isGreaterThanOrEqualTo(10);
    }

    @Test
    public void canUpdate(){
        Person savedPerson = repo.save(new Person( "John1", "Smith", ZonedDateTime.of(1980,11,11,11,15,0,0, ZoneId.of("-6"))));
        Person p1 = repo.findById(savedPerson.getId()).get();
        savedPerson.setSalary(new BigDecimal("73000.23"));
        repo.update(savedPerson);
        Person p2 = repo.findById(savedPerson.getId()).get();
        assertThat(p2.getSalary()).isNotEqualTo(p1.getSalary());
    }
}