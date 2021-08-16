package xyz.xfqlittlefan.easytest.data;

import static xyz.xfqlittlefan.easytest.util.UtilClass.QUESTION_BANK_ID;
import static xyz.xfqlittlefan.easytest.util.UtilClass.QUESTION_SET_ID;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Keep
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

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Set> getSet() {
        return set;
    }

    public void init() {
        for (int i = 0; i < set.size(); i++) {
            set.get(i).savedQuestionSet = this;
            set.get(i).init();
        }
    }

    @Keep
    public static class Set {
        private QuestionSet savedQuestionSet;
        private Set savedSet;
        private String name;
        private String description;
        private String url;
        private String id;
        private final Boolean random = false;
        private List<Set> children;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getUrl() {
            return url;
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

        public List<Set> getChildren() {
            return children;
        }

        public void init() {
            if (children != null) {
                for (int i = 0; i < children.size(); i++) {
                    children.get(i).savedSet = this;
                    children.get(i).savedQuestionSet = savedQuestionSet;
                    children.get(i).init();
                }
            }
        }

        public QuestionSet getQuestionSet() {
            if (savedQuestionSet != null) {
                return savedQuestionSet;
            } else {
                return savedSet.getQuestionSet();
            }
        }

        public List<Set> getList() {
            if (savedSet != null) {
                return savedSet.children;
            } else if (savedQuestionSet != null) {
                return savedQuestionSet.set;
            } else {
                return new ArrayList<>();
            }
        }

        public String getQuestionSetUrl() {
            if (getQuestionSet() == null) {
                return "";
            }
            return getQuestionSet().url;
        }

        public Integer getIndex() {
            int index = 114514;
            List<Set> set = getList();
            for (int i = 0; i < set.size(); i++) {
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
            return Objects.equals(name, set.name) && Objects.equals(description, set.description) && Objects.equals(url, set.url) && Objects.equals(id, set.id) && Objects.equals(random, set.random);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, description, url, id, random);
        }
    }
}