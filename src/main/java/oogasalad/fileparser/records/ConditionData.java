package oogasalad.fileparser.records;

import java.util.Map;

public record ConditionData(String name,
                            Map<String,String> stringProperties,
                            Map<String,Double> doubleProperties
                            ) {}
