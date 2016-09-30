package cn.seu.herald_android.app_module.pedetail;

import java.util.Calendar;

import cn.seu.herald_android.custom.CalendarUtils;

public class ExerciseUtil {
    public enum ExerciseStatus {
        BeforeExercise, DuringExercise, AfterExercise
    }

    public static ExerciseStatus getCurrentExerciseStatus() {
        Calendar nowCal = Calendar.getInstance();
        final long now = nowCal.getTimeInMillis();
        long today = CalendarUtils.toSharpDay(nowCal).getTimeInMillis();
        long startTime = today + (long) (6 * 60 + 20) * 60 * 1000;
        long endTime = today + (long) (7 * 60 + 20) * 60 * 1000;

        if (now < startTime) {
            return ExerciseStatus.BeforeExercise;
        } else if (now < endTime) {
            return ExerciseStatus.DuringExercise;
        } else {
            return ExerciseStatus.AfterExercise;
        }
    }
}
