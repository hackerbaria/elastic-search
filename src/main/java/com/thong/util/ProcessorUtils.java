package com.thong.util;

import com.thong.model.Student;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessorUtils {

    public static void main(String[] args) {
        /*List<String> myList =
                Arrays.asList("a1", "a2", "b1", "c2", "c1");

        myList.stream()
                .filter(s -> s.startsWith("c"))
                .map(String::toUpperCase)
                .sorted()
                .forEach(System.out::println);*/


        /*Runnable r = new Runnable() {
            @Override
            public void run() {
                System.out.println("hello");
            }
        };
        r.run();

        // change to lambda
        Runnable t = () -> System.out.println("hello");
        t.run();
*/

        Student obj1 = new Student();
        obj1.setName("mkyong");
        obj1.addBook("Java 8 in Action");
        obj1.addBook("Spring Boot in Action");
        obj1.addBook("Effective Java (2nd Edition)");

        Student obj2 = new Student();
        obj2.setName("zilap");
        obj2.addBook("Learning Python, 5th Edition");
        obj2.addBook("Effective Java (2nd Edition)");

        List<Student> list = new ArrayList<>();
        list.add(obj1);
        list.add(obj2);



        List<String> books = list.stream()
                .map(x -> x.getBook())
                .flatMap(x -> x.stream())
                .distinct()
                .collect(Collectors.toList());
        books.forEach(System.out::println);



    }


    public static <T> Set<T> findDuplicateBySetAdd(List<T> list) {
        Set<T> items = new HashSet<>();
        return list.stream()
                .filter(n -> Integer.valueOf((Integer) n) % 2 ==0).collect(Collectors.toSet());
    }
}
