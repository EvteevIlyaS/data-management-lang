package com.digdes.school;


import static com.digdes.school.Tools.printTable;

public class App {
    public static void main(String[] args) {
        JavaSchoolStarter starter = new JavaSchoolStarter();
        try {
            starter.execute("INSERT VALUES 'lastName' = 'Петров' , 'id'=1, 'age'=30, 'active'=true, 'cost'=5.4").forEach(System.out::println);

            starter.execute("INSERT VALUES 'lastName' = 'Иванов' , 'id'=2, 'age'=25, 'active'=false, 'cost'=4.3").forEach(System.out::println);

            starter.execute("INSERT VALUES 'lastName' = 'Федоров' , 'id'=3, 'age'=40, 'active'=true").forEach(System.out::println);


            // Checker
            printTable(starter.execute("SELECT"));


            starter.execute("UPDATE VALUES 'active'=false, 'cost'=10.1 where 'id'=3").forEach(System.out::println);


            // Checker
            printTable(starter.execute("SELECT"));


            starter.execute("UPDATE VALUES 'active'=true  where 'active'=false").forEach(System.out::println);


            // Checker
            printTable(starter.execute("SELECT"));


            starter.execute("SELECT WHERE 'age'>=30 and 'lastName' ilike '%п%'").forEach(System.out::println);


            // Checker
            printTable(starter.execute("SELECT WHERE 'age'>=30 and 'lastName' ilike '%п%'"));


            starter.execute("DELETE WHERE 'id'=3").forEach(System.out::println);


            // Checker
            printTable(starter.execute("SELECT"));


            starter.execute("SELECT WHERE 'age'>=30 and 'lastName' ilike '%п%'").forEach(System.out::println);


            // Checker
            printTable(starter.execute("SELECT"));


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
