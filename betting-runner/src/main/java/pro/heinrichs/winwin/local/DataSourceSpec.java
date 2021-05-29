package pro.heinrichs.winwin.local;

import lombok.Data;

@Data
public final class DataSourceSpec {
    private String namespace;
    private String name;
    private String[] authors;
    private Limits limits;
    private StepSpec[] pipeline;
}
