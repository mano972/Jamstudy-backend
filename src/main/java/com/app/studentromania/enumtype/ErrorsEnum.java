package com.app.studentromania.enumtype;

import org.springframework.http.HttpStatus;

public enum ErrorsEnum {

    NO_ERROR(HttpStatus.OK, "Success", 0), GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Eroare.", -1),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Eroare baza de date.", -2),

    // Config
    CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND, "Config was not found.", -3),

    // Admin Login
    ADMIN_LOGIN_MISSING_HEADER(HttpStatus.BAD_REQUEST, "Authentication header missing.", -4),
    ADMIN_LOGIN_WRONG_CREDENTIALS(HttpStatus.BAD_REQUEST, "Admin username/password not correct", -5),

    // Faculty
    FACULTY_NOT_FOUND(HttpStatus.NOT_FOUND, "Facultatea nu a fost gasită.", -10),
    FACULTY_EXISTS(HttpStatus.NOT_ACCEPTABLE, "Facultatea exista deja.", -11),
    FACULTY_ID_MISSING(HttpStatus.BAD_REQUEST, "Lipsete id-ul facultatii.", -12),
    FACULTY_LOGO_ERROR(HttpStatus.NOT_FOUND, "A apărut o eroare la încarcărea logo-ului.", -13),

    // University
    UNIVERSITY_NOT_FOUND(HttpStatus.NOT_FOUND, "Universitatea nu a fost gasită.", -20),
    UNIVERSITY_EXISTS(HttpStatus.NOT_ACCEPTABLE, "Universitatea exista deja.", -21),
    UNIVERSITY_ID_MISSING(HttpStatus.BAD_REQUEST, "Lipsete id-ul universitatii.", -22),

    // Review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "Evaluarea nu a fost gasită.", -30),
    REVIEW_EXISTS(HttpStatus.NOT_ACCEPTABLE, "Evaluarea exista deja.", -31),
    REVIEW_ID_MISSING(HttpStatus.BAD_REQUEST, "Lipsete id-ul evaluarii", -32),
    REVIEW_GENERAL_RATING_ERROR(HttpStatus.BAD_REQUEST, "Nota acordata nu este corecta.", -33),
    REVIEW_DIFFICULTY_ERROR(HttpStatus.BAD_REQUEST, "Valoarea dificultatii nu este corecta.", -34),
    REVIEW_ALREADY_UPVOTED(HttpStatus.NOT_ACCEPTABLE, "Această evaluare a fost deja votată.", -35),
    REVIEW_MAX_NUMBER(HttpStatus.NOT_ACCEPTABLE, "Ai atins deja numărul maxim de evaluări adăugate.", -36),
    REVIEW_SAME_FACULTY(HttpStatus.NOT_ACCEPTABLE,
            "Ai adăugat deja o evaluare pentru această facultate. Poți adăuga din nou după %s zile.", -37),
    REVIEW_UPDATE_DIFFERENT_USER(HttpStatus.BAD_REQUEST,
            "User-ul care a editat evaluarea este diferit de userul care a creat-o.", -38),
    REVIEW_NOT_EDITABLE(HttpStatus.NOT_ACCEPTABLE, "Evaluarea nu mai este editabilă.", -39),
    REVIEW_LENGTH_ERROR(HttpStatus.NOT_ACCEPTABLE, "Lungimea textului evaluarii nu este corecta.", -49),

    // Question
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Întrebarea nu a fost gasită.", -50),

    // Answer
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "Raspunsul nu a fost gasit.", -60),

    // UserProfile
    USERPROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "Nu există user cu acest email.", -70),
    USERPROFILE_EXISTS(HttpStatus.NOT_ACCEPTABLE, "Un user cu acest email există deja.", -71),
    USERPROFILE_LOGIN_WRONG_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Datele de autentificare sunt incorecte.", -72),
    USERPROFILE_EMAIL_PASSWORD_MISSING(HttpStatus.BAD_REQUEST, "Email/parola nu au fost completate.", -73),
    JWT_GENERATION_ERROR(HttpStatus.BAD_REQUEST, "Eroare la generarea JWT.", -74),
    JWT_MISSING(HttpStatus.UNAUTHORIZED, "Token-ul JWT lipseste din request.", -75),
    JWT_VERIFY_ERROR(HttpStatus.UNAUTHORIZED, "Token-ul JWT nu este corect.", -76),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "Token-ul JWT a expirat.", -77),
    USER_NOT_LOGGED_IN(HttpStatus.UNAUTHORIZED, "Este nevoie de autentificare pentru a folosi această funcționalitate.",
            -78),
    USERPROFILE_MAX_FAVORITE_FACULTIES(HttpStatus.NOT_ACCEPTABLE,
            "Ai adăugat deja numărul maxim de favorite.", -79),
    REGISTER_EMAIL_NOT_VALID(HttpStatus.NOT_ACCEPTABLE, "Adresa de email nu este validă.", -80),
    REGISTER_TERMS_AND_CONDITIONS(HttpStatus.NOT_ACCEPTABLE,
            "Trebuie să accepți Termenii și condițiile de utilizare și Politica de prelucrare a datelor.", -81),
    REGISTER_PASSWORD_ERROR(HttpStatus.NOT_ACCEPTABLE,
            "Parola trebuie să fie de minim 7 caractere și să conțină litere și cifre.", -82),
    USERPROFILE_LOGIN_EMAIL_NOT_CONFIRMED(HttpStatus.UNAUTHORIZED, "Adresa de email nu a fost încă confirmată.", -83),
    USERPROFILE_REGISTER_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED,
            "Verificarea adresei de email a eșuat. Link-ul nu mai este valabil.", -84),
    USERPROFILE_REGISTER_VERIFICATION_EXPIRED(HttpStatus.UNAUTHORIZED,
            "Timpul alocat verificării adresei de email a expirat.", -85),
    USERPROFILE_RESET_PASS_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED,
            "Resetarea parolei a eșuat. Link-ul nu mai este valabil.", -86),
    USERPROFILE_EMAIL_CONFIRMATION_FAILED(HttpStatus.BAD_REQUEST, "Email-ul de confirmare nu a putut fi trimis.", -87),

    // Statistic
    ANALYTICS_NOT_FOUND(HttpStatus.NOT_FOUND, "Analytics document was not found.", -90),
    ANALYTICS_DOCUMENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Analytics document already exists.", -91),
    ANALYTICS_EXCEL_DOCUMENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Analytics excel document already exists.", -92),
    ANALYTICS_EXCEL_DOCUMENT_ERROR(HttpStatus.BAD_REQUEST, "Analytics excel error.", -93),
    ANALYTICS_EXCEL_DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Analytics excel document was not found.", -94),

    // Newsletter
    NEWSLETTER_NOT_FOUND(HttpStatus.NOT_FOUND, "Newsletter was not found.", -100),
    NEWSLETTER_EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST,
            "Deja ești abonat la newsletter cu această adresă de e-mail. Îți mulțumim!", -101),
    NEWSLETTER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Newsletter document already exists.", -102),

    // Feedback
    FEEDBACK_DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Feedback document was not found.", -110),
    FEEDBACK_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Feedback document already exists.", -111),

    // Email
    EMAIL_AUTH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Email: eroare la autentificare.", -120),
    EMAIL_SENDING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Email: eroare generală.", -121),

    // Company
    COMPANY_EXISTS(HttpStatus.NOT_ACCEPTABLE, "Compania exista deja.", -130),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "Compania nu a fost gasita.", -131),

    // EntityProfile
    ENTITY_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "Profilul nu a fost gasit.", -140);

    private String errorDescription;
    private int errorCode;
    private HttpStatus httpStatus;

    private ErrorsEnum(HttpStatus httpStatus, String errorDescription, int errorCode) {
        this.errorDescription = errorDescription;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public ErrorsEnum formatErrorDescription(Object... params) {
        this.setErrorDescription(String.format(this.getErrorDescription(), params));
        return this;
    }

}
