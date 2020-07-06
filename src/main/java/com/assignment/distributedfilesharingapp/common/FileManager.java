package com.assignment.distributedfilesharingapp.common;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class FileManager {

    private static FileManager FILE_MANAGER;

    private final Map<String, String> files = new HashMap<>();
    private final String rootFolder;

    private String fileName;

    public static FileManager getInstance(String userName, String fileName) {
        if (Objects.isNull(FILE_MANAGER)) {
            synchronized (FileManager.class) {
                if (Objects.isNull(FILE_MANAGER)) {
                    FILE_MANAGER = new FileManager(userName, fileName);
                }
                return FILE_MANAGER;
            }
        } else {
            return FILE_MANAGER;
        }
    }

    private FileManager(String userName, String fileName) {
        this.fileName = fileName;
        this.rootFolder = "." + "/" + userName;
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

    public List<String> getFileNamesList() {
        return new ArrayList<>(files.keySet());
    }

    public void createFile(String fileName) {
        try {
            String absoluteFilePath = this.rootFolder + "/" + fileName;
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
        return new File(rootFolder + "/" + fileName);
    }
}
