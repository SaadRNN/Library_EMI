package domainModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    public enum Role{ ADMIN , USER};
    private Role role;

    //PATTERN_OF_MATCH//
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(gmail|hotmail|outlook|yahoo|student\\.emi\\.ac|emi\\.ac)\\.(com|fr|ma)$"
    );    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'?/<>,.]).{8,}$");
//ID//
public int getId() {return id;}
public void setId(int id) {
    if (id >0) {
        this.id = id;
    } else {
        throw new IllegalArgumentException("id cannot be negative");
    }
}
//USERNAME//
public String getUsername() {return this.username;}
public void setUsername(String username) {
    if (username == null ||   username.isBlank()) {throw new IllegalArgumentException("enter a username !");}
    else {this.username = username;}
}
//EMAIL//
public String getEmail() {return this.email;}
public void setEmail(String email) {
    if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Veuillez saisir un email !");
    }
    Matcher matcher = EMAIL_PATTERN.matcher(email);
    if(!matcher.matches()) {
        throw new IllegalArgumentException("Veuillez saisir un email !");
    }
    this.email = email;
    }
    //PASSWORD//
public String getPassword() {return this.password;}
public void setPassword(String password) {
    if(password == null || password.isBlank()) { throw new IllegalArgumentException("Veuillez saisir un password !");}
    Matcher matcher = PASSWORD_PATTERN.matcher(password);
    if (!matcher.matches()) {throw new SecurityException("Weak Password");}
    else {this.password=password;}
}
//ROLE
    public Role getRole() {return this.role;}
    public void setRole(Role role) {this.role = role;}
//CONSTRUCTOR//
public User(int id, String username, String email, String password,String role) {
    setId(id);
    setUsername(username);
    setEmail(email);
    setPassword(password);
    try {
        this.role = Role.valueOf(role.toUpperCase());
    } catch (IllegalArgumentException |NullPointerException e) {
        this.role=Role.USER;
    }
}

}
