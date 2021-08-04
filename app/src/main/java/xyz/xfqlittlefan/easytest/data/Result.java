package xyz.xfqlittlefan.easytest.data;

import org.litepal.crud.LitePalSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Result extends LitePalSupport {

    private String question;
    //          题目索引     “空”索引       类型     数值
    private Map<Integer, Map<Integer, Map<Integer, Float>>> state;
    private Long id;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Map<Integer, Map<Integer, Map<Integer, Float>>> getState() {
        return state;
    }

    public void setState(Map<Integer, Map<Integer, Map<Integer, Float>>> state) {
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
