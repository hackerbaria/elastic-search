package com.thong.model;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * convert List to Map
 */
public class Java8Function3 {

    public static void main(String[] args) {
        Java8Function3 obj = new Java8Function3();

        List<String> list = Arrays.asList("node", "c++", "java", "javascript");

        /*Map<String, Integer> maps = obj.convertListToMap(list, x -> x.length());
        System.out.println(maps);*/

        /*List<String> list2 = obj.map(list, x -> obj.sha256(x));
        System.out.println(list2);*/

        /*List<String> notJavaList = list.stream()
                .filter(x -> !"java".equalsIgnoreCase(x))
                .collect(Collectors.toList());
        System.out.println(notJavaList);*/

        /*String[][] data = new String[][]{{"a", "c"}, {"c", "d"}, {"thong", "ga", "ga"}};
        Stream<String[]> temp = Arrays.stream(data);
        Set<String> stringList = temp.flatMap(x -> Arrays.stream(x)).collect(Collectors.toSet());
        System.out.println(stringList);*/

        // stream.iterate example
        // we use iterate to create stream values on demand, infinite stream
        Stream.iterate(0, n -> n+1).limit(10).collect(Collectors.toList());


        //3 apple, 2 banana, others 1
        List<String> items =
                Arrays.asList("apple", "apple", "banana",
                        "apple", "orange", "banana", "papaya");
        Map<String, Long> result = items.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        //System.out.println(result);
        Map<String, Long> finalMap = new LinkedHashMap<>();

        //Sort a map and add to finalMap
        result.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()).forEachOrdered(e -> finalMap.put(e.getKey(), e.getValue()));

        System.out.println(finalMap);


    }

    public <T, R> Map<T, R> convertListToMap(List<T> list, Function<T, R> func) {
        Map<T, R> result = new HashMap<>();
        for (T t : list) {
            result.put(t, func.apply(t));
        }
        return result;
    }

    public <T, R> List<R> map(List<T> list, Function<T, R> function) {
        List<R> result = new ArrayList<>();
        for (T t : list) {
            result.add(function.apply(t));
        }
        return result;
    }

    // sha256 a string
    public String sha256(String str) {
        return DigestUtils.sha256Hex(str);
    }
}
