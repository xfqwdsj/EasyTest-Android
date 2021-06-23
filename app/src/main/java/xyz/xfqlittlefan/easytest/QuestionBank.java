package xyz.xfqlittlefan.easytest;

import java.util.ArrayList;
import java.util.List;

public class QuestionBank {
    private String name;
    private String description;
    private String url;
    private String id;
    private Boolean random = false;
    private List<QuestionBank> children = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getRandom() {
        return random;
    }

    public void setRandom(Boolean random) {
        if (random != null) this.random = random;
    }

    public List<QuestionBank> getChildren() {
        return children;
    }

    public void setChildren(List<QuestionBank> children) {
        if (children != null) this.children = children;
    }
}