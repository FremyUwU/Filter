package ru.shift;



import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(
        name = "FilterUtil",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Фильтрует строки по типу: строки, целые числа, вещественные."
)
public class FilterUtil implements Callable<Integer> {

    @Option(names = {"-o"}, description = "Путь для результатов фильтрации.")
    private final Path outputDir = Paths.get(".");

    @Option(names = {"-p"}, description = "Задать префикс для имен выходных файлов.")
    private String prefix = "";

    @Option(names = {"-a"}, description = "Задать режим добавления в существующие файлы.")
    private  boolean overwrite = false;

    @Option(names = {"-s"}, description = "Показать краткую статистику.")
    private boolean shortStats = false;

    @Option(names = {"-f"}, description = "Показать полную статистику.")
    private  boolean fullStats = false;

    @Parameters(arity = "1..*", paramLabel = "<files>", description = "Входные файлы")
    private List<Path> inputFiles;

    @Override
    public Integer call() throws Exception {
        if(!shortStats && !fullStats) shortStats = true;

        IntStats intStats = new IntStats();
        FloatStats floatStats = new FloatStats();
        StringStats stringStats = new StringStats();

        List<String> intLines = new ArrayList<>();
        List<String> floatLines = new ArrayList<>();
        List<String> stringLines = new ArrayList<>();

        for(Path p : inputFiles) {

            if (!Files.exists(p)){ System.err.println("Файл не найден" + p);
            continue;
        }

            try(BufferedReader reader = Files.newBufferedReader(p)){
                String line;
                while ((line = reader.readLine()) !=null){
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        // пустую строку считаем строкой
                        stringLines.add(line);
                        stringStats.accept(line);
                        continue;
                    }

                    boolean handled = false;

                    try{
                        BigInteger b = new BigInteger(trimmed);
                        intLines.add(trimmed);
                        intStats.accept(b);
                        handled = true;
                    } catch (NumberFormatException e) {
                        //не целое
                    }
                    if(handled) continue;

                    try{
                        double d = Double.parseDouble(trimmed);
                        floatLines.add(trimmed);
                        floatStats.accept(d);
                        handled = true;
                    } catch (NumberFormatException e){
                        //не вещественное
                    }
                    if(handled) continue;

                    stringLines.add(line);
                    stringStats.accept(line);
                }
            } catch (IOException e){
                System.err.println("Ошибка чтения файла " + p + ": " + e.getMessage());
            }
        }
        //Создание директории результатов при отсутствии
        try {
            if(!Files.exists(outputDir)){
                Files.createDirectory(outputDir);
            }
        } catch (IOException e) {
            System.err.println("Не удалось создать каталог результатов: " + e.getMessage());
            return 1;
        }

        writeLinesIfNeeded(intLines, outputDir, prefix + "integers.txt", overwrite);
        writeLinesIfNeeded(floatLines, outputDir, prefix + "floats.txt", overwrite);
        writeLinesIfNeeded(stringLines, outputDir, prefix + "strings.txt", overwrite);

        // Вывод статистики
        System.out.println("Статистика:");

        System.out.println("\nIntegers:");
        printIntStats(intStats);

        System.out.println("\nFloats:");
        printFloatStats(floatStats);

        System.out.println("\nStrings:");
        printStringStats(stringStats);

        return 0;
    }
    // ----- helper methods -----
    private static void writeLinesIfNeeded(List<String> lines, Path outDir, String fileName, boolean appendMode) {
        if (lines.isEmpty()) return;
        Path outFile = outDir.resolve(fileName);
        Set<OpenOption> options = new HashSet<>();
        options.add(StandardOpenOption.CREATE);
        if (appendMode) {
            options.add(StandardOpenOption.APPEND);
        } else {
            options.add(StandardOpenOption.TRUNCATE_EXISTING);
        }
        options.add(StandardOpenOption.WRITE);

        try (BufferedWriter writer = Files.newBufferedWriter(outFile,
                options.toArray(new OpenOption[0]))) {
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Ошибка записи в файл " + outFile + ": " + e.getMessage());
        }
    }

    private void printIntStats(IntStats s) {
        if (s.count == 0) {
            System.out.println("  нет элементов");
            return;
        }
        System.out.println("  count = " + s.count);
        if (fullStats) {
            System.out.println("  min   = " + s.min);
            System.out.println("  max   = " + s.max);
            System.out.println("  sum   = " + s.sum);
            double avg = s.sum.doubleValue() / (double) s.count;
            System.out.println("  avg   = " + avg);
        }
    }

    private void printFloatStats(FloatStats s) {
        if (s.count == 0) {
            System.out.println("  нет элементов");
            return;
        }
        System.out.println("  count = " + s.count);
        if (fullStats) {
            System.out.println("  min   = " + s.min);
            System.out.println("  max   = " + s.max);
            System.out.println("  sum   = " + s.sum);
            double avg = s.sum / (double) s.count;
            System.out.println("  avg   = " + avg);
        }
    }

    private void printStringStats(StringStats s) {
        if (s.count == 0) {
            System.out.println("  нет элементов");
            return;
        }
        System.out.println("  count = " + s.count);
        if (fullStats) {
            System.out.println("  min length = " + s.min);
            System.out.println("  max length = " + s.max);
        }
    }
}
