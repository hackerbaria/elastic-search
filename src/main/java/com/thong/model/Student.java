package com.thong.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Student {

    private String name;
    private Set<String> book;

    public void addBook(String book) {
        if (this.book == null) {
            this.book = new HashSet<>();
        }
        this.book.add(book);
    }
}
