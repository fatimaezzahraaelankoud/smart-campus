package ma.smartcampus.common.event;

/**
 * Catalogue des topics (section 5.8 du cahier de développement). Toute modification passe par une PR
 * relue par l'architecte.
 */
public final class Topics {

    public static final String USER_CREATED = "campus.user.created";
    public static final String USER_ROLE_CHANGED = "campus.user.role-changed";
    public static final String USER_DEACTIVATED = "campus.user.deactivated";
    public static final String PASSWORD_RESET_REQUESTED = "campus.auth.password-reset-requested";

    public static final String STUDENT_CREATED = "campus.student.created";
    public static final String STUDENT_UPDATED = "campus.student.updated";
    public static final String ENROLLMENT_DECIDED = "campus.enrollment.decided";
    public static final String GRADE_PUBLISHED = "campus.grade.published";

    public static final String TEACHER_CREATED = "campus.teacher.created";
    public static final String TEACHER_UPDATED = "campus.teacher.updated";
    public static final String TIMETABLE_SESSION_CHANGED = "campus.timetable.session-changed";
    public static final String TIMETABLE_EXCEPTION_CREATED = "campus.timetable.exception-created";

    public static final String RESERVATION_REQUESTED = "campus.reservation.requested";
    public static final String RESERVATION_DECIDED = "campus.reservation.decided";
    public static final String RESERVATION_CANCELLED = "campus.reservation.cancelled";

    public static final String EVENT_PUBLISHED = "campus.event.published";
    public static final String EVENT_UPDATED = "campus.event.updated";
    public static final String EVENT_CANCELLED = "campus.event.cancelled";
    public static final String EVENT_VALIDATION_REQUESTED = "campus.event.validation-requested";
    public static final String EVENT_VALIDATED = "campus.event.validated";
    public static final String EVENT_REGISTRATION_PROMOTED = "campus.event.registration-promoted";
    public static final String EVENT_REMINDER = "campus.event.reminder";

    private Topics() {
    }

    public static String deadLetter(String topic) {
        return topic + ".dlt";
    }
}
