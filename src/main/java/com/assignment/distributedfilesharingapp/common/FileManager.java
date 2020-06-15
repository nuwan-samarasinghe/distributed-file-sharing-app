package com.assignment.distributedfilesharingapp.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class FileManager {

    private final Map<String, String> files = new HashMap<>();
    private final String rootFolder;
    private final String fileSeparator = "/";

    @Value("${app.common.file-name}")
    private String fileName;

    public FileManager(String userName) {
        this.rootFolder = "." + fileSeparator + userName;
        ArrayList<String> fullList = readFileNamesFromResources();
        Random random = new Random();
        IntStream.range(0, 5).forEach(i -> files.put(fullList.get(random.nextInt(fullList.size())), ""));
        printFileNames();
    }

    public boolean addFile(String fileName, String filePath) {
        this.files.put(fileName, filePath);
        return true;
    }

    public Set<String> searchForFile(String query) {
        String[] querySplit = query.split(" ");
        Set<String> result = new HashSet<>();
        Arrays.stream(querySplit)
                .forEach(q ->
                        this.files.keySet().forEach(key -> {
                            String[] fileNameSplit = key.split(" ");
                            Arrays.stream(fileNameSplit)
                                    .filter(f -> f.toLowerCase()
                                            .equals(q.toLowerCase()))
                                    .map(f -> key).forEach(result::add);
                        }));
        return result;
    }

    private ArrayList<String> readFileNamesFromResources() {
        ArrayList<String> fileNames = new ArrayList<>();
        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader
                (Objects.requireNonNull(classLoader.getResourceAsStream(fileName))));
        try {
            fileNames = bufferedReader.lines().collect(Collectors.toCollection(ArrayList::new));
            bufferedReader.close();
        } catch (IOException e) {
            log.error("an error occurred while read file name", e);
        }
        return fileNames;
    }

    private void printFileNames() {
        files.keySet().forEach(s -> {
            log.info("initiate create file for {}", s);
            createFile(s);
        });
    }

    public String getFileNames() {
        return files.keySet()
                .stream()
                .map(s -> s + "\n")
                .collect(Collectors.joining("", "Total files: " + files.size() + "\n" + "++++++++++++++++++++++++++\n", ""));
    }

    public void createFile(String fileName) {
        try {
            String absoluteFilePath = this.rootFolder + fileSeparator + fileName;
            File file = new File(absoluteFilePath);
            file.getParentFile().mkdir();
            if (file.createNewFile()) {
                log.info(absoluteFilePath + " File Created");
            } else log.info("File " + absoluteFilePath + " already exists");
            RandomAccessFile f = new RandomAccessFile(file, "rw");
            f.setLength(1024 * 1024 * 8);
        } catch (IOException e) {
            log.error("File creating failed", e);
        }
    }

    public File getFile(String fileName) {
        return new File(rootFolder + fileSeparator + fileName);
    }
}
