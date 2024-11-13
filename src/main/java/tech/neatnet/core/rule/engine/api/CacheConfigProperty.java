package tech.neatnet.core.rule.engine.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CacheConfigProperty {
    private String name;
    private Class<?> keyType;
    private Class<?> valueType;
    private int heapSize;
}
