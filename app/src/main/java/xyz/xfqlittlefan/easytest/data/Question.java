package xyz.xfqlittlefan.easytest.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Question {
    private Integer type;
    private String question;
    private List<Answer> answers;
    private List<Option> options;
    private Integer scoreType = 1;
    private List<String> userAnswer;
    private Integer maxSelecting;

    public Integer getType() {
        return type;
    }

    public String getQuestion() {
        return question;
    }

    public Integer getScoreType() {
        return scoreType;
    }

    public List<String> getUserAnswer() {
        if (userAnswer == null) {
            List<Object> list = new ArrayList<>();
            if (answers != null && options == null) {
                list = Collections.singletonList(answers);
            } else if (options != null && answers == null) {
                list = Collections.singletonList(options);
            }
            userAnswer = new ArrayList<>();
            for (int i = 0; i < ((List<Object>) list.get(0)).size(); i ++) {
                userAnswer.add("");
            }
        }
        return userAnswer;
    }

    public void setUserAnswer(List<String> userAnswer) {
        if (userAnswer != null) this.userAnswer = userAnswer;
    }

    public Integer getMaxSelecting() {
        return maxSelecting;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public List<Option> getOptions() {
        return options;
    }

    public static class Answer {
        private List<String> text;
        private List<Float> score;
        private Boolean exactMatch = true;

        public List<String> getText() {
            return text;
        }

        public void setText(List<String> text) {
            this.text = text;
        }

        public List<Float> getScore() {
            if (score.size() == 1) {
                for (int i = 0; i < text.size() - 1; i ++) {
                    score.add(score.get(0));
                }
            }
            return score;
        }

        public Boolean getExactMatch() {
            return exactMatch;
        }
    }

    public static class Option {
        private String text;
        private Boolean isCorrect = false;
        private Float score = 0F;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Boolean isCorrect() {
            return isCorrect;
        }

        public Float getScore() {
            return score;
        }
    }
}
