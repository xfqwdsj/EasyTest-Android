package xyz.xfqlittlefan.easytest.data;

import java.util.ArrayList;
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

    public void setType(Integer type) {
        this.type = type;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getScoreType() {
        return scoreType;
    }

    public void setScoreType(Integer scoreType) {
        if (scoreType != null) this.scoreType = scoreType;
    }

    public List<String> getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(List<String> userAnswer) {
        if (userAnswer != null) this.userAnswer = userAnswer;
    }

    public Integer getMaxSelecting() {
        return maxSelecting;
    }

    public void setMaxSelecting(Integer maxSelecting) {
        this.maxSelecting = maxSelecting;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
        if (userAnswer == null) {
            userAnswer = new ArrayList<>();
            for (int i = 0; i < answers.size(); i ++) {
                userAnswer.add("");
            }
        }
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
        if (userAnswer == null) {
            userAnswer = new ArrayList<>();
            for (int i = 0; i < options.size(); i ++) {
                userAnswer.add("");
            }
        }
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

        public void setScore(List<Float> score) {
            this.score = score;
        }

        public Boolean getExactMatch() {
            return exactMatch;
        }

        public void setExactMatch(Boolean exactMatch) {
            if (exactMatch != null ) this.exactMatch = exactMatch;
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

        public void setCorrect(Boolean correct) {
            if (correct != null) isCorrect = correct;
        }

        public Float getScore() {
            return score;
        }

        public void setScore(Float score) {
            if (score != null) this.score = score;
        }
    }
}
