package xyz.xfqlittlefan.easytest.data;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestionSet {
    private String name;
    private String id;
    private String url;
    private List<Set> set;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Set> getSet() {
        return set;
    }

    public void setSet(List<Set> set) {
        for (int i = 0; i < set.size(); i++) {
            set.get(i).savedQuestionSet = this;
        }
        this.set = set;
    }

    public static class Set {
        private QuestionSet savedQuestionSet;
        private Set savedSet;
        private String name;
        private String description;
        private String url;
        private String id;
        private Boolean random = false;
        private List<Set> children = new ArrayList<>();

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

        public List<Set> getChildren() {
            return children;
        }

        public void setChildren(List<Set> children) {
            if (children != null) {
                for (int i = 0; i < children.size(); i++) {
                    children.get(i).savedSet = this;
                }
                this.children = children;
            }
        }

        public QuestionSet getQuestionSet() {
            if (savedQuestionSet != null) {
                return savedQuestionSet;
            } else {
                QuestionSet questionSet = null;
                Set set = this;
                while (questionSet == null) {
                    if (set.savedQuestionSet != null) {
                        questionSet = set.savedQuestionSet;
                    } else if (set.savedSet != null) {
                        set = set.savedSet;
                    } else {
                        break;
                    }
                }
                return questionSet;
            }
        }

        public List<Set> getList() {
            if (savedQuestionSet != null) {
                return savedQuestionSet.set;
            } else if (savedSet != null) {
                return savedSet.children;
            } else {
                return new ArrayList<>();
            }
        }

        public String getOuterUrl() {
                if (getQuestionSet() == null) {
                    return "";
                }
                return getQuestionSet().url;
        }

        public Integer getIndex() {
            int index = 114514;
            List<Set> set = getList();
            for (int i = 0; i < set.size(); i ++) {
                if (set.get(i).equals(this)) {
                    index = i;
                    break;
                }
            }
            return index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Set set = (Set) o;
            return Objects.equals(name, set.name) && Objects.equals(description, set.description) && Objects.equals(url, set.url) && Objects.equals(id, set.id) && Objects.equals(random, set.random) && Objects.equals(children, set.children);
        }


    }
}