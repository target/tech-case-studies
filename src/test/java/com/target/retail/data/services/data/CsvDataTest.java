package com.target.retail.data.services.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CsvDataTest {

    private CsvData<Employee> testInstance;

    private static final String testFileName = "test_employee.csv";
    @BeforeEach
    void setUp() throws Exception {
        writeTestData(testFileName);
        testInstance = new CsvData<>(testFileName, Employee.class);
    }

    @AfterEach
    void tearDown() throws IOException {
        clearTestData(testFileName);
    }

    @Test
    void testGetByIdWhenPresent() {
        Optional<Employee> emp = testInstance.getById("Z007");
        assertTrue(emp.isPresent());
        assertEquals(Optional.of(new Employee("Z007", "product", "ZeeSeven", 5, 1)), emp, "Unexpected employee found "+emp);
    }

    @Test
    void testGetByIdWhenAbsent() {
        assertTrue(testInstance.getById("ZZZ").isEmpty());
    }

    @Test
    void testGetAllBySize() {
        assertEquals(7, testInstance.getAll().size(), "Unexpected size returned by getAll()" + testInstance.getAll());
    }

    @Test
    void testSearch() {
        List<Employee> searchResults = testInstance.search((Employee e) -> e.getTenure() > 2);
        assertEquals(2, searchResults.size(), "Unexpected number of results in search " + searchResults);
    }

    @Test
    void testGetCount() {
        assertEquals(7, testInstance.getCount(), "Unexpected count returned by getCount()"+testInstance.getCount());
    }

    @Test
    void testMapByKeys() {
        Map<String, List<Employee>> mappedByDept = testInstance.mapUsingKey(Employee::getDepartment);
        assertEquals(4, mappedByDept.get("tech").size(), "unexpected number of employees in tech");
    }

    @Test
    void testGetAllResultIsImmutable() {
        List<Employee> csvData  = testInstance.getAll();
        assertThrows(UnsupportedOperationException.class, () -> csvData.remove(0), "Expected UnsupportedOperationException");
    }

    private void writeTestData(String fileName) throws IOException {
        File testFile = new File(fileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(testFile));
        try (writer) {
            writer.write("employeeId,department,name,grade,tenure\n");
            writer.write("Z001,tech,ZeeOne,4,2\n");
            writer.write("Z002,tech,ZeeTwo,4,1\n");
            writer.write("Z003,tech,ZeeThree,5,3\n");
            writer.write("Z004,tech,ZeeFour,6,5\n");
            writer.write("Z005,product,ZeeFive,6,1\n");
            writer.write("Z006,product,ZeeSix,5,2\n");
            writer.write("Z007,product,ZeeSeven,5,1\n");
            writer.flush();
        }
    }

    private void clearTestData(String fileName)  throws IOException {
        File testFile = new File(fileName);
        if(!testFile.delete()) {
            throw new IOException(fileName+" could not be deleted");
        }
    }

}


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({ "employeeId", "department", "name", "grade", "tenure" })
class Employee implements  Identifiable {

    private String employeeId;

    private String department;

    private String name;

    private int grade;
    private int tenure;

    public Employee() {}

    public Employee(String employeeId, String department, String name, int grade, int tenure) {
        this.employeeId = employeeId;
        this.department = department;
        this.name = name;
        this.grade = grade;
        this.tenure = tenure;
    }
    public String getEmployeeId() {
        return employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public String getName() {
        return name;
    }

    public int getGrade() {
        return grade;
    }

    public int getTenure() {
        return tenure;
    }

    public String getId() {
        return getEmployeeId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return grade == employee.grade &&
                tenure == employee.tenure &&
                Objects.equals(employeeId, employee.employeeId) &&
                Objects.equals(department, employee.department) &&
                Objects.equals(name, employee.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, department, name, grade, tenure);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId='" + employeeId + '\'' +
                ", department='" + department + '\'' +
                ", name='" + name + '\'' +
                ", grade=" + grade +
                ", tenure=" + tenure +
                '}';
    }

}