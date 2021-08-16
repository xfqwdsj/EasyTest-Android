package xyz.xfqlittlefan.easytest.data;

import androidx.annotation.Keep;

import org.litepal.crud.LitePalSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Keep
public class Result extends LitePalSupport {

    private String question;
    private String state;
    private String url;
    private String setId;
    private Long id;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isCorrect() {
        return (question != null && state != null && setId != null && url != null);
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }
}
