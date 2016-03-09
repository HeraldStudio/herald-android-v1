package cn.seu.herald_android.mod_query.curriculum;

import org.json.JSONArray;

import java.util.Comparator;

public class TermInfo {

    public int gradeYear;

    public int termOfGrade;

    public String data = null;

    public boolean currentTerm = false;

    public JSONArray sidebar = null;

    public int weekCount = 0;

    public TermInfo(int gradeYear, int termOfGrade) {
        this.gradeYear = gradeYear;
        this.termOfGrade = termOfGrade;
    }

    public static TermInfo createFromString(String src) {
        try {
            String[] split = src.split("-");
            int gradeYear = Integer.valueOf(split[0]);
            int termOfGrade = Integer.valueOf(split[2]);
            return new TermInfo(gradeYear, termOfGrade);
        } catch (Exception e) {
            return null;
        }
    }

    public static Comparator<TermInfo> comparator = new Comparator<TermInfo>() {
        @Override
        public int compare(TermInfo lhs, TermInfo rhs) {
            return (lhs.gradeYear * 10 + lhs.termOfGrade) - (rhs.gradeYear * 10 + rhs.termOfGrade);
        }
    };

    @Override
    public String toString() {
        return gradeYear + "-" + (gradeYear + 1) + "-" + termOfGrade;
    }
}
