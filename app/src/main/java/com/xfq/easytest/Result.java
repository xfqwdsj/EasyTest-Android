package com.xfq.easytest;

import org.litepal.crud.LitePalSupport;

import java.util.List;

class Result extends LitePalSupport {
    private Integer id;
    private String question;
    private List<String> answer;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getAnswer() {
        return answer;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }
}
