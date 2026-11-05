package ma.smartcampus.common.security;

public final class Roles {

    public static final String ETUDIANT = "ETUDIANT";
    public static final String ENSEIGNANT = "ENSEIGNANT";
    public static final String ADMIN = "ADMIN";

    /** En-têtes posés par la gateway après validation du JWT (les services re-valident tout de même le jeton). */
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";

    private Roles() {
    }
}
