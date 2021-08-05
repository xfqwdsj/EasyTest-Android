package xyz.xfqlittlefan.easytest.data;

import org.litepal.crud.LitePalSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Result extends LitePalSupport {

    private String question;
    private String state;
    private Long id;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
