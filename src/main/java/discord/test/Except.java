package discord.test;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Except {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        Except except = new Except();
        int currentIndex = 2;
        
        for (int i = 0; i < 50; i++) list.add(i);

        System.out.println("\nqueue: ");
        System.out.println(except.queue(list, currentIndex + 1, currentIndex + 10, false));

        System.out.println("\nhistory: ");
        System.out.println(except.queue(list, currentIndex - 10, currentIndex, true));
    }

    public String queue(List<Integer> list, int start, int end, boolean reverse) {
        return list.stream()
                .filter(s -> list.indexOf(s) >= start && list.indexOf(s) < end)
                .sorted (reverse ? Comparator.reverseOrder() : Comparator.naturalOrder())
                .map(e -> String.format("%s. %s", list.indexOf(e) + 1, e))
                .collect(Collectors.joining("\n"));
    }
    
}

