package com.xfq.easytest;

import java.util.List;

class Question {
    private Integer type;
    private String question;
    private List<Children> children;
    private Integer scoreType;
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

    public List<Children> getChildren() {
        return children;
    }

    public void setChildren(List<Children> children) {
        this.children = children;
    }

    public Integer getScoreType() {
        return scoreType;
    }

    public void setScoreType(Integer scoreType) {
        this.scoreType = scoreType;
    }

    public List<String> getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(List<String> userAnswer) {
        this.userAnswer = userAnswer;
    }

    public Integer getMaxSelecting() {
        return maxSelecting;
    }

    public void setMaxSelecting(Integer maxSelecting) {
        this.maxSelecting = maxSelecting;
    }

    static class Children {
        private String text;
        private Float score;
        private Boolean exactMatch;
        private Boolean isCorrect;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Float getScore() {
            return score;
        }

        public void setScore(Float score) {
            this.score = score;
        }

        public Boolean getExactMatch() {
            return exactMatch;
        }

        public void setExactMatch(Boolean exactMatch) {
            this.exactMatch = exactMatch;
        }

        public Boolean getIsCorrect() {
            return isCorrect;
        }

        public void setIsCorrect(Boolean isCorrect) {
            this.isCorrect = isCorrect;
        }
    }
}
