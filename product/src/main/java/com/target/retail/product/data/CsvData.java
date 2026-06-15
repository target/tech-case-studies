package com.target.retail.product.data;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CsvData<T extends Identifiable> {

    private final File file;
    private final Class<T> type;
    private final List<T> data;
    private final Map<String, T> dataByIdentifier;

    public CsvData(String fileName, Class<T> type) {
        this.file = new File(fileName);
        this.type = type;
        this.data = loadDataFromFile();
        this.dataByIdentifier = Collections.unmodifiableMap(data.stream().collect(Collectors.toMap(T::getId, t -> t)));
    }

    public Map<String, List<T>> mapUsingKey(Function<T, String> keyProvider) {
        return data.stream().collect(Collectors.groupingBy(keyProvider));
    }

    public Optional<T> getById(String id) {
        return Optional.ofNullable(dataByIdentifier.get(id));
    }

    public List<T> getAll() {
        return List.copyOf(data);
    }

    public List<T> search(Predicate<T> predicate) {
        return data.stream().filter(predicate).collect(Collectors.toList());
    }

    public int getCount() {
        return data.size();
    }

    private List<T> loadDataFromFile() {
        CsvMapper mapper = new CsvMapper();
        List<T> fileData = new ArrayList<>();
        CsvSchema schema = mapper.schemaFor(type).withSkipFirstDataRow(true);
        try (MappingIterator<T> iterator = mapper.readerFor(type).with(schema).readValues(file)) {
            while (iterator.hasNext()) {
                T t = iterator.next();
                fileData.add(t);
            }
        } catch (Exception e) {
            throw new DataException("could not load file " + file.getAbsolutePath(), e);
        }
        return Collections.unmodifiableList(fileData);
    }
}